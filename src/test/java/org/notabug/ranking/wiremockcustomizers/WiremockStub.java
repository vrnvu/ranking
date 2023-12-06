package org.notabug.ranking.wiremockcustomizers;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.util.StringUtils;
import wiremock.com.fasterxml.jackson.core.JsonProcessingException;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.trace;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class WiremockStub implements Snippet {
  private static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

    @Override
    public String getId() {
      return "json";
    }

    @Override
    public String getFileExtension() {
      return "json";
    }
  };

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private String snippetName = "stubs";

  private final Set<String> headerBlackList = new HashSet<>(Arrays.asList("host", "content-length"));

  private Set<String> jsonPaths = new LinkedHashSet<>();

  private MediaType contentType;

  private StubMapping stubMapping;

  private final int priority;

  private boolean hasJsonBodyRequestToMatch = false;

  private boolean hasXmlBodyRequestToMatch = false;

  public WiremockStub() {
    // This constructor is intentionally empty. Default stub will be created.
    this.priority = StubMapping.DEFAULT_PRIORITY;
  }

  public WiremockStub(StubMapping stubMapping) {
    this.stubMapping = stubMapping;
    this.priority = stubMapping.getPriority() != null
        ? stubMapping.getPriority()
        : StubMapping.DEFAULT_PRIORITY;
  }

  public WiremockStub(int priority) {
    this.priority = priority;
  }

  @Override
  public void document(Operation operation) throws IOException {
    extractMatchers(operation);
    if (this.stubMapping == null) {
      this.stubMapping = request(operation)
          .willReturn(response(operation))
          .atPriority(priority)
          .build();
    } else {
      var stubbedResponse = this.stubMapping.getResponse();

      var responseFromOperation = response(operation);
      responseFromOperation = addHeadersFromStubIfPresent(stubbedResponse, responseFromOperation);
      responseFromOperation = addBodyFromStubIfPresent(stubbedResponse, responseFromOperation);
      responseFromOperation = addTransformersFromStubIfPresent(stubbedResponse, responseFromOperation);

      MappingBuilder requestBuilder = getMappingBuilderWithQueryParams(operation);

      stubMapping = requestBuilder
          .willReturn(responseFromOperation)
          .atPriority(priority)
          .build();
    }

    String json = Json.write(this.stubMapping);
    RestDocumentationContext context;
    context = (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName());
    RestDocumentationContextPlaceholderResolverFactory placeholders;
    placeholders = new RestDocumentationContextPlaceholderResolverFactory();
    WriterResolver writerResolver = new StandardWriterResolver(placeholders, "UTF-8", TEMPLATE_FORMAT);
    try (Writer writer = writerResolver.resolve(this.snippetName, operation.getName(), context)) {
      writer.append(json);
    }
  }

  private MappingBuilder getMappingBuilderWithQueryParams(Operation operation) {
    var requestBuilder = request(operation);
    if (stubMappingHasQueryParameters()) {
      this.stubMapping.getRequest().getQueryParameters().forEach((name, multiValuePattern) ->
          requestBuilder.withQueryParam(name, multiValuePattern.getValuePattern())
      );
    }
    if (stubMappingHasHeaders()) {
      this.stubMapping.getRequest().getHeaders().forEach((name, multiValuePattern) ->
          requestBuilder.withHeader(name, multiValuePattern.getValuePattern())
      );
    }
    return requestBuilder;
  }

  private boolean stubMappingHasRequest() {
    return this.stubMapping != null
        && this.stubMapping.getRequest() != null;
  }

  private boolean stubMappingHasHeaders() {
    return stubMappingHasRequest()
        && this.stubMapping.getRequest().getHeaders() != null;
  }

  private boolean stubMappingHasQueryParameters() {
    return stubMappingHasRequest()
        && this.stubMapping.getRequest().getQueryParameters() != null;
  }

  private static ResponseDefinitionBuilder addHeadersFromStubIfPresent(
      ResponseDefinition stubbedResponse, ResponseDefinitionBuilder responseFromOperation) {

    if (stubbedResponse.getHeaders() != null) {
      var newHeaders = new HttpHeaders(stubbedResponse.getHeaders().all());

      var uniqueHeadersFromOperation = Optional
          .ofNullable(responseFromOperation.build().getHeaders())
          .map(httpHeaders -> httpHeaders.all().stream()
              .filter(httpHeader -> !newHeaders.keys().contains(httpHeader.caseInsensitiveKey().value()))
              .toList().toArray(HttpHeader[]::new));

      if (uniqueHeadersFromOperation.isPresent()) {
        return responseFromOperation.withHeaders(newHeaders.plus(uniqueHeadersFromOperation.get()));
      } else {
        return responseFromOperation.withHeaders(newHeaders);
      }
    }

    return responseFromOperation;
  }

  private static ResponseDefinitionBuilder addTransformersFromStubIfPresent(
      ResponseDefinition stubbedResponse, ResponseDefinitionBuilder responseFromOperation) {

    if (stubbedResponse.getTransformers() != null && !stubbedResponse.getTransformers().isEmpty()) {
      return responseFromOperation.withTransformers(stubbedResponse.getTransformers().toArray(String[]::new));
    }

    return responseFromOperation;
  }

  private static ResponseDefinitionBuilder addBodyFromStubIfPresent(ResponseDefinition stubbedResponse,
      ResponseDefinitionBuilder responseFromOperation) {
    if (stubbedResponse.getBody() != null) {
      if (isContentTypeJson(stubbedResponse) || isContentTypeJson(responseFromOperation.build())) {
        try {
          return responseFromOperation.withJsonBody(OBJECT_MAPPER.readTree(stubbedResponse.getBody()));
        } catch (JsonProcessingException e) {
          return responseFromOperation.withBody(stubbedResponse.getBody());
        }
      } else {
        return responseFromOperation.withBody(stubbedResponse.getBody());
      }
    } else if (stubbedResponse.getJsonBody() != null) {
      return responseFromOperation.withJsonBody(stubbedResponse.getJsonBody());
    }

    return responseFromOperation;
  }

  private static boolean isContentTypeJson(ResponseDefinition stubbedResponse) {
    return stubbedResponse.getHeaders() != null
        && stubbedResponse.getHeaders().getContentTypeHeader().isPresent()
        && stubbedResponse.getHeaders().getContentTypeHeader().containsValue("application/json");
  }

  private void extractMatchers(Operation operation) {
    if (this.stubMapping != null) {
      return;
    }
    @SuppressWarnings("unchecked")
    Set<String> jsonPaths = (Set<String>) operation.getAttributes().get("contract.jsonPaths");
    this.jsonPaths = jsonPaths;
    this.contentType = (MediaType) operation.getAttributes().get("contract.contentType");
    if (this.contentType == null) {
      this.hasJsonBodyRequestToMatch = hasJsonContentType(operation);
      this.hasXmlBodyRequestToMatch = hasXmlContentType(operation);
    }
  }

  private boolean hasJsonContentType(Operation operation) {
    return hasContentType(operation, MediaType.APPLICATION_JSON);
  }

  private boolean hasXmlContentType(Operation operation) {
    return hasContentType(operation, MediaType.APPLICATION_XML);
  }

  private boolean hasContentType(Operation operation, MediaType mediaType) {
    return operation.getRequest().getHeaders().getContentType() != null
        && operation.getRequest().getHeaders().getContentType().isCompatibleWith(mediaType);
  }

  private ResponseDefinitionBuilder response(Operation operation) {
    String content = operation.getResponse().getContentAsString();
    ResponseDefinitionBuilder response = aResponse().withHeaders(responseHeaders(operation));
    if (content != null) {
      if (isContentTypeJson(response.build())) {
        try {
          response = response.withJsonBody(OBJECT_MAPPER.readTree(content));
        } catch (JsonProcessingException e) {
          response = response.withBody(content);
        }
      } else {
        response = response.withBody(content);
      }

      if (content.contains("localhost:{{request.requestLine.port}}")) {
        response = response.withTransformers("response-template");
      }
    }
    return response.withStatus(operation.getResponse().getStatus().value());
  }

  private MappingBuilder request(Operation operation) {
    return queryParams(requestHeaders(requestBuilder(operation), operation), operation);
  }

  private MappingBuilder queryParams(MappingBuilder request, Operation operation) {
    String rawQuery = operation.getRequest().getUri().getRawQuery();
    if (!StringUtils.hasText(rawQuery)) {
      return request;
    }
    MappingBuilder requestWithParams = request;
    for (String queryPair : rawQuery.split("&")) {
      String[] splitQueryPair = queryPair.split("=");
      String value = splitQueryPair.length > 1 ? splitQueryPair[1] : "";
      requestWithParams = requestWithParams.withQueryParam(splitQueryPair[0], equalTo(value));
    }
    return requestWithParams;
  }

  private MappingBuilder requestHeaders(MappingBuilder request, Operation operation) {
    org.springframework.http.HttpHeaders headers = operation.getRequest().getHeaders();
    MappingBuilder requestWithHeaders = request;
    // TODO: whitelist headers
    for (String name : headers.keySet()) {
      if (!this.headerBlackList.contains(name.toLowerCase(Locale.getDefault()))) {
        if ("content-type".equalsIgnoreCase(name) && this.contentType != null) {
          continue;
        }
        if ("x-error".equalsIgnoreCase(name)) {
          requestWithHeaders = requestWithHeaders
              .withHeader(name, equalToIgnoreCase(headers.getFirst(name)));
        } else {
          requestWithHeaders = requestWithHeaders
              .withHeader(name, equalTo(headers.getFirst(name)));
        }
      }
    }
    if (this.contentType != null) {
      requestWithHeaders = requestWithHeaders
          .withHeader("Content-Type", matching(Pattern.quote(this.contentType.toString()) + ".*"));
    }

    return requestWithHeaders;
  }

  private MappingBuilder requestBuilder(Operation operation) {
    return switch (operation.getRequest().getMethod().name()) {
      case "DELETE" -> delete(requestPattern(operation));
      case "POST" -> isRequestWithImageContent(operation)
          ? bodyPattern(post(requestPattern(operation)), operation.getRequest().getContent())
          : bodyPattern(post(requestPattern(operation)), operation.getRequest().getContentAsString());
      case "PUT" -> isRequestWithImageContent(operation)
          ? bodyPattern(put(requestPattern(operation)), operation.getRequest().getContent())
          : bodyPattern(put(requestPattern(operation)), operation.getRequest().getContentAsString());
      case "PATCH" -> isRequestWithImageContent(operation)
          ? bodyPattern(patch(requestPattern(operation)), operation.getRequest().getContent())
          : bodyPattern(patch(requestPattern(operation)), operation.getRequest().getContentAsString());
      case "GET" -> get(requestPattern(operation));
      case "HEAD" -> head(requestPattern(operation));
      case "OPTIONS" -> options(requestPattern(operation));
      case "TRACE" -> trace(requestPattern(operation));
      default ->
          throw new UnsupportedOperationException("Unsupported method type: " + operation.getRequest().getMethod());
    };
  }

  private static boolean isRequestWithImageContent(Operation operation) {
    MediaType requestMediaType = operation.getRequest().getHeaders().getContentType();
    return requestMediaType != null && requestMediaType.getType().equals("image");
  }

  @SuppressWarnings("PMD.AvoidMultipleUnaryOperators")
  private MappingBuilder bodyPattern(MappingBuilder builder, String content) {
    if (stubbedBodyPatternsArePresent()) {
      this.stubMapping.getRequest().getBodyPatterns()
          .forEach(builder::withRequestBody);
    } else if (this.jsonPaths != null && !this.jsonPaths.isEmpty()) {
      for (String jsonPath : this.jsonPaths) {
        builder.withRequestBody(matchingJsonPath(jsonPath));
      }
    } else if (!!StringUtils.hasText(content)) {
      if (this.hasJsonBodyRequestToMatch) {
        builder.withRequestBody(equalToJson(content));
      } else if (this.hasXmlBodyRequestToMatch) {
        builder.withRequestBody(equalToXml(content));
      } else {
        builder.withRequestBody(equalTo(content));
      }
    }
    return builder;
  }

  private MappingBuilder bodyPattern(MappingBuilder builder, byte[] content) {
    if (stubbedBodyPatternsArePresent()) {
      this.stubMapping.getRequest().getBodyPatterns()
          .forEach(builder::withRequestBody);
    } else if (this.jsonPaths != null && !this.jsonPaths.isEmpty()) {
      for (String jsonPath : this.jsonPaths) {
        builder.withRequestBody(matchingJsonPath(jsonPath));
      }
    } else if (content != null) {
      builder.withRequestBody(binaryEqualTo(content));
    }
    return builder;
  }

  private boolean stubbedBodyPatternsArePresent() {
    return this.stubMapping != null
        && this.stubMapping.getRequest() != null
        && this.stubMapping.getRequest().getBodyPatterns() != null;
  }

  private UrlPattern requestPattern(Operation operation) {
    if (this.stubMapping != null && this.stubMapping.getRequest().getUrlMatcher() != null) {
      return this.stubMapping.getRequest().getUrlMatcher();
    }
    return urlPathEqualTo(operation.getRequest().getUri().getPath());
  }

  private HttpHeaders responseHeaders(Operation operation) {
    org.springframework.http.HttpHeaders headers = operation.getResponse().getHeaders();
    HttpHeaders result = new HttpHeaders();
    for (String name : headers.keySet()) {
      if (!this.headerBlackList.contains(name.toLowerCase(Locale.getDefault()))) {
        result = result.plus(new HttpHeader(name, headers.get(name)));
      }
    }
    return result;
  }
}

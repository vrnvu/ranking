package org.notabug.ranking.wiremockcustomizers.transformers;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

public class RequestIdTransformer extends ResponseTransformer {
  @Override
  public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
    if (request.containsHeader("X-Request-id") && !response.getHeaders().getHeader("X-Request-id").isPresent()) {
      HttpHeaders responseHeaders = response
          .getHeaders()
          .plus(HttpHeader.httpHeader("X-Request-id", request.getHeader("X-Request-id")));

      return Response.Builder.like(response)
          .but().headers(responseHeaders)
          .build();
    }

    return response;
  }

  @Override
  public String getName() {
    return "request-id-transformer";
  }

  @Override
  public boolean applyGlobally() {
    return true;
  }
}

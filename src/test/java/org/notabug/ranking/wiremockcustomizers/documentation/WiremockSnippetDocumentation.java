package org.notabug.ranking.wiremockcustomizers.documentation;

import com.epages.restdocs.apispec.ResourceSnippetDetails;
import com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.notabug.ranking.wiremockcustomizers.WiremockStub;
import org.springframework.cloud.contract.wiremock.restdocs.ContractDslSnippet;
import org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

public final class WiremockSnippetDocumentation {
  private WiremockSnippetDocumentation() {
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(
      String identifier,
      ResourceSnippetDetails resourceDetails,
      OperationRequestPreprocessor requestPreprocessor,
      OperationResponsePreprocessor responsePreprocessor,
      Function<List<Snippet>, List<Snippet>> snippetFilter,
      Snippet... snippets) {

    return WebTestClientRestDocumentationWrapper.document(identifier,
        resourceDetails,
        requestPreprocessor,
        responsePreprocessor,
        snippetFilter,
        snippetsWithDefaults(snippets)
    );
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(
      String identifier,
      ResourceSnippetDetails resourceDetails,
      Snippet... snippets) {

    return WebTestClientRestDocumentationWrapper.document(identifier,
        resourceDetails,
        snippetsWithDefaults(snippets)
    );
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(String identifier, Snippet... snippets) {
    return WebTestClientRestDocumentationWrapper.document(identifier, snippetsWithDefaults(snippets));
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(String identifier,
      OperationRequestPreprocessor requestPreprocessor,
      Snippet... snippets) {
    return WebTestClientRestDocumentationWrapper.document(identifier,
        requestPreprocessor, snippetsWithDefaults(snippets));
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(String identifier,
      OperationResponsePreprocessor responsePreprocessor,
      Snippet... snippets) {
    return WebTestClientRestDocumentationWrapper.document(identifier,
        responsePreprocessor,
        snippetsWithDefaults(snippets));
  }

  public static <T> Consumer<EntityExchangeResult<T>> documentStub(String identifier,
      OperationRequestPreprocessor requestPreprocessor,
      OperationResponsePreprocessor responsePreprocessor,
      Snippet... snippets) {
    return WebTestClientRestDocumentationWrapper.document(identifier,
        requestPreprocessor,
        responsePreprocessor,
        snippetsWithDefaults(snippets));
  }

  public static Snippet[] snippetsWithDefaults(Snippet... snippets) {
    var newSnippets = snippetsWithDefaultWiremockStub(snippets);
    return snippetsWithDefaultContractDslSnippet(newSnippets);
  }

  private static Snippet[] snippetsWithDefaultWiremockStub(Snippet... snippets) {
    if (Arrays.stream(snippets)
        .anyMatch(snippet -> snippet instanceof WiremockStub)) {
      return snippets;
    }

    var newSnippets = new ArrayList<>(List.of(snippets));
    newSnippets.add(new WiremockStub());

    return newSnippets.toArray(Snippet[]::new);
  }

  private static Snippet[] snippetsWithDefaultContractDslSnippet(Snippet... snippets) {
    if (Arrays.stream(snippets)
        .anyMatch(snippet -> snippet instanceof ContractDslSnippet)) {
      return snippets;
    }

    var newSnippets = new ArrayList<>(List.of(snippets));
    newSnippets.add(SpringCloudContractRestDocs.dslContract());

    return newSnippets.toArray(Snippet[]::new);
  }
}

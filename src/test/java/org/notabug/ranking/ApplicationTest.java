package org.notabug.ranking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.notabug.ranking.wiremockcustomizers.documentation.WiremockSnippetDocumentation.documentStub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@AutoConfigureObservability
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApplicationTest {

  @Autowired
  public WebTestClient webClient;

  @Test
  void healthcheckIsOk() {
    webClient.get()
        .uri("/health")
        .exchange()
        .expectStatus().isOk()
        .expectBody().jsonPath("$.status")
        .isEqualTo("UP")
        .consumeWith(documentStub("healthcheck"));
  }
}

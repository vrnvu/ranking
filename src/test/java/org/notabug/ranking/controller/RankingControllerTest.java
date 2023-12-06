package org.notabug.ranking.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.notabug.ranking.TestConfig;
import org.notabug.ranking.wiremockcustomizers.WiremockStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.notabug.ranking.wiremockcustomizers.documentation.WiremockSnippetDocumentation.documentStub;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestDocs
@AutoConfigureObservability
@Import({TestConfig.class})
public class RankingControllerTest {

  @Autowired
  public WebTestClient webClient;

  @Test
  public void shouldReturn404WhenUrlIsNotFound() {
    webClient.get().uri("/url-does-not-exist", "")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .consumeWith(documentStub("get-url-does-not-exist-404",
            new WiremockStub(WireMock.get(urlPathEqualTo("/url-does-not-exist"))
                .build()
            )));
  }
}
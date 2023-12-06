package org.notabug.ranking.wiremockcustomizers.contracts;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import io.restassured.RestAssured;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.notabug.ranking.wiremockcustomizers.transformers.RequestIdTransformer;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BaseStubTest {
  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig()
          .port(1234)
          .mappingSource(new JsonFileMappingsSource(new SingleRootFileSource("build/generated-snippets/stubs")))
          .extensions(new ResponseTemplateTransformer(false), new RequestIdTransformer())
      )
      .build();

  @BeforeEach
  void setUp() {
    RestAssured.port = 1234;
    RestAssured.baseURI = "http://localhost";
    RestAssuredWebTestClient.standaloneSetup(WebTestClient.bindToServer()
        .baseUrl("http://localhost:1234")
    );
  }
}

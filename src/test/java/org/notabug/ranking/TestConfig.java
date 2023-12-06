package org.notabug.ranking;

import java.net.URI;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsWebTestClientConfigurationCustomizer;
import org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.web.util.DefaultUriBuilderFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@Configuration
public class TestConfig {
  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    AwsBasicCredentials creds =
        AwsBasicCredentials.create("foo", "bar");
    return StaticCredentialsProvider.create(creds);
  }

  @Bean
  public DynamoDbAsyncClient dynamoDbAsyncClient(AwsCredentialsProvider awsCredentialsProvider) {
    return DynamoDbAsyncClient.builder()
        .region(Region.EU_WEST_1)
        .endpointOverride(URI.create("http://localhost:8000"))
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }

  @Bean
  public RestDocsWebTestClientConfigurationCustomizer restDocsWebTestClientConfigurationCustomizer() {
    return configurer -> configurer.snippets()
        .withDefaults(new DoNothingSnippet())
        .and()
        .operationPreprocessors()
        .withRequestDefaults(prettyPrint())
        .withResponseDefaults(prettyPrint());
  }

  @Bean
  WebTestClientBuilderCustomizer customizer() {
    return (builder) -> {
      var builderFactory = new DefaultUriBuilderFactory();

      // this is needed otherwise all test parameters like "foo/bar"
      // will be automatically encoded to "foo%2Fbar"
      builderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
      builder.uriBuilderFactory(builderFactory);
    };
  }

  public static class DoNothingSnippet implements Snippet {
    @Override
    public void document(Operation operation) {
    }
  }
}

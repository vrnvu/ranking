package org.notabug.ranking;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

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
}

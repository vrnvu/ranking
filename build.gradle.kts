import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
  java
  jacoco
  id("org.springframework.boot") version "3.2.0"
  id("io.spring.dependency-management") version "1.1.4"
  id("com.adarshr.test-logger") version "3.2.0"
  id("com.epages.restdocs-api-spec") version "0.19.0"
//  id("org.springframework.cloud.contract") version "4.0.4"
}

group = "org.notabug"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

repositories {
  maven { url = uri("https://repo.spring.io/milestone") }
  mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0-RC1"

dependencies {
  // Spring
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

  // AWS
  implementation("software.amazon.awssdk:dynamodb-enhanced")

  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.1")
  testImplementation("com.playtika.reactivefeign:feign-reactor-core:4.0.0")
//  implementation("io.rest-assured:rest-assured")
  testImplementation("org.springframework.restdocs:spring-restdocs-core")
  testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
  testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock")

  testImplementation("com.epages:restdocs-api-spec-webtestclient:0.19.0")
  // For some reason if this needed for dependencies to be reachable, seems a bug in the package.
  testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:localstack:1.19.3")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("io.rest-assured:spring-web-test-client")
  testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
  testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
  testImplementation("org.testcontainers:junit-jupiter")
}

configurations {
  all {
    exclude(module = "spring-boot-starter-logging")
    exclude(module = "spring-boot-starter-tomcat")
    exclude(module = "spring-boot-starter-jetty")
  }
}

configure<DependencyManagementExtension> {
  overriddenByDependencies(true)
  imports {
    mavenBom("software.amazon.awssdk:bom:2.20.148")
    // this is to upgrade reactor-core to the latest version from 3.5.6 to 3.5.9 which fixes the issue with id correlation
    // as recommended in here: https://github.com/reactor/reactor-core/issues/3553,
    // when spring upgrades to this version in their pom this can be removed
    mavenBom("io.projectreactor:reactor-bom:2022.0.12")
  }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<JacocoReport> {
  reports {
    xml.required.set(true)
    html.required.set(false)
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  environment("AWS_ACCESS_KEY_ID", "foo")
  environment("AWS_SECRET_ACCESS_KEY", "bar")
  environment("AWS_REGION", "eu-west-1")
  finalizedBy(tasks.jacocoTestReport)
}


// TODO :copyContracts not needed for now
//tasks.contractTest {
//  useJUnitPlatform()
//}
//
//contracts {
//  testMode.set(TestMode.WEBTESTCLIENT)
//}


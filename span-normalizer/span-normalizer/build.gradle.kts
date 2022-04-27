plugins {
  java
  application
  jacoco
  id("org.hypertrace.docker-java-application-plugin")
  id("org.hypertrace.docker-publish-plugin")
  id("org.hypertrace.jacoco-report-plugin")
}

application {
  mainClass.set("org.hypertrace.core.serviceframework.PlatformServiceLauncher")
}

hypertraceDocker {
  image("hypertrace-ui") {
    dockerFile.set(file("./build/docker/Dockerfile"))
    javaApplication {
      serviceName.set("${project.name}")
      adminPort.set(8099)
    }
  }
  tag("${project.name}" + "_" + getCommitHash())
}

// Config for gw run to be able to run this locally. Just execute gw run here on Intellij or on the console.
tasks.run<JavaExec> {
  jvmArgs = listOf("-Dservice.name=${project.name}")
}

tasks.test {
  useJUnitPlatform()
}

dependencies {
  implementation(project(":span-normalizer:raw-span-constants"))
  implementation(project(":span-normalizer:span-normalizer-api"))
  implementation(project(":span-normalizer:span-normalizer-constants"))
  implementation(project(":semantic-convention-utils"))

  implementation("org.hypertrace.core.datamodel:data-model:0.1.19")
  implementation("org.hypertrace.core.serviceframework:platform-service-framework:0.1.26")
  implementation("org.hypertrace.core.serviceframework:platform-metrics:0.1.26")
  implementation("org.hypertrace.core.kafkastreams.framework:kafka-streams-framework:0.1.21")

  // Required for the GRPC clients.
  runtimeOnly("io.grpc:grpc-netty:1.41.0")
  constraints {
    runtimeOnly("io.netty:netty-codec-http2:4.1.68.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1083991")
    }
    runtimeOnly("io.netty:netty-handler-proxy:4.1.68.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1083991")
    }
    implementation("org.glassfish.jersey.core:jersey-common:2.34") {
      because("https://snyk.io/vuln/SNYK-JAVA-ORGGLASSFISHJERSEYCORE-1255637")
    }
  }

  implementation("com.typesafe:config:1.4.1")
  implementation("de.javakaffee:kryo-serializers:0.45")
  implementation("io.confluent:kafka-avro-serializer:6.0.1")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("org.apache.httpcomponents:httpclient:4.5.13")

  // Logging
  implementation("org.slf4j:slf4j-api:1.7.30")
  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

  testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
  testImplementation("org.hypertrace.core.serviceframework:platform-metrics:0.1.26")
  testImplementation("org.junit-pioneer:junit-pioneer:1.3.8")
  testImplementation("org.mockito:mockito-core:3.8.0")
  testImplementation("org.apache.kafka:kafka-streams-test-utils:6.0.1-ccs")
}

fun getCommitHash(): String {
  val os = com.bmuschko.gradle.docker.shaded.org.apache.commons.io.output.ByteArrayOutputStream()
  project.exec {
    commandLine = "git rev-parse --verify HEAD".split(" ")
    standardOutput = os
  }
  return String(os.toByteArray()).trim()
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "4.0.3"
//  pmd
  jacoco
//  id("com.github.spotbugs") version "2.0.0"
}

repositories {
  mavenCentral()
  jcenter()
}

val vertxVersion = "3.8.0"
val junitVersion = "5.3.2"
val hamcrestVersion = "1.3"

val logbackVersion = "1.2.3"
val owaspSecurityLoggingVersion = "1.1.6"
val javaUuidGeneratorVersion = "3.2.0"

dependencies {
  implementation("io.vertx:vertx-core:$vertxVersion")
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-config:$vertxVersion")
  implementation("io.vertx:vertx-config-yaml:$vertxVersion")
  implementation("io.vertx:vertx-config-kubernetes-configmap:$vertxVersion")
  implementation("io.vertx:vertx-service-discovery:$vertxVersion")


  // Logging
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  implementation("org.owasp:security-logging-logback:$owaspSecurityLoggingVersion")
  implementation("com.fasterxml.uuid:java-uuid-generator:$javaUuidGeneratorVersion")

  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("io.vertx:vertx-web-client:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
  testImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
  mainClassName = "io.vertx.core.Launcher"
}

val mainVerticleName = "io.luminara.quickstart.vertxweb.MainVerticle"
val watchForChange = "src/**/*.java"
val doOnChange = "${projectDir}/gradlew classes"

jacoco {
  toolVersion = "0.8.4"
  reportsDir = file("$buildDir/customJacocoReportDir")
}

tasks {
  test {
    useJUnitPlatform()
  }

  getByName<JavaExec>("run") {
    args = listOf("run", mainVerticleName, "--redeploy=${watchForChange}", "--launcher-class=${application.mainClassName}", "--on-redeploy=${doOnChange}")
  }

  withType<ShadowJar> {
    classifier = "fat"
    manifest {
      attributes["Main-Verticle"] = mainVerticleName
    }
    mergeServiceFiles {
      include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }
  }
}

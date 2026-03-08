plugins {
    java
    application
}

application {
    mainClass = "com.nullang.Repl"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.register<JavaExec>("runFile") {
    group = "application"
    description = "Run a Nullang file from resources (use -Pfile=path/to/file.null)"
    mainClass = "com.nullang.RunFile"
    classpath = sourceSets["main"].runtimeClasspath
    if (project.hasProperty("file")) {
        systemProperty("file", project.property("file"))
    }
}
group = "com.nullang"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.assertj:assertj-core:4.0.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }
}

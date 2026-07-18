plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    implementation("org.jsoup:jsoup:1.18.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.wiremock:wiremock-standalone:3.10.0")
    // Gradle 7.6 + JUnit 5 호환성 위해 명시 (Gradle 8+ 부터는 자동)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // ThreadLocalMap reflection 접근에 필요
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // bug 재현 시 OOM 빠르게 나도록 힙 작게
    if (project.hasProperty("leakRepro")) {
        jvmArgs("-Xmx128m", "-XX:+HeapDumpOnOutOfMemoryError",
                "-XX:HeapDumpPath=${projectDir}/dumps/")
    }
}

tasks.bootRun {
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-Xmx256m",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=${projectDir}/dumps/"
    )
}

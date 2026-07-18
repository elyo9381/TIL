plugins {
    java
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.6"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
}

tasks.test {
    useJUnitPlatform()
}

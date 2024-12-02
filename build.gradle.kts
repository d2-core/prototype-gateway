import com.palantir.gradle.docker.DockerExtension

plugins {
    java
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("com.palantir.docker") version "0.35.0"
}

rootProject.group = "com.d2"
version = "1.0.0"
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2022.0.3"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("org.springframework.boot:spring-boot-starter-security")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testRuntimeOnly("com.h2database:h2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<DockerExtension> {

    val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
    val registry = "d2-core"
    name = "$registry/${project.name}:${project.version}"

    //TODO: PATH CHANGE
    val path = "$rootDir/../prototype-infra/dockerfile/gateway/Dockerfile"
    setDockerfile(file(path))

    files(bootJar.outputs.files)

    buildArgs(mapOf("JAR_FILE" to bootJar.outputs.files.singleFile.name))
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
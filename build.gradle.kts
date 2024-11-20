import com.palantir.gradle.docker.DockerExtension

plugins {
    java
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("com.palantir.docker") version "0.35.0"
}

group = "com.d2"
version = "1.0.0"
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2022.0.3"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configure<DockerExtension> {

    val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
    name = "${rootProject.name}/${project.name}:${project.version}"

    //TODO: PATH CHANGE
    setDockerfile(file("$rootDir/../prototype-infra/spring-app/Dockerfile"))

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
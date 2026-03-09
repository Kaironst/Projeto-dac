import com.github.gradle.node.npm.task.NpmTask

plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "br.ufpr.dac"
version = "0.0.1-SNAPSHOT"

node {
    download.set(true)
    version.set("22.18.0")
    npmVersion.set("10.9.3")

    // where package.json is
    nodeProjectDir.set(file("../../frontend"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/*
 * Runs: npm run build
 */
tasks.register<NpmTask>("buildFrontend") {
    dependsOn("npmInstall")
    workingDir.set(file("../../frontend"))
    args.set(listOf("run", "build"))
}

/*
 * Make Spring build depend on Angular build
 */
tasks.processResources {
    dependsOn("buildFrontend")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

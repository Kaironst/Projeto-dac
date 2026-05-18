plugins {
	java
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "br.ufpr.dac"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
  mavenLocal()
	mavenCentral()
}

dependencies {
  implementation("br.ufpr.dac:shared:1.0.0")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	testImplementation("org.springframework.boot:spring-boot-starter-amqp-test")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-test")
	testImplementation("org.springframework.boot:spring-boot-starter-mongodb-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.amqp:spring-rabbit-stream")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

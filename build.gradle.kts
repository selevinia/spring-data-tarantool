group = "io.github.selevinia"
version = "0.1.0"
description = "Reactive Spring Data module for Tarantool"

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    create("integration") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations["integrationImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
configurations["integrationCompileOnly"].extendsFrom(configurations.testCompileOnly.get())
configurations["integrationAnnotationProcessor"].extendsFrom(configurations.testAnnotationProcessor.get())

dependencies {
    implementation("io.tarantool:cartridge-driver:0.4.3")
    implementation("org.springframework:spring-context:5.3.8")
    implementation("org.springframework:spring-tx:5.3.8")
    implementation("org.springframework.data:spring-data-commons:2.4.10")
    implementation("io.projectreactor:reactor-core:3.4.7")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

    testImplementation("org.springframework:spring-test:5.3.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("io.projectreactor:reactor-test:3.4.7")
    testImplementation("org.slf4j:slf4j-simple:1.7.26")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
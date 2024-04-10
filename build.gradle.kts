group = "io.github.selevinia"
version = "0.5.0"
description = "Spring Data module for Tarantool Database"

plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
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
    api("org.springframework.data:spring-data-commons:3.2.4")
    api("org.springframework:spring-context:6.1.5")
    api("org.springframework:spring-tx:6.1.5")
    api("org.springframework:spring-context:6.1.5")

    implementation("io.tarantool:cartridge-driver:0.13.0")

    implementation("io.projectreactor:reactor-core:3.6.4")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation("io.netty:netty-buffer:4.1.108.Final")
    implementation("io.netty:netty-codec:4.1.108.Final")
    implementation("io.netty:netty-common:4.1.108.Final")
    implementation("io.netty:netty-handler:4.1.108.Final")
    implementation("io.netty:netty-transport:4.1.108.Final")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.108.Final")
    implementation("io.netty:netty-resolver-dns:4.1.108.Final")
    implementation("io.netty:netty-transport-native-epoll:4.1.108.Final")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("org.springframework:spring-test:6.1.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
    testImplementation("org.slf4j:slf4j-simple:2.0.12")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf(
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.math=ALL-UNNAMED",
        "--add-opens=java.base/sun.util.locale=ALL-UNNAMED"
    )
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Spring Data Tarantool")
                description.set("Spring Data module for Tarantool Database")
                url.set("https://github.com/selevinia/spring-data-tarantool")

                scm {
                    connection.set("scm:git:git://github.com/selevinia/spring-data-tarantool.git")
                    developerConnection.set("scm:git:ssh://github.com/selevinia/spring-data-tarantool.git")
                    url.set("https://github.com/selevinia/spring-data-tarantool")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("rx-alex")
                        name.set("Alexander Rublev")
                        email.set("invalidator.post@gmail.com")
                    }
                    developer {
                        id.set("t-obscurity")
                        name.set("Tatiana Blinova")
                        email.set("blinova.tv@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = rootProject.findProperty("nexus.username").toString()
                password = rootProject.findProperty("nexus.password").toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
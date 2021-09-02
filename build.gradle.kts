group = "io.github.selevinia"
version = "0.3.1"
description = "Spring Data module for Tarantool Database"

plugins {
    `java-library`
    `maven-publish`
    signing
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
    api("org.springframework.data:spring-data-commons:2.4.10")
    api("org.springframework:spring-context:5.3.8")
    api("org.springframework:spring-tx:5.3.8")
    api("org.springframework:spring-context:5.3.8")

    implementation("io.tarantool:cartridge-driver:0.4.3")
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
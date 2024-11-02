plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.poohscord.pooheconomy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    implementation(platform("io.projectreactor:reactor-bom:2023.0.11"))
    implementation("io.projectreactor:reactor-core")

    implementation("org.mongodb:mongodb-driver-reactivestreams:5.2.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    dependsOn(tasks.build)
    archiveFileName.set("pooheconomy-${project.version}.jar")

    doFirst {
        println("Generated fatJars!")
    }
}
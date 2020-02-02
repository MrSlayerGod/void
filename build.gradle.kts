plugins {
    kotlin("jvm") version "1.3.61"
}

buildscript {
    repositories {
        jcenter()
    }
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "redrune"
    version = "0.0.2"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://repo.maven.apache.org/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.bintray.com/michaelbull/maven")

    }

    dependencies {
        //Main
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.netty:netty-all:4.1.44.Final")
        implementation("com.github.Displee:RS-Cache-Library:5.2")
        compile(group = "org.yaml", name = "snakeyaml", version = "1.8")
        compile(group = "io.github.classgraph", name = "classgraph", version = "4.6.3")
        compile(group = "com.michael-bull.kotlin-inline-logger", name = "kotlin-inline-logger-jvm", version = "1.0.1")

        //Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")

        //Utilities
        implementation("com.google.guava:guava:19.0")
        implementation("org.apache.commons:commons-lang3:3.0")

        //Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

}
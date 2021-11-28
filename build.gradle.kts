plugins {
    kotlin("multiplatform") version "1.5.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            commonWebpackConfig {
            }
            webpackTask {
                destinationDirectory = File(projectDir, "./docs/")
            }
            dceTask {
                dceOptions {
                    keep("klox.klox.Lox")
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val jsMain by getting
    }
}

repositories {
    mavenCentral()
}

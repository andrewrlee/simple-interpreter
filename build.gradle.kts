plugins {
    kotlin("multiplatform") version "1.5.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        withJava()
    }
    js() {
        browser {
            commonWebpackConfig {
            }
            webpackTask {
            }
            dceTask {
                dceOptions { devMode = true }
//                keep("klox.klox.Lox", "klox.klox.Lox.run")
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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion = "3.3.3"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.ktor:ktor-client-core:${ktorVersion}")
            implementation("io.ktor:ktor-client-cio:${ktorVersion}")
            implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            implementation("io.mockk:mockk:1.13.13")
            implementation("io.ktor:ktor-client-mock:${ktorVersion}")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.example.spring_profiler_app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.spring_profiler_app"
            packageVersion = "1.0.0"
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                // Exclude generated code
                classes("*ComposableSingletons*")
                classes("*.BuildConfig")
                classes("*\$\$serializer")

                // Exclude main function
                packages("com.example.spring_profiler_app.MainKt")

                // Exclude Compose generated code
                classes("*\$\$ExternalSyntheticLambda*")
            }
        }

        verify {
            rule {
                minBound(70)
            }
        }
    }
}

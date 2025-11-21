plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.scheduler"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.scheduler"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Starts the Ktor backend in the background before Android builds so developers
// can interact with the app without manually launching the server.
val backendProject = project(":backend:api")
val backendInstallDir = backendProject.layout.buildDirectory.dir("install/api")
val backendStartScript = backendInstallDir.map { it.file("bin/api").asFile }

tasks.register<Exec>("startBackendForLocalDev") {
    group = "application"
    description = "Starts the backend API if it is not already running."
    dependsOn(backendProject.tasks.named("installDist"))

    doFirst {
        // The command starts the generated installDist script in the background unless it is already running.
        commandLine(
            "bash",
            "-c",
            """
            if pgrep -f 'com.example.scheduler.backend.ServerKt' > /dev/null; then
              echo "Backend API already running on port 8080"
            else
              nohup ${backendStartScript.get().absolutePath} >/tmp/backend-api.log 2>&1 &
              echo "Started backend API (logs: /tmp/backend-api.log)"
            fi
            """.trimIndent()
        )
    }
}

tasks.named("preBuild").configure {
    dependsOn("startBackendForLocalDev")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yn.setbox"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.yn.setbox"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-ripple")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0") 
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("dev.rikka.shizuku:api:12.1.0")
    implementation("dev.rikka.shizuku:provider:12.1.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.slf4j:slf4j-nop:2.0.12") 
    implementation("org.snakeyaml:snakeyaml-engine:2.7")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
}
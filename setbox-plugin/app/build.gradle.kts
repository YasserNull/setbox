plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") 
}
android {
    namespace = "com.yn.setbox.plugin"
    compileSdk = 22
    defaultConfig {
        applicationId = "com.yn.setbox.plugin"
        minSdk = 21
        targetSdk = 22
        versionCode = 1
        versionName = "1.0" 
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
       abortOnError = false
       checkReleaseBuilds = false
   }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

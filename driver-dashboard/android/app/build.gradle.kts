plugins {
    id("com.android.application")
}

android {
    namespace = "br.com.hospitalidadeabordo.driver"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.hospitalidadeabordo.driver"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

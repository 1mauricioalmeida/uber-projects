plugins {
    id("com.android.application")
}

android {
    namespace = "br.com.hospitalidadeabordo.driver"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.hospitalidadeabordo.driver"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "0.2.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

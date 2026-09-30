plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.splashskip.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.splashskip.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 只在电脑上跑单元测试时用，不会打包进 App
    testImplementation("junit:junit:4.13.2")
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.takephoto"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.takephoto"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Hapus baris ini jika sudah menggunakan libs.*
    // implementation 'androidx.appcompat:appcompat:1.6.1'
    // implementation 'com.google.android.material:material:1.9.0'
    // implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    // implementation 'androidx.cardview:cardview:1.0.0'

    // Gunakan versi dari catalog (libs.versions.toml)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.cardview) // Pastikan cardview didefinisikan di libs.versions.toml

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

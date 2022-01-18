plugins {
    id("com.android.library")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 23
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.5"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(mapOf("path" to ":gamedata")))
    implementation(project(mapOf("path" to ":ui")))
    implementation(project(mapOf("path" to ":web")))

    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    api("com.google.firebase:firebase-firestore-ktx")
    api("com.google.firebase:firebase-auth-ktx")

    implementation("com.google.dagger:hilt-android:2.40.5")
    kapt("com.google.dagger:hilt-compiler:2.40.5")
    implementation("com.google.android.material:material:1.5.0")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.compose.ui:ui:1.0.5")
    implementation("androidx.compose.ui:ui-tooling-preview:1.0.5")
    implementation("androidx.compose.ui:ui-tooling:1.0.5")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.5")
    implementation("androidx.compose.material:material:1.0.5")
    implementation("androidx.compose.animation:animation:1.0.5")
    implementation("androidx.compose.compiler:compiler:1.0.5")
    implementation("androidx.compose.foundation:foundation:1.0.5")
    implementation("androidx.compose.runtime:runtime:1.0.5")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
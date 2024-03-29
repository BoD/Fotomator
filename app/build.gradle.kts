plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = AppConfig.COMPILE_SDK

    defaultConfig {
        applicationId = AppConfig.APPLICATION_ID
        minSdk = AppConfig.MIN_SDK
        targetSdk = AppConfig.TARGET_SDK
        versionCode = AppConfig.buildNumber
        versionName = AppConfig.buildProperties["versionName"]

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // For now we enable strict mode for all the configs
        buildConfigField("boolean", "STRICT_MODE", "true")
        // For now we enable debug logs all the configs
        buildConfigField("boolean", "DEBUG_LOGS", "true")

        resourceConfigurations.set("en", "fr")

        // Useful for api keys in the manifest (Maps, Crashlytics, ...)
        manifestPlaceholders.set(AppConfig.buildProperties as Map<String, Any>)

        // Setting this to true disables the png generation at buildtime
        // (see http://android-developers.blogspot.fr/2016/02/android-support-library-232.html)
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(AppConfig.buildProperties["signingStoreFile"]!!)
            storePassword = AppConfig.buildProperties["signingStorePassword"]
            keyAlias = AppConfig.buildProperties["signingKeyAlias"]
            keyPassword = AppConfig.buildProperties["signingKeyPassword"]
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"

            buildConfigField("String", "GIT_SHA1", "\"dev\"")
            buildConfigField("String", "BUILD_DATE", "\"dev\"")
        }

        getByName("release") {
            buildConfigField("String", "GIT_SHA1", "\"${getGitSha1()}\"")
            buildConfigField("String", "BUILD_DATE", "\"${buildDate}\"")

            // Disable proguard for now - trying to investigate "killed service" issues and obfuscation doesn't help
//            isMinifyEnabled = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//            isShrinkResources = true

            isMinifyEnabled = false
            isShrinkResources = false

            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    lint {
        abortOnError = true
        textReport = true
        ignoreWarnings = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.ANDROIDX_COMPOSE
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib", Versions.KOTLIN))

    // AndroidX
    implementation("androidx.core", "core-ktx", Versions.ANDROIDX_CORE)
    implementation("androidx.appcompat", "appcompat", Versions.ANDROIDX_APPCOMPAT)
    implementation("androidx.fragment", "fragment-ktx", Versions.ANDROIDX_FRAGMENT)
    implementation("androidx.activity", "activity-ktx", Versions.ANDROIDX_ACTIVITY)
    implementation("androidx.lifecycle", "lifecycle-livedata-ktx", Versions.ANDROIDX_LIFECYCLE)
    implementation("androidx.room", "room-runtime", Versions.ANDROIDX_ROOM)
    kapt("androidx.room", "room-compiler", Versions.ANDROIDX_ROOM)
    implementation("androidx.room", "room-ktx", Versions.ANDROIDX_ROOM)
    implementation("androidx.work", "work-runtime-ktx", Versions.ANDROIDX_WORK_MANAGER)
    kapt("androidx.hilt", "hilt-compiler", Versions.ANDROIDX_HILT)

    // Material
    implementation("com.google.android.material", "material", Versions.MATERIAL)

    // JRAF
    implementation("org.jraf", "kprefs", Versions.KPREFS)
    implementation("com.github.BoD", "jraf-android-util", "-SNAPSHOT")
//    implementation("org.jraf", "jraf-android-util", "1.0.0")

    // Retrofit / Moshi
    implementation("com.squareup.retrofit2", "retrofit", Versions.RETROFIT)
    implementation("com.squareup.retrofit2", "converter-moshi", Versions.RETROFIT)
    implementation("com.squareup.moshi", "moshi", Versions.MOSHI)
    kapt("com.squareup.moshi", "moshi-kotlin-codegen", Versions.MOSHI)
    implementation("com.squareup.okhttp3", "logging-interceptor", Versions.OKHTTP)

    // Hilt
    implementation("com.google.dagger", "hilt-android", Versions.HILT)
    kapt("com.google.dagger", "hilt-compiler", Versions.HILT)

    // Play services
    implementation("com.google.android.gms", "play-services-oss-licenses", Versions.PLAY_SERVICES_OSS_LICENSE)

    // Compose
    implementation("androidx.compose.ui", "ui", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.ui", "ui-tooling", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.foundation", "foundation", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.material", "material", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.material", "material-icons-core", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.material", "material-icons-extended", Versions.ANDROIDX_COMPOSE)
    implementation("androidx.compose.material3", "material3", Versions.ANDROIDX_COMPOSE_MATERIAL3)
    implementation("androidx.activity", "activity-compose", Versions.ANDROIDX_ACTIVITY)
    implementation("androidx.lifecycle", "lifecycle-viewmodel-compose", Versions.ANDROIDX_LIFECYCLE_VIEWMODEL_COMPOSE)
    implementation("androidx.compose.runtime", "runtime-livedata", Versions.ANDROIDX_COMPOSE)

    // Testing
    androidTestImplementation("androidx.test.espresso", "espresso-core", Versions.ESPRESSO) {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit", "junit", Versions.JUNIT)
}

// Run `./gradlew bundleRelease` to build a release version

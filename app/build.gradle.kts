plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(AppConfig.COMPILE_SDK)

    defaultConfig {
        applicationId = AppConfig.APPLICATION_ID
        minSdkVersion(AppConfig.MIN_SDK)
        targetSdkVersion(AppConfig.TARGET_SDK)
        versionCode = AppConfig.buildNumber
        versionName = AppConfig.buildProperties["versionName"]

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // For now we enable strict mode for all the configs
        buildConfigField("boolean", "STRICT_MODE", "true")
        // For now we enable debug logs all the configs
        buildConfigField("boolean", "DEBUG_LOGS", "true")

        resConfigs("en", "fr")

        // Useful for api keys in the manifest (Maps, Crashlytics, ...)
        manifestPlaceholders = AppConfig.buildProperties as Map<String, Any>

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

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    lintOptions {
        isAbortOnError = true
        textReport = true
        isIgnoreWarnings = true
    }

    compileOptions {
        incremental = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

afterEvaluate {
    android.applicationVariants.forEach { variant ->
        // Create new copy tasks, for release builds
        if (variant.buildType.name == "release") {
            variant.outputs.forEach { output ->
                val outputFile = file("build/outputs/apk/${variant.flavorName}/release/${output.outputFile.name}")
                val apkName = "${rootProject.name}-${project.name}-${android.defaultConfig.versionCode}-${variant.flavorName}-signed.apk"

                // Copy the apk to the 'etc' folder
                val copyApkToEtc = tasks.register<Copy>("copy${variant.name.capitalize()}ApkToEtc") {
                    from(outputFile)
                    into("../etc/apk")
                    rename(output.outputFile.name, apkName)
                }

                // Copy the apk to the deploy folder
                val copyApkToDeploy = tasks.register<Copy>("copy${variant.name.capitalize()}ApkToDeploy") {
                    from(outputFile)
                    into("${AppConfig.buildProperties["deployFolder"]}/${rootProject.name}/${android.defaultConfig.versionCode}")
                    rename(output.outputFile.name, apkName)
                }

                // Make the copy tasks run after the assemble tasks of the variant
                variant.assembleProvider!!.get().finalizedBy(copyApkToEtc, copyApkToDeploy)
            }
        }
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
    implementation("androidx.constraintlayout", "constraintlayout", Versions.ANDROIDX_CONSTRAINT_LAYOUT)
    implementation("androidx.work", "work-runtime-ktx", Versions.ANDROIDX_WORK_MANAGER)

    // Material
    implementation("com.google.android.material", "material", Versions.MATERIAL)

    // JRAF
    implementation("org.jraf", "kprefs", Versions.KPREFS)
    implementation("com.github.BoD", "jraf-android-util", "-SNAPSHOT")

    // Retrofit / Moshi
    implementation("com.squareup.retrofit2", "retrofit", Versions.RETROFIT)
    implementation("com.squareup.retrofit2", "converter-moshi", Versions.RETROFIT)
    implementation("com.squareup.moshi", "moshi", Versions.MOSHI)
    kapt("com.squareup.moshi", "moshi-kotlin-codegen", Versions.MOSHI)

    // Dagger
    implementation("com.google.dagger", "dagger", Versions.DAGGER)
    kapt("com.google.dagger", "dagger-compiler", Versions.DAGGER)
    compileOnly("javax.annotation", "jsr250-api", Versions.JSR_250)


    // Testing
    androidTestImplementation("androidx.test.espresso", "espresso-core", Versions.ESPRESSO) {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit", "junit", Versions.JUNIT)
}
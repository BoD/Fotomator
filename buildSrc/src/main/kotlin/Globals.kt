object Versions {
    // Misc and plugins
    const val GRADLE = "7.4"

    const val KOTLIN = "1.6.10"
    const val BEN_MANES_VERSIONS_PLUGIN = "0.42.0"
    const val ANDROID_GRADLE_PLUGIN = "7.1.1"
    const val OSS_LICENSES_PLUGIN = "0.10.4"

    // App dependencies
    const val ANDROIDX_CORE = "1.7.0"
    const val ANDROIDX_APPCOMPAT = "1.4.1"
    const val ANDROIDX_FRAGMENT = "1.4.1"
    const val ANDROIDX_ACTIVITY = "1.4.0"
    const val ANDROIDX_LIFECYCLE = "2.4.1"
    const val ANDROIDX_LIFECYCLE_VIEWMODEL_COMPOSE = "2.4.1"
    const val ANDROIDX_ROOM = "2.4.1"
    const val ANDROIDX_WORK_MANAGER = "2.7.1"
    const val ANDROIDX_HILT = "1.0.0"
    const val ANDROIDX_HILT_LIFECYCLE_VIEWMODEL = "1.0.0-alpha03"
    const val ANDROIDX_COMPOSE = "1.1.0"
    const val ANDROIDX_COMPOSE_MATERIAL3 = "1.0.0-alpha01"

    const val HILT = "2.41"
    const val KPREFS = "1.6.0"
    const val RETROFIT = "2.9.0"
    const val MOSHI = "1.13.0"
    const val OKHTTP = "4.9.3"
    const val MATERIAL = "1.5.0"
    const val PLAY_SERVICES_OSS_LICENSE = "17.0.0"

    // Testing dependencies
    const val ESPRESSO = "3.4.0"
    const val JUNIT = "4.13.2"
}

object AppConfig {
    const val APPLICATION_ID = "org.jraf.android.fotomator"
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 31
    const val MIN_SDK = 23

    var buildNumber: Int = 0
    val buildProperties = mutableMapOf<String, String>()
}

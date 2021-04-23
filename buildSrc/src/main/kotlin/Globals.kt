object Versions {
    // Misc and plugins
    const val GRADLE = "7.0"
    const val KOTLIN = "1.4.32"
    const val BEN_MANES_VERSIONS_PLUGIN = "0.38.0"
    const val ANDROID_GRADLE_PLUGIN = "7.0.0-alpha14"
    const val OSS_LICENSES_PLUGIN = "0.10.4"

    // App dependencies
    const val ANDROIDX_CORE = "1.3.2"
    const val ANDROIDX_APPCOMPAT = "1.2.0"
    const val ANDROIDX_FRAGMENT = "1.3.3"
    const val ANDROIDX_ACTIVITY = "1.3.0-alpha07"
    const val ANDROIDX_LIFECYCLE = "2.3.1"
    const val ANDROIDX_LIFECYCLE_VIEWMODEL_COMPOSE = "1.0.0-alpha04"
    const val ANDROIDX_ROOM = "2.2.6"
    const val ANDROIDX_WORK_MANAGER = "2.5.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "2.0.4"
    const val ANDROIDX_HILT = "1.0.0-beta01"
    const val ANDROIDX_HILT_LIFECYCLE_VIEWMODEL = "1.0.0-alpha03"
    const val ANDROIDX_COMPOSE = "1.0.0-beta05"

    const val HILT = "2.35"
    const val KPREFS = "1.6.0"
    const val RETROFIT = "2.9.0"
    const val MOSHI = "1.12.0"
    const val MATERIAL = "1.3.0"
    const val PLAY_SERVICES_OSS_LICENSE = "17.0.0"

    // Testing dependencies
    const val ESPRESSO = "3.3.0"
    const val JUNIT = "4.13.2"
}

object AppConfig {
    const val APPLICATION_ID = "org.jraf.android.fotomator"
    const val COMPILE_SDK = 30
    const val TARGET_SDK = 30
    const val MIN_SDK = 23

    var buildNumber: Int = 0
    val buildProperties = mutableMapOf<String, String>()
}
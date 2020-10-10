object Versions {
    // Misc and plugins
    const val GRADLE = "6.6.1"
    const val KOTLIN = "1.4.10"
    const val BEN_MANES_VERSIONS_PLUGIN = "0.33.0"
    const val ANDROID_GRADLE_PLUGIN = "4.0.2"

    // App dependencies
    const val ANDROIDX_CORE = "1.3.2"
    const val ANDROIDX_APPCOMPAT = "1.2.0"
    const val ANDROIDX_FRAGMENT = "1.3.0-beta01"
    const val ANDROIDX_ACTIVITY = "1.2.0-beta01"
    const val ANDROIDX_LIFECYCLE = "2.2.0"
    const val ANDROIDX_ROOM = "2.2.5"
    const val ANDROIDX_WORK_MANAGER = "2.4.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "2.0.2"
    const val ANDROIDX_HILT = "1.0.0-alpha02"
    const val HILT = "2.29.1-alpha"
    const val KPREFS = "1.4.0"
    const val RETROFIT = "2.9.0"
    const val MOSHI = "1.11.0"
    const val MATERIAL = "1.2.1"

    // Testing dependencies
    const val ESPRESSO = "3.3.0"
    const val JUNIT = "4.13"
}

object AppConfig {
    const val APPLICATION_ID = "org.jraf.android.fotomator"
    const val COMPILE_SDK = 30
    const val TARGET_SDK = 30
    const val MIN_SDK = 23

    var buildNumber: Int = 0
    val buildProperties = mutableMapOf<String, String>()
}
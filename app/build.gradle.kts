plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
//    alias(libs.plugins.kapt)
    id("kotlin-kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.navigation)
}

android {

    namespace = "com.kblack.project_base"
    compileSdk = ((rootProject.extra["versions"] as Map<*, *>)["target_sdk"] as Int?)!!

    signingConfigs {

    }

//    androidResources {
//        generateLocaleConfig = true
//    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        dex {
            useLegacyPackaging = false
        }
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "META-INF/*.version"
            // https://youtrack.jetbrains.com/issue/KT-48019/Bundle-Kotlin-Tooling-Metadata-into-apk-artifacts
            excludes += "kotlin-tooling-metadata.json"
            // https://github.com/Kotlin/kotlinx.coroutines?tab=readme-ov-file#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
        }
    }

    lint {
        lintConfig = file("lint.xml")
    }

    baselineProfile {
        dexLayoutOptimization = true
    }

    defaultConfig {
        applicationId = "com.kblack.project_base"
        minSdk = ((rootProject.extra["versions"] as Map<*, *>)["min_sdk"] as Int?)!!
        targetSdk = ((rootProject.extra["versions"] as Map<*, *>)["target_sdk"] as Int?)!!
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MY_VERSION_NAME",
            "\"$versionName${rootProject.extra["myVersionName"] as String}\""
        )
        buildConfigField(
            "String",
            "MY_COMMIT_NAME",
            "\"${rootProject.extra["commitMessage"] as String}\""
        )
        setProperty("archivesBaseName", "ToeicK-$versionName${versionNameSuffix ?: ""}")
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isPseudoLocalesEnabled = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = rootProject.extra["myVersionName"] as String
        }
//        create("staging") {
//            applicationIdSuffix = ".staging"
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    kotlinOptions {
        jvmTarget = "23"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    defaultConfig{
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.bundles.androidxCoreComponents)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycleAware)
    implementation(libs.bundles.okhttp)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.hilt.android)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.swiperefreshlayout)
    "baselineProfile"(project(":baselineprofile"))
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.roomDb)
    ksp(libs.room.compiler)
    implementation(libs.bundles.retrofit2)
    implementation(libs.material3)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.dotsindicator)
    implementation(libs.paging)
    implementation(libs.shimmer)
    implementation(libs.glide)
    implementation(libs.junit)
    implementation(libs.androidx.junit)
    implementation(libs.espresso.core)
    debugImplementation(libs.leak.canary)
}

kapt {
    correctErrorTypes = true
}
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
}


val appVersion = providers.gradleProperty("appVersion").orElse("1.0.0").get()
val appVersionParts = appVersion.split(".").map(String::toInt)
require(appVersionParts.size == 3 && appVersionParts.all { it in 0..999 }) {
    "appVersion must use MAJOR.MINOR.PATCH with each part between 0 and 999"
}
val computedVersionCode = appVersionParts[0] * 1_000_000 + appVersionParts[1] * 1_000 + appVersionParts[2]


android {
  namespace = "kr.stonecold.securitycard"
  compileSdk = 37
    compileSdkMinor = 1
  defaultConfig {
        applicationId = "kr.stonecold.securitycard"
        minSdk = 33
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val releaseStore = providers.gradleProperty("keystoreFile")
                .map(::File)
                .orElse(File(projectDir, "release-keystore.jks"))
                .get()
            storeFile = releaseStore
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
    lint {
        warningsAsErrors = true
        disable += setOf("GradleDependency", "ObsoleteSdkInt")
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name == "compose-group-mapping") {
                useVersion("2.4.21")
            }
        }
    }
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation.compose)

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  add("ksp", libs.room.compiler)

  // Hilt
  implementation(libs.hilt.android)
  add("ksp", libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)

  // DataStore & Serialization
  implementation(libs.datastore.preferences)
  implementation(libs.kotlinx.serialization.json)

  // Biometric
  implementation(libs.androidx.biometric)
}

import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE")!!)
            keyAlias = project.property("RELEASE_KEY_ALIAS")!!.toString()
            keyPassword = project.property("RELEASE_KEY_PASSWORD")!!.toString()
            storePassword = project.property("RELEASE_STORE_PASSWORD")!!.toString()
        }
    }
    namespace = "com.example.tfg"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ahydul.logic_games"
        minSdk = 26
        targetSdk = 34
        versionCode = 7 //Check licenses before changing
        versionName = "1.1.2"
        //setProperty("archivesBaseName", "logic-games-$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    applicationVariants.all{
        outputs.all {
            if(name.contains("release"))
                (this as BaseVariantOutputImpl).outputFileName = "logic-games-$versionName.apk"
        }
    }
    buildTypes {
        release {
            //isDebuggable = true
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

}
aboutLibraries {
    // When licences break reload this before making the apk... this is so stupid
    additionalLicenses += "MIT"
    configPath = "config"
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2") // Add this line
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") // Use the latest version available

    // Java to Json converters
    implementation("com.google.code.gson:gson:2.10")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    // To use Kotlin annotation processing tool (kapt)
    //kapt("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    val aboutLib = "11.2.1"
    implementation("com.mikepenz:aboutlibraries:$aboutLib") // Add this line
    implementation("com.mikepenz:aboutlibraries-compose-m3:$aboutLib")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
}

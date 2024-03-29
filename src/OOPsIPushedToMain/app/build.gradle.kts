plugins {
    alias(libs.plugins.androidApplication)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.oopsipushedtomain"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oopsipushedtomain"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.messaging)
    implementation("androidx.navigation:navigation-fragment:2.7.7")
//    implementation(libs.glide)

//    implementation(files("C:/Users/matte/AppData/Local/Android/Sdk/platforms/android-34/android.jar"))
    implementation(libs.fragment.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//    androidTestImplementation("androidx.test:core")
//    debugImplementation("androidx.fragment:fragment-testing:1.4.0")
//    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-installations")

    //Add the ZXing library for the QR code scanner https://github.com/zxing/zxing
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")
    implementation("com.google.zxing:core:3.4.0")

    // Mockito for testing
    androidTestImplementation("org.mockito:mockito-core:4.0.0")

    // Testing dependancies
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}

tasks.register("runSpecificTest") {
    doLast {
        val testClass = if (project.hasProperty("testClass")) project.property("testClass") as String else ""

        if (testClass.isNotBlank()) {
            project.exec {
                commandLine(
                    "adb",
                    "shell",
                    "am",
                    "instrument",
                    "-w",
                    "-e",
                    "class",
                    testClass,
                    "com.oopsipushedtomain.test/androidx.test.runner.AndroidJUnitRunner"
                )
            }
        } else {
            throw GradleException("No test class specified. Use -PtestClass to specify one.")
        }
    }
}
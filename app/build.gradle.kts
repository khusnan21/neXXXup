import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

fun getGitCommitHash(): String {
    return try {
        val headFile = file("${project.rootDir}/.git/HEAD")

        // Read the commit hash from .git/HEAD
        if (headFile.exists()) {
            val headContent = headFile.readText().trim()
            if (headContent.startsWith("ref:")) {
                val refPath = headContent.substring(5) // e.g., refs/heads/main
                val commitFile = file("${project.rootDir}/.git/$refPath")
                if (commitFile.exists()) commitFile.readText().trim() else ""
            } else headContent // If it's a detached HEAD (commit hash directly)
        } else {
            "" // If .git/HEAD doesn't exist
        }.take(7) // Return the short commit hash
    } catch (_: Throwable) {
        "" // Just return an empty string if any exception occurs
    }
}

android {
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // Looks like google likes to add metadata only they can read https://gitlab.com/IzzyOnDroid/repo/-/work_items/491
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    viewBinding {
        enable = true
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("unan.keystore")
            storePassword = "galur1234"
            keyAlias = "unan"
            keyPassword = "galur1234"
        }
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 68
        versionName = "4.7.0"

        resValue("string", "commit_hash", getGitCommitHash())

        manifestPlaceholders["target_sdk_version"] = libs.versions.targetSdk.get()

        // Reads local.properties
        val localProperties = gradleLocalProperties(rootDir, project.providers)

        buildConfigField(
            "long",
            "BUILD_DATE",
            "${System.currentTimeMillis()}"
        )
        buildConfigField(
            "String",
            "SIMKL_CLIENT_ID",
            "\"" + (System.getenv("SIMKL_CLIENT_ID") ?: localProperties["simkl.id"]) + "\""
        )
        buildConfigField(
            "String",
            "SIMKL_CLIENT_SECRET",
            "\"" + (System.getenv("SIMKL_CLIENT_SECRET") ?: localProperties["simkl.secret"]) + "\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        buildConfig = true
        resValues = true
    }

    packaging {
        jniLibs {
            // Enables legacy JNI packaging to reduce APK size (similar to builds before minSdk 23).
            // Note: This may increase app startup time slightly.
            useLegacyPackaging = true
        }
    }

    namespace = "com.lagradost.cloudstream3"
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.core)
    implementation(libs.junit.ktx)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Android Core & Lifecycle
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)

    // Design & UI
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Coil Image Loading
    implementation(libs.bundles.coil)

    // Media 3 (ExoPlayer)
    implementation(libs.bundles.media3)
    implementation(libs.video)

    // FFmpeg Decoding
    implementation(libs.bundles.nextlib)

    // PlayBack
    implementation(libs.colorpicker) // Subtitle Color Picker
    implementation(libs.newpipeextractor) // For Trailers
    implementation(libs.juniversalchardet) // Subtitle Decoding

    // UI Stuff
    implementation(libs.shimmer) // Shimmering Effect (Loading Skeleton)
    implementation(libs.palette.ktx) // Palette for Images -> Colors
    implementation(libs.tvprovider)
    implementation(libs.overlappingpanels) // Gestures
    implementation(libs.biometric) // Fingerprint Authentication
    implementation(libs.previewseekbar.media3) // SeekBar Preview
    implementation(libs.qrcode.kotlin) // QR Code for PIN Auth on TV

    // Extensions & Other Libs
    implementation(libs.jsoup) // HTML Parser
    implementation(libs.rhino) // Run JavaScript
    implementation(libs.fuzzywuzzy) // Library/Ext Searching with Levenshtein Distance
    implementation(libs.safefile) // To Prevent the URI File Fu*kery
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio) // NIO Flavor Needed for NewPipeExtractor
    implementation(libs.conscrypt.android) // To Fix SSL Fu*kery on Android 9
    implementation(libs.jackson.module.kotlin) // JSON Parser
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation(libs.zipline)

    // Torrent Support
    implementation(libs.torrentserver)

    // Downloading & Networking
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp) // HTTP Lib

    implementation(project(":library"))
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.directories) // Full Sources
}

tasks.register<Copy>("copyJar") {
    dependsOn("build", ":library:jvmJar")
    from(
        "build/intermediates/compile_app_classes_jar/stableDebug/bundleStableDebugClassesToCompileJar",
        "../library/build/libs"
    )
    into("build/app-classes")
    include("classes.jar", "library-jvm*.jar")
    // Remove the version
    rename("library-jvm.*.jar", "library-jvm.jar")
}

// Merge the app classes and the library classes into classes.jar
tasks.register<Jar>("makeJar") {
    // Duplicates cause hard to catch errors, better to fail at compile time.
    duplicatesStrategy = DuplicatesStrategy.FAIL
    dependsOn(tasks.getByName("copyJar"))
    from(
        zipTree("build/app-classes/classes.jar"),
        zipTree("build/app-classes/library-jvm.jar")
    )
    destinationDirectory.set(layout.buildDirectory)
    archiveBaseName = "classes"
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(javaTarget)
        jvmDefault.set(JvmDefaultMode.ENABLE)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
        optIn.addAll(
            "com.lagradost.cloudstream3.InternalAPI",
            "com.lagradost.cloudstream3.Prerelease",
        )
    }
}

dokka {
    moduleName = "App"
    dokkaSourceSets {
        main {
            analysisPlatform = KotlinPlatform.JVM
            documentedVisibilities(
                VisibilityModifier.Public,
                VisibilityModifier.Protected
            )

            sourceLink {
                localDirectory = file("..")
                remoteUrl("https://github.com/recloudstream/cloudstream/tree/master")
                remoteLineSuffix = "#L"
            }
        }
    }
}

tasks.register<Delete>("cleanBuildOutputs") {
    delete(rootProject.file("build-outputs"))
    delete(rootProject.file(".build-outputs"))
    delete(layout.buildDirectory.dir("outputs/apk/prerelease"))
}

val buildOutputDir = rootProject.file("build-outputs")
val dotBuildOutputDir = rootProject.file(".build-outputs")
val apkSrcDir = layout.buildDirectory.dir("outputs/apk/debug")

val rootFileDest = rootProject.file("app-debug.apk")

tasks.register("copyApksToBuildOutputs") {
    dependsOn("cleanBuildOutputs")
    dependsOn("packageDebug")
    val outputsDir = buildOutputDir
    val dotOutputsDir = dotBuildOutputDir
    val srcFile = File(apkSrcDir.get().asFile, "app-debug.apk")
    val destFile = File(outputsDir, "app-debug.apk")
    val dotDestFile = File(dotOutputsDir, "app-debug.apk")
    doLast {
        if (!outputsDir.exists()) outputsDir.mkdirs()
        if (!dotOutputsDir.exists()) dotOutputsDir.mkdirs()
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, true)
            srcFile.copyTo(dotDestFile, true)
        }
    }
}

tasks.register("copyApkToRoot") {
    dependsOn("copyApksToBuildOutputs")
    val srcFile = File(dotBuildOutputDir, "app-debug.apk")
    val destFile = rootFileDest
    doLast {
        if (destFile.exists()) {
            destFile.delete()
        }
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, true)
        }
    }
}

tasks.configureEach {
    if (name.startsWith("assemble")) {
        finalizedBy("copyApkToRoot")
    }
}


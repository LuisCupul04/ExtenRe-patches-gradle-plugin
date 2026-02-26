import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.binary.compatibility.validator)
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = "com.extenre"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.android.application)
    implementation(libs.binary.compatibility.validator)
    implementation(libs.guava)
    implementation(libs.kotlin)
    implementation(libs.kotlin.android)

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

gradlePlugin {
    plugins {
        create("patchesSettingsPlugin") {
            id = "com.extenre.patches"
            implementationClass = "com.extenre.patches.gradle.SettingsPlugin"
            version = version
            description = "Plugin to configure a ExtenRe Patches project."
            displayName = "ExtenRe Patches Gradle settings plugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/LuisCupul04/ExtenRe-patches-gradle-plugin")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(System.getenv("GITHUB_ACTOR")).get()
                password = providers.gradleProperty("gpr.key").orElse(System.getenv("GITHUB_TOKEN")).get()
            }
        }
    }
}

signing {
    val gpgKey = System.getenv("GPG_KEY") ?: return@signing
    val gpgPassphrase = System.getenv("GPG_PASSPHRASE") ?: return@signing
    useInMemoryPgpKeys(gpgKey, gpgPassphrase)
    sign(publishing.publications)
}

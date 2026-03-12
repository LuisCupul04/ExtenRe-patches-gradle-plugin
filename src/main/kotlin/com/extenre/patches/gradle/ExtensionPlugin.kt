/*
 * Copyright (C) 2022 ReVanced LLC
 * Copyright (C) 2022 inotia00
 * Copyright (C) 2026 LuisCupul04
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */
package com.extenre.patches.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Suppress("unused")
abstract class ExtensionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("extension", ExtensionExtension::class.java)

        // Verificar que el plugin de Android esté aplicado (opcional, solo para advertencia)
        if (!project.pluginManager.hasPlugin("com.android.library")) {
            project.logger.warn("The Android library plugin is not applied in this module. The syncExtension task may not work.")
        }

        project.configureArtifactSharing(extension)
        project.configureAndroid()
    }

    private fun Project.configureArtifactSharing(extension: ExtensionExtension) {
        val syncExtensionTask = tasks.register("syncExtension", Sync::class.java) { syncTask ->
            syncTask.group = "extenre"
            syncTask.description = "Copies the extension dex file to the build directory."

            syncTask.dependsOn("assembleRelease")

            val apkFile = layout.buildDirectory.dir("outputs/apk/release").map { dir ->
                dir.asFile.listFiles { _, name -> name.endsWith(".apk") }?.firstOrNull()
                    ?: error("No APK found in release output")
            }

            syncTask.from(project.zipTree(apkFile)) {
                it.include("classes.dex")
            }

            syncTask.into(
                layout.buildDirectory.zip(extension.name) { buildDir, extensionName ->
                    buildDir.dir("extenre/${Path(extensionName).parent.pathString}")
                }
            )

            syncTask.rename { "${Path(extension.name.get()).fileName}" }
        }

        configurations.consumable("extensionConfiguration").also { configuration ->
            artifacts.add(
                configuration.name,
                layout.buildDirectory.dir("extenre"),
            ) { artifact -> artifact.builtBy(syncExtensionTask) }
        }
    }

    private fun Project.configureAndroid() {
        // No aplicamos el plugin de Android aquí, solo configuramos si ya está aplicado
        afterEvaluate {
            extensions.findByType(LibraryExtension::class.java)?.apply {
                compileSdk = 35
                defaultConfig {
                    minSdk = 24
                    multiDexEnabled = false
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
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }

            extensions.findByType(KotlinAndroidProjectExtension::class.java)?.compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
}
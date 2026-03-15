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
    // Registrar la tarea que genera el DEX a partir del AAR
    val generateDexTask = tasks.register("generateExtensionDex", Sync::class.java) { syncTask ->
        syncTask.group = "extenre"
        syncTask.description = "Generates DEX from the extension AAR."

        // Depender de la tarea que ensambla el AAR
        syncTask.dependsOn("bundleReleaseAar")

        // Obtener el archivo AAR generado
        val aarFile = layout.buildDirectory.file("outputs/aar/${name}-release.aar").get().asFile

        // Directorio temporal para extraer el contenido del AAR
        val extractDir = layout.buildDirectory.dir("tmp/extractAar").get().asFile
        syncTask.doFirst {
            extractDir.deleteRecursively()
            extractDir.mkdirs()
            // Extraer el AAR (es un ZIP)
            project.copy {
                it.from(project.zipTree(aarFile))
                it.into(extractDir)
            }
        }

        // Ruta del classes.jar extraído
        val classesJar = extractDir.resolve("classes.jar")

        // Directorio de salida para el DEX
        val dexOutputDir = layout.buildDirectory.dir("extenre/dex").get().asFile
        syncTask.doFirst {
            dexOutputDir.mkdirs()
            // Ejecutar D8 sobre el JAR para generar DEX
            com.android.tools.r8.D8Command.builder()
                .addProgramFiles(classesJar.toPath())
                .setMode(com.android.tools.r8.CompilationMode.RELEASE)
                .setOutput(dexOutputDir.toPath(), com.android.tools.r8.OutputMode.DexIndexed)
                .build()
                .let(com.android.tools.r8.D8::run)
        }

        // Incluir el DEX generado en la sincronización
        syncTask.from(dexOutputDir) {
            it.include("*.dex")
        }

        // Directorio final donde se copiará el DEX (estructura original)
        val finalDir = layout.buildDirectory.zip(extension.name) { buildDir, extensionName ->
            buildDir.dir("extenre/${Path(extensionName).parent.pathString}")
        }
        syncTask.into(finalDir)

        // Renombrar el DEX al nombre esperado (ej: shared.dex)
        syncTask.rename { "${Path(extension.name.get()).fileName}" }
    }

    // Configurar el artefacto consumible
    configurations.consumable("extensionConfiguration").also { configuration ->
        artifacts.add(
            configuration.name,
            layout.buildDirectory.dir("extenre"),
        ) { artifact -> artifact.builtBy(generateDexTask) }
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
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            extensions.findByType(KotlinAndroidProjectExtension::class.java)?.compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
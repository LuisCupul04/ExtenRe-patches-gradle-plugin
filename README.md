<p align="center">
  <picture>
    <source
      width="350px"
      media="(prefers-color-scheme: dark)"
      srcset="Logo/ExtenRe-Red.svg">
    <img src="Logo/ExtenRe-Red-Light.svg" alt="ExtenRe Patcher Logo" width="350px">
  </picture>
</p>

# 🐘 ExtenRe Patches Gradle plugin

![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/LuisCupul04/ExtenRe-patches-gradle-plugin/release.yml)
![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)

A Gradle plugin for ExtenRe Patches projects.

## ❓ About

ExtenRe Patches Gradle plugin configures a project to develop ExtenRe Patches.

For that, the plugin provides:

- The [settings plugin](src/main/kotlin/com/extenre/patches/gradle/SettingsPlugin.kt):
Applied to the `settings.gradle.kts` file, configures the project repositories and subprojects
- The [patches plugin](src/main/kotlin/com/extenre/patches/gradle/PatchesPlugin.kt):
Applied to the patches subproject by the settings plugin
- The [extension plugin](src/main/kotlin/com/extenre/patches/gradle/ExtensionPlugin.kt):
Applied to extension subprojects by the settings plugin

> [!CAUTION]
> This plugin is not stable yet and likely to change due to lacking experience with Gradle plugins.  
> If you have experience with Gradle plugins and can help improve this plugin.

## 📜 Licence

ExtenRe Patches Gradle plugin is licensed under the GPLv3 license.
Please see the [license file](LICENSE) for more information. [tl;dr](https://www.tldrlegal.com/license/gnu-general-public-license-v3-gpl-3) you may copy, distribute and modify
ExtenRe Patches Gradle plugin as long as you track changes/dates in source files.
Any modifications to ExtenRe Patches Gradle plugin must also be made available under the GPL,
along with build & install instructions.

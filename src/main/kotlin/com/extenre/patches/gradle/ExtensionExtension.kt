/*
 * Copyright (C) 2026 LuisCupul04
 * Copyright (C) 2022 inotia00
 * Copyright (C) 2022 ReVanced LLC
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.extenre.patches.gradle

import org.gradle.api.provider.Property

abstract class ExtensionExtension {
    /**
     * The name of the extension.
     *
     * The name is the full resource path of the extension in the final patches file.
     * Example: `extensions/extension.rve`.
     */
    abstract val name: Property<String>
}

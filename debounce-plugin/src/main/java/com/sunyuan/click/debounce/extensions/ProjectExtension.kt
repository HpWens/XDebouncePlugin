package com.sunyuan.click.debounce.extensions

import com.android.build.api.variant.AndroidComponentsExtension
import com.sunyuan.click.debounce.EXTENSION_NAME
import com.sunyuan.click.debounce.config.DebounceExtension
import org.gradle.api.Project
import java.io.File

/**
 * @author sy007
 * @date 2022/08/31
 * @description
 */

private const val DEBOUNCE_ENABLE = "debounceEnable"

fun Project.getReportFile(dirName: String, fileName: String): File {
    // return project.buildDir.file("reports", "debounce-plugin", dirName, fileName)
    return layout.buildDirectory.file("reports/debounce-plugin/$dirName/$fileName").get().asFile
}

val Project.enablePlugin: Boolean
    get() = if (project.hasProperty(DEBOUNCE_ENABLE)) {
        project.properties[DEBOUNCE_ENABLE].toString().toBoolean()
    } else {
        true
    }

fun Project.getBootClasspath(): List<File> {
    val components = extensions.findByType(AndroidComponentsExtension::class.java)
    return components?.sdkComponents?.bootClasspath?.get()?.map { it.asFile } ?: emptyList()
}

val Project.debounceEx: DebounceExtension
    get() = extensions.findByName(EXTENSION_NAME) as DebounceExtension




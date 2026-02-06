@file:Suppress("DEPRECATION")

package com.sunyuan.click.debounce

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.sunyuan.click.debounce.config.DebounceExtension
import com.sunyuan.click.debounce.extensions.debounceEx
import com.sunyuan.click.debounce.extensions.enablePlugin
import com.sunyuan.click.debounce.extensions.getReportFile
import com.sunyuan.click.debounce.task.ModifyClassesTask
import com.sunyuan.click.debounce.utils.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * author : Sy007
 * date   : 2020/11/28
 * desc   : Plugin入口
 * version: 1.0
 */
internal const val EXTENSION_NAME = "debounce"

class DebouncePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.findByName("android")
            ?: throw GradleException("$project is not an Android project")

        LogUtil.init(project.logger)

        val debounceEx = project.extensions.create(
            EXTENSION_NAME,
            DebounceExtension::class.java,
            project.objects
        )

        if (!project.enablePlugin) {
            LogUtil.warn("debounce-plugin is off.")
            return
        }

        LogUtil.warn("debounce-plugin is on.")

        when {
            VersionUtil.V7_4 -> {
                val androidComponents =
                    project.extensions.getByType(AndroidComponentsExtension::class.java)
                androidComponents.onVariants { variant ->
                    val taskProvider = project.tasks.register(
                        "${variant.name}DebounceModifyClasses",
                        ModifyClassesTask::class.java
                    )
                    variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                        .use(taskProvider)
                        .toTransform(
                            ScopedArtifact.CLASSES,
                            ModifyClassesTask::allJars,
                            ModifyClassesTask::allDirectories,
                            ModifyClassesTask::output
                        )
                }
            }

            else -> {
//                project.getAndroid<BaseExtension>()
//                    .registerTransform(DebounceTransform(project, debounceEx))
            }

        }
        project.debounceEx.init()
        val androidComponents =
            project.extensions.findByType(AndroidComponentsExtension::class.java)
        androidComponents?.onVariants { variant ->
            project.afterEvaluate {
                val task = when {
                    VersionUtil.V7_4 -> {
                        // 筛选出项目中所有类型为 ModifyClassesTask 的任务。
                        project.tasks.withType(ModifyClassesTask::class.java).find {
                            it.name.contains(variant.name)
                        }
                    }

                    else -> {

//                    val transform = findLastClassesTransform()
//                    project.tasks.withType(TransformTask::class.java).find { transformTask ->
//                        transformTask.name.endsWith(variant.name.capitalize()) && transformTask.transform == transform
//                    }

                        null
                    }
                }

                task?.doLast {
                    // 检查用户是否启用了报告生成功能。如果未启用，则直接退出监听器，不再执行后续逻辑。
                    if (!debounceEx.generateReport.get()) {
                        return@doLast
                    }
                    dump(project, it.name)
                }
            }
        }
    }


    private fun dump(project: Project, dirName: String) {
        val file = project.getReportFile(
            dirName, "modified-method-list.html"
        )
        HtmlReportUtil().dump(file)
    }
}







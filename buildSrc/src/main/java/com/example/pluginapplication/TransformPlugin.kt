package com.example.pluginapplication

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.example.pluginapplication.transform.TestAsmTransform
import com.example.pluginapplication.transform.TestJavassistTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by Fuzhicheng on 2022/3/25
 */
class TransformPlugin :Plugin<Project> {
    override fun apply(target: Project) {
        target.subprojects { subProject ->
            subProject.afterEvaluate {
                val baseExtension = it.extensions.findByType(AppExtension::class.java) ?: it.extensions.findByType(
                    LibraryExtension::class.java)
                baseExtension?.registerTransform(TestJavassistTransform(baseExtension))
                baseExtension?.registerTransform(TestAsmTransform())
            }
        }

//        val baseExtension = target.extensions.findByType(AppExtension::class.java) ?: target.extensions.findByType(
//            LibraryExtension::class.java)
//        baseExtension?.registerTransform(TestAsmTransform())
    }
}
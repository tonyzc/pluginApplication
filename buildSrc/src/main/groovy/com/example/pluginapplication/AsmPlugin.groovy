package com.example.pluginapplication

import com.android.build.gradle.AppExtension
//import com.example.pluginapplication.transform.TestAsmTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsmPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        System.out.println("AsmPlugin 执行")
        project.task('secondTask'){
            doFirst {
                println 'SecondPlugin in Groovy'
            }
        }

//        project.getExtensions().findByType(AppExtension.class).registerTransform(new TestAsmTransform())

        project.android.applicationVariants.all{
            variant ->
                variant.outputs.all{
                    outputFileName = "${variant.name}-${variant.versionName}.apk"
                }
        }


    }
}

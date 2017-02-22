package com.brucesharpe.gradle.hibeaver

import com.android.build.gradle.AppExtension
import com.brucesharpe.gradle.hibeaver.utils.DataHelper
import com.brucesharpe.gradle.hibeaver.utils.Log
import org.gradle.api.Plugin
import org.gradle.api.Project

class HiBeaverPluginImpl implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println ":applied HiBeaver"
        android = project.extensions.getByType(AppExtension)
        project.extensions.create('hiBeaver', HiBeaverParams)
        registerTransform(project)
        project.afterEvaluate {
            Log.setQuiet(project.hiBeaver.keepQuiet)
//            learnAbout(project)
            applyInject(project)
            if (project.hiBeaver.watchTimeConsume) {
                Log.info "time consumed enabled"
                project.gradle.addListener(new TimeListener())
            } else {
                Log.info "time consumed patch disabled"
            }
        }
    }

    private void learnAbout(Project project) {
//        project.configurations.all {
//            c ->
//                Log.learn "config $c.name"
//                Log.learn "$c.files"
//                Log.learn "$c.dependencies"
//        }
        Log.learn "applicationVariants ${android.applicationVariants}"
        Log.learn "applicationVariants Class ${android.applicationVariants.getClass()}"
        android.applicationVariants.all {
            variant ->
                variant.outputs.each { output ->
                    Log.learn "applicationVariants variant${variant.name}"
                    Log.learn "variant ${variant.name}"
                    Log.learn "${output.outputFile.absolutePath}"
                    Log.learn "${output.assemble}"
                    Log.learn "${output.dirName}"
                    Log.learn "${output.assemble}"
                    Log.learn "${output.processManifest}"
                    Log.learn "${output.baseName}"
                    Log.learn "${output.splitFolder.absolutePath}"
                }
        }
    }
    def static AppExtension android;

    def static registerTransform(Project project) {
        def isApp = project.plugins.hasPlugin("com.android.application")
        if (isApp) {
//            Log.info "\nregisterTransform"
            def transform = new InjectTransform(project)
            android.registerTransform(transform)
        }
    }

    static void applyInject(Project project) {
        //如果存在hash.txt,解析并存储
        //建立文件夹
        File myDir = new File(project.buildDir, "HiBeaver")
        if (!myDir.exists()) {
            myDir.mkdir()
        }
        DataHelper.ext.patchDir = myDir
        android.sourceSets.all {
            ss ->
                Log.learn "sourceSets ${ss.name}"
                Log.learn "${ss.compileConfigurationName}"
                Log.learn "${ss.jniLibs}"
                Log.learn "${ss.assets}"
                Log.learn "${ss.manifest.srcFile.absolutePath}"
                Log.learn "${ss.manifest.name}"
                Log.learn "${ss.jni.sourceFiles.files}"
        }
    }
}

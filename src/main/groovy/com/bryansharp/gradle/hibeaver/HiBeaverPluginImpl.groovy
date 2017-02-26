package com.bryansharp.gradle.hibeaver

import com.android.build.gradle.BaseExtension
import com.bryansharp.gradle.hibeaver.utils.DataHelper
import com.bryansharp.gradle.hibeaver.utils.Log
import org.gradle.api.Plugin
import org.gradle.api.Project

class HiBeaverPluginImpl implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println ":applied HiBeaver"
        project.extensions.create('hiBeaver', HiBeaverParams)
        registerTransform(project)
        initDir(project);
        project.afterEvaluate {
            Log.setQuiet(project.hiBeaver.keepQuiet);
            Log.setShowHelp(project.hiBeaver.showHelp);
            Log.logHelp();
            if (project.hiBeaver.watchTimeConsume) {
                Log.info "watchTimeConsume enabled"
                project.gradle.addListener(new TimeListener())
            } else {
                Log.info "watchTimeConsume disabled"
            }
        }
    }

    def static registerTransform(Project project) {
//        def isApp = project.plugins.hasPlugin("com.android.application")
        BaseExtension android = project.extensions.getByType(BaseExtension)
        InjectTransform transform = new InjectTransform(project)
        android.registerTransform(transform)
    }

    static void initDir(Project project) {
        File hiBeaverDir = new File(project.buildDir, "HiBeaver")
        if (!hiBeaverDir.exists()) {
            hiBeaverDir.mkdir()
        }
        DataHelper.ext.hiBeaverDir = hiBeaverDir
    }
}

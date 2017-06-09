package com.bryansharp.gradle.hibeaver

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.bryansharp.gradle.hibeaver.utils.DataHelper
import com.bryansharp.gradle.hibeaver.utils.Log
import com.bryansharp.gradle.hibeaver.utils.ModifyFiles
import com.bryansharp.gradle.hibeaver.utils.Util
import org.gradle.api.Plugin
import org.gradle.api.Project

class HiBeaverPluginImpl implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println ":applied HiBeaver"
        project.extensions.create('hiBeaver', HiBeaverParams)
        Util.setProject(project)
        registerTransform(project)
        initDir(project);
        project.afterEvaluate {
            Log.setQuiet(project.hiBeaver.keepQuiet);
            Log.setShowHelp(project.hiBeaver.showHelp);
            Log.logHelp();
            Map<String, Map<String, Object>> taskMap = project.hiBeaver.modifyTasks;
            if (taskMap != null && taskMap.size() > 0) {
                generateTasks(project, taskMap);
            }
            Map<String, Map<String, Object>> changeNameMap = project.hiBeaver.changeClassName;
            if (changeNameMap != null && changeNameMap.size() > 0) {
                generateChangeNameTasks(project, changeNameMap);
            }
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
        if (android instanceof LibraryExtension){
            DataHelper.ext.projectType = DataHelper.TYPE_LIB;
        } else if(android instanceof AppExtension){
            DataHelper.ext.projectType = DataHelper.TYPE_APP;
        } else {
            DataHelper.ext.projectType = -1
        }
        InjectTransform transform = new InjectTransform()
        android.registerTransform(transform)
    }

    static void initDir(Project project) {
        File hiBeaverDir = new File(project.buildDir, "HiBeaver")
        if (!hiBeaverDir.exists()) {
            hiBeaverDir.mkdir()
        }
        File tempDir = new File(hiBeaverDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        DataHelper.ext.hiBeaverDir = hiBeaverDir
        DataHelper.ext.hiBeaverTempDir = tempDir
    }

    def static generateTasks(Project project, Map<String, Map<String, Object>> taskMap) {
        project.task("hibeaverModifyFiles") << {
            ModifyFiles.modify(taskMap)
        }
    }
    def static generateChangeNameTasks(Project project, Map<String, Map<String, Object>> changeNameMap) {
        project.task("hibeaverModifyFilesChangeName") << {
            ModifyFiles.changeName(changeNameMap)
        }
    }
}

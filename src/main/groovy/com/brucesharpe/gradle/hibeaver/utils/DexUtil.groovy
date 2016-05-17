package com.brucesharpe.gradle.hibeaver.utils

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

public class DexUtil {
    /**
     * 创建patch的dex补丁文件的具体逻辑
     * @param project
     * @param classDir
     * @return
     */
    public static dex(Project project, File classDir) {
        if (classDir) {
            def sdkDir
            Properties properties = new Properties()
            File localProps = project.rootProject.file("local.properties")
            if (localProps.exists()) {
                properties.load(localProps.newDataInputStream())
                sdkDir = properties.getProperty("sdk.dir")
            } else {
                sdkDir = System.getenv("ANDROID_HOME")
            }
            if (sdkDir) {
                def cmdExt = Os.isFamily(Os.FAMILY_WINDOWS) ? '.bat' : ''
                def stdout = new ByteArrayOutputStream()
                project.exec {
                    commandLine "${sdkDir}${File.separator}build-tools${File.separator}${project.android.buildToolsVersion}${File.separator}dx${cmdExt}",
                            '--dex',
                            "--output=${new File(DataHelper.ext.patchOutputDir, "patch.jar").absolutePath}",
                            "${classDir.absolutePath}"
                    standardOutput = stdout
                }
                def error = stdout.toString().trim()
                if (error) {
                    println "dex error:" + error
                }
            } else {
                throw new InvalidUserDataException('$ANDROID_HOME is not defined')
            }
        }
    }
}
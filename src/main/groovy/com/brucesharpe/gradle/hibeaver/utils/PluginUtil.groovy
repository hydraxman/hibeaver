package com.brucesharpe.gradle.hibeaver.utils;

/**
 * Created by MrBu(bsp0911932@163.com) on 2016/5/14.
 *
 * @author bushaopeng
 *         Project: HiBeaverPlugin
 *         introduction:
 */
public class PluginUtil {
    def static DIVIDER = '=='
    public static File createEmptyDir(File myDir, String name) {
        def file = new File(myDir, name)
        if (file.exists()) {
            file.delete()
        }
        file.mkdir()
        file
    }
    public static File touchFile(File dir, String path) {
        def file = new File("${dir}/${path}")
        file.getParentFile().mkdirs()
        file
    }
    public static void parseClassHash(String path) {
        def file = new File(path)
        Log.info "提供的hash文件位置:${file.absolutePath}"
        if(file.exists()){
            def value
            DataHelper.ext.oldClassHashMap=[:]
            file.eachLine('utf-8'){ line, lineNum->
                if(line&&lineNum>1){
                    value=line.split("${DIVIDER}")
                    DataHelper.ext.oldClassHashMap.put(value[0],value[1])
                }
            }
        }else{
            throw new Exception('没有提供hash文件，无法继续')
        }
    }
}

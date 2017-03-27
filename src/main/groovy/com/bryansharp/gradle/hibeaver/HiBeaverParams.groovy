package com.bryansharp.gradle.hibeaver

public class HiBeaverParams {
    String hiBeaverModifyName = ''
    boolean enableModify = true
    boolean watchTimeConsume = false
    boolean keepQuiet = false
    boolean showHelp = true
    Map<String, Object> modifyMatchMaps = [:]
    Map<String, Map<String, Object>> modifyTasks = [:]


    String getHiBeaverModifyName() {
        return hiBeaverModifyName
    }

    boolean getEnableModify() {
        return enableModify
    }

    boolean getWatchTimeConsume() {
        return watchTimeConsume
    }

    boolean getKeepQuiet() {
        return keepQuiet
    }

    boolean getShowHelp() {
        return showHelp
    }

    Map<String, Object> getModifyMatchMaps() {
        return modifyMatchMaps
    }

    Map<String, Map<String, Object>> getModifyTasks() {
        return modifyTasks
    }
}
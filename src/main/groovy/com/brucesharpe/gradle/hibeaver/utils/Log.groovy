package com.brucesharpe.gradle.hibeaver.utils

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class Log {
    def static Logger logger= Logging.getLogger("myDefault")

    static void setQuiet(boolean quiet) {
        //此处如果不加@会导致循环调用
        Log.@quiet = quiet
    }
    def static boolean quiet=false
    def static boolean learnMode = false

    def static init(Logger log) {
        logger = log;
    }

    def static learn(Object msg) {
        if(quiet) return
        if (learnMode) {
            logger.error "learn:${msg}"
        }
    }

    def static info(Object msg) {
        if(quiet) return
        println "patch-log-info:${msg}"
    }
}
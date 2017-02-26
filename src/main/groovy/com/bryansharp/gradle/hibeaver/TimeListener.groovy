package com.bryansharp.gradle.hibeaver

import com.bryansharp.gradle.hibeaver.utils.Log
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock

class TimeListener implements TaskExecutionListener, BuildListener {
    private Clock clock
    private times = []
    private long totalTime

    @Override
    void beforeExecute(Task task) {
        Log.learn("beforeExecute")
        clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = clock.timeInMs
        times.add([ms, task.path])
        totalTime += ms;
        Log.info "${task.path} 花费时间 spend ${ms}ms"
    }

    @Override
    void buildFinished(BuildResult result) {
        println "Task spend time:${totalTime}ms"
        for (time in times) {
            if (time[0] >= 50) {
                printf "%7sms %s\n", time
            }
        }
    }

    @Override
    void buildStarted(Gradle gradle) {
        Log.learn("buildStarted")
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
        Log.learn("projectsEvaluated")
    }

    @Override
    void projectsLoaded(Gradle gradle) {
        Log.learn("projectsLoaded")
    }

    @Override
    void settingsEvaluated(Settings settings) {
        Log.learn("settingsEvaluated")
    }
}
package org.mym.netdiag

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.concurrent.thread

object NetworkDiagnosis {
    /**
     * If set to true, debug logs will be printed via [logger].
     */
    var debug: Boolean = false

    /**
     * Use this property to customize your logger implementations.
     */
    var logger = object : Logger{
        override fun debug(message: String) {
            Log.d(LOG_TAG, message)
        }

        override fun warn(message: String) {
            Log.d(LOG_TAG, message)
        }
    }

    /**
     * Use this property to define your own thread logic. If not set, every task will be executed in a new thread.
     */
    var executor: Executor = object : Executor {
        override fun doInBackground(action: () -> Unit) {
            thread(name = "NetworkDiagnosisThread") { action.invoke() }
        }

        override fun doInMainThread(action: () -> Unit) {
            Handler(Looper.getMainLooper()).post {
                action.invoke()
            }
        }
    }

    /**
     * Execute a diagnosis task.
     *
     * @param[task] Arbitrary instance of [Task].
     * @param[progressListener] Optional param to observe task progress. This listener will never be called if the task does not support progress listening.
     * @param[errorListener] Optional param to handle task errors. If not set, a task will silently failed.
     * @param[resultListener] Mandatory param to handle result.
     */
    @JvmStatic
    @JvmOverloads
    fun <T> execute(task: Task<T>,
                    progressListener: ProgressListener? = null,
                    errorListener: ErrorListener? = null,
                    resultListener: ResultListener<T>) {
        log4Debug("Enqueued task $task")
        executor.doInBackground {
            log4Debug("Started task $task")
            try {
                val result = task.run(progressListener)
                log4Debug("Task $task returned as: $result")
                executor.doInMainThread {
                    resultListener.invoke(result)
                }
            } catch (e: Exception) {
                log4Warn("Task $task throws an exception: $e")
                executor.doInMainThread {
                    errorListener?.invoke(e)
                }
            }
        }
    }

}

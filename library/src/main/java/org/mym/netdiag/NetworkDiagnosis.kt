package org.mym.netdiag

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
    var logger: Logger = {
        Log.v("NetworkDiagnosis", it)
    }

    /**
     * Use this property to define your own thread logic. If not set, every task will be executed in a new thread.
     */
    var executor: Executor = { thread(name = "NetworkDiagnosisThread") { it.invoke() } }

    internal fun log4Debug(content: String) {
        if (debug) {
            logger.invoke(content)
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
    fun <T : Result> execute(task: Task<T>,
                             progressListener: ProgressListener? = null,
                             errorListener: ErrorListener? = null,
                             resultListener: ResultListener<T>) {
        log4Debug("Enqueued task $task")
        executor.invoke {
            log4Debug("Started task $task")
            try {
                val result = task.run(progressListener)
                log4Debug("Task $task returned as: $result")
                resultListener.invoke(result)
            } catch (e: Exception) {
                log4Debug("Task $task throws an exception: $e")
                errorListener?.invoke(e)
            }
        }
    }

}

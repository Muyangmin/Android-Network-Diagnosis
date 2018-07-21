package org.mym.netdiag.api

/**
 * You can do extra things using this interface.
 *
 * **NOTE: This callback is not equals to progress(0).** *In fact, this callback will always be called by task framework,
 * but [ProgressListener] needs concrete task support.*
 *
 * @see TaskListener.onTaskStarted
 */
typealias StartListener = () -> Unit

/**
 * A [ProgressListener] is used to observe task progress. Not all tasks support this kind of observing -- it depends on task implementations.
 *
 * Param passed in can be trusted in range (0, 100).
 *
 * @see TaskListener.onTaskProgressChanged
 */
typealias ProgressListener = (progress: Int) -> Unit

/**
 * A [ResultListener] is used to handle result for task.
 *
 * @see TaskListener.onTaskFinished
 */
typealias ResultListener<T> = (T) -> Unit

/**
 * An [ErrorListener] is used to handle exceptions during task executing.
 *
 * @see TaskListener.onTaskError
 */
typealias ErrorListener = (e: Exception) -> Unit

/**
 * Full event support for task lifecycle. All methods will called in main thread.
 */
interface TaskListener<T> {
    /**
     * Called when task is actually started.
     */
    fun onTaskStarted()

    /**
     * Called when the progress of this task changed.
     */
    fun onTaskProgressChanged(progress: Int)

    /**
     * Called when the result of this task is returned normally.
     */
    fun onTaskFinished(result: T)

    /**
     * Called when the task throws an Exception.
     */
    fun onTaskError(e: Exception)
}

/**
 * Simple adapter for the [TaskListener]. You can pass actions in the constructor, or just override this class.
 */
open class SimpleTaskListener<T>(private val startListener: StartListener? = null,
                                 private val progressListener: ProgressListener? = null,
                                 private val resultListener: ResultListener<T>? = null,
                                 private val errorListener: ErrorListener? = null) : TaskListener<T> {
    override fun onTaskStarted() {
        startListener?.invoke()
    }

    override fun onTaskProgressChanged(progress: Int) {
        progressListener?.invoke(progress)
    }

    override fun onTaskFinished(result: T) {
        resultListener?.invoke(result)
    }

    override fun onTaskError(e: Exception) {
        errorListener?.invoke(e)
    }
}
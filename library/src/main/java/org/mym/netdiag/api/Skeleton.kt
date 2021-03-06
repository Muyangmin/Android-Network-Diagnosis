package org.mym.netdiag.api
import org.mym.netdiag.NetworkDiagnosis

interface Logger {
    /**
     * Debug information will be printed in this method.Those information is verbose for most apps, thus will not be called unless [NetworkDiagnosis.debug] is set to `true`.
     */
    fun debug(message: String)

    /**
     * Errors and internal exceptions will be printed in this method. These methods will always be printed.
     */
    fun warn(message: String)
}

interface Executor {
    /**
     * Execute [action] in a background thread.
     */
    fun doInBackground(action: () -> Unit)

    /**
     * Execute [action] in a main thread, i.e. UI thread.
     */
    fun doInMainThread(action: () -> Unit)
}

/**
 * A `Task` often represents a diagnosis command, e,g. `ping`.
 *
 * Tasks may need some parameters to initialize command (e.g. in constructor), and returns a specific type of result.
 *
 * Tasks may execute for long time, so some (NOT all) of them may notify progress, see [ProgressListener].
 *
 * If a task met an exception while executing, it will just throw it.
 *
 * @param[T] Represents a return value of a [Task] in normal case, i.e. no exceptions occurred during executing that task.
 * A concrete result class can has arbitrary properties and methods, just like any other classes.
 * Those properties and methods can be directly accessed (without type cast) in [ResultListener].
 * However, it is a convention that [toString] method should be overridden to return a readable string for this result.
 */
interface Task<T> {
    /**
     * Implementations should define concrete task logic here.
     *
     * The param listener has been wrapped so that it can be safely called on background threads.
     * *Tip: Scheduling logic and error handing is trivial for concrete tasks: they are handled by [NetworkDiagnosis].*
     */
    fun run(progressListener: ProgressListener? = null): T

    /**
     * If a task may be cancelled, define cancel logic here. Otherwise it may be empty.
     */
    fun cancel()
}

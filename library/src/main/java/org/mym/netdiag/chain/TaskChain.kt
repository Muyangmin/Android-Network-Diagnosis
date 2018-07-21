package org.mym.netdiag.chain

import org.mym.netdiag.*
import org.mym.netdiag.api.*

/**
 * This is a bonus class to support chained check, i.e. serially execute [Task]s.
 *
 * Every task added into the chain is executed one by one, and each task can abort the whole chain,
 * once the chain is aborted, tasks in the queue will never be executed.
 *
 * Sample usage:
 * ```Kotlin
 * val chain = TaskChain()
 * with(chain) {
 *   addTask(SampleTask(),/.../, decision= {it.flag})
 *   addTask(SampleTask(),/.../, decision= {it.value==-1})
 * }
 * chain.start()
 * ```
 */
class TaskChain {

    private var isAborted: Boolean = false

    private var actionList = mutableListOf<() -> Unit>()

    private var currentIndex = -1

    var abortCallback: (() -> Unit)? = null

    var finishCallback: (() -> Unit)? = null

    /**
     * Add a task into the chained call. Parameters are same as explained in [NetworkDiagnosis.execute], but you can call [abort] in resultListener or errorListener.
     *
     * Task added into the chain will not be executed until you call [start].
     */
    fun <T> addTask(task: Task<T>,
                    startListener: StartListener? = null,
                    progressListener: ProgressListener? = null,
                    errorListener: ErrorListener? = null,
                    resultListener: ResultListener<T>) {
        addTask(task, SimpleTaskListener(startListener, progressListener, resultListener, errorListener))
    }

    fun <T> addTask(task: Task<T>, taskListener: TaskListener<T>) {
        val wrapAction = {
            if (!isAborted) {
                NetworkDiagnosis.execute(task, object : SimpleTaskListener<T>() {
                    override fun onTaskStarted() {
                        taskListener.onTaskStarted()
                    }

                    override fun onTaskProgressChanged(progress: Int) {
                        taskListener.onTaskProgressChanged(progress)
                    }

                    override fun onTaskFinished(result: T) {
                        if (!isAborted) {
                            doNextAction()
                            taskListener.onTaskFinished(result)
                        }
                    }

                    override fun onTaskError(e: Exception) {
                        if (!isAborted) {
                            taskListener.onTaskError(e)
                            doNextAction()
                        }
                    }
                })
            }
        }
        actionList.add(wrapAction)
    }

    /**
     * Start this chain. You should never call [addTask] after called this method.
     */
    fun start() {
        currentIndex = -1
        doNextAction()
    }

    private fun doNextAction() {
        if (currentIndex + 1 < actionList.size) {
            currentIndex++
            actionList[currentIndex].invoke()
        } else {
            finishCallback?.invoke()
        }
    }

    /**
     * Abort this chain. Currently executing tasks cannot be cancelled in this version, but may do that in future.
     */
    fun abort() {
        isAborted = true
        abortCallback?.invoke()
    }

}

package org.mym.netdiag.chain

import org.mym.netdiag.*

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

    /**
     * Add a task into the chained call. Most parameters are same as explained in [NetworkDiagnosis.execute].
     *
     * @param[decision] determine whether the chain should be aborted after the result returned.
     * @param[errorDecision] determine whether the chain should be aborted if the task met a error.
     */
    fun <T> addTask(task: Task<T>,
                             progressListener: ProgressListener? = null,
                             errorListener: ErrorListener? = null,
                             resultListener: ResultListener<T>,
                             decision: (T) -> Boolean = { false },
                             errorDecision: ((e: Exception) -> Boolean) = { false }) {
        val wrapAction = {
            if (!isAborted) {
                NetworkDiagnosis.execute(task, progressListener, errorListener = {
                    if (errorDecision.invoke(it)) {
                        isAborted = true
                    }
                    errorListener?.invoke(it)
                }, resultListener = {
                    if (decision.invoke(it)) {
                        isAborted = true
                    }
                    resultListener.invoke(it)
                })
            }
        }
        actionList.add(wrapAction)
    }

    /**
     * Start this chain. You should never call [addTask] after called this method.
     */
    fun start() {
        actionList.forEach { it.invoke() }
    }

    /**
     * Abort this chain. Currently executing tasks cannot be cancelled in this version, but may do that in future.
     */
    fun abort() {
        isAborted = true
    }

}

package org.mym.netdiag.tasks

import org.mym.netdiag.api.ProgressListener
import org.mym.netdiag.api.Task

/**
 * This class is used to wrap simple actions for the task framework. Just pass the action into it.
 */
class SimpleTask<T>(private val action: () -> T) : Task<T> {

    override fun run(progressListener: ProgressListener?): T {
        return action.invoke()
    }

    override fun cancel() {
        //Does not support
    }
}
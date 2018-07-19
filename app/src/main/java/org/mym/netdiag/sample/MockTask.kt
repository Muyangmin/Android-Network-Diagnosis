package org.mym.netdiag.sample

import org.mym.netdiag.ProgressListener
import org.mym.netdiag.Result
import org.mym.netdiag.Task


class FakeResult : Result {

    fun specialValue() = 100

    override fun toString(): String = "Faked"
}

class FakeTask : Task<FakeResult> {
    override fun run(progressListener: ProgressListener?): FakeResult {
        progressListener?.invoke(20)
        progressListener?.invoke(80)
        return FakeResult()
    }

    override fun cancel() {
        //DO NOTHING
    }
}
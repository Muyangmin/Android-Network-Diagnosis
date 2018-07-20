package org.mym.netdiag.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import org.mym.kotlog.GlobalTagDecorator
import org.mym.kotlog.L
import org.mym.netdiag.Executor
import org.mym.netdiag.Logger
import org.mym.netdiag.NetworkDiagnosis
import org.mym.netdiag.tasks.NetworkInfoTask
import org.mym.netdiag.tasks.IpTask
import org.mym.netdiag.tasks.PingTask
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(L) {
            install()
            addDecorator(GlobalTagDecorator("Sample"))
        }

        with(NetworkDiagnosis) {
            debug = true
            logger = object : Logger {
                override fun debug(message: String) {
                    L.d(message)
                }

                override fun warn(message: String) {
                    L.w(message)
                }
            }
            val pool = Executors.newFixedThreadPool(5)
            executor = object : Executor {
                override fun doInBackground(action: () -> Unit) {
                    pool.submit(action)
                }

                override fun doInMainThread(action: () -> Unit) {
                    Handler(Looper.getMainLooper()).post {
                        action.invoke()
                    }
                }
            }
        }

        val tasks = arrayListOf(NetworkInfoTask(this), IpTask(this),
                PingTask("www.bbb.markup"))

        tasks.forEach { task ->
            NetworkDiagnosis.execute(task) {
                L.i("Task $task result: $it")
            }
        }
    }
}

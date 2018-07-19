package org.mym.netdiag.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.mym.kotlog.GlobalTagDecorator
import org.mym.kotlog.L
import org.mym.netdiag.NetworkDiagnosis
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
            logger = { L.d(msg = it) }
            val pool = Executors.newFixedThreadPool(5)
            executor = {
                pool.submit(it)
            }
        }

        NetworkDiagnosis.execute(FakeTask()) {
            L.i("FakeTask result: $it")
        }
    }
}

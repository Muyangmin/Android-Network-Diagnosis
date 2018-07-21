package org.mym.netdiag.tasks

import org.mym.netdiag.api.ProgressListener
import org.mym.netdiag.api.Task
import org.mym.netdiag.log4Debug
import org.mym.netdiag.log4Warn
import org.mym.netdiag.readStrFromRuntimeProcess

/**
 * This task is used to execute ping command to specified server, and resolve the text results into [PingResult].
 *
 * @param[target] The server you want to ping
 * @param[count] How many times should passed to ping command.
 */
class PingTask(private val target: String, private val count: Int = 4) : Task<PingResult> {

    private var nativeCommand: Process? = null

    override fun run(progressListener: ProgressListener?): PingResult {
        val pingResult = readStrFromRuntimeProcess("/system/bin/ping -c $count $target")
        return parsePingResult(pingResult)
    }

    private fun parsePingResult(resultStr: String): PingResult {
        val regex = Regex("(\\d+) packets transmitted, (\\d+) received, (\\d+)% packet loss, time (\\d+)ms[\\s\\n\\t]+rtt min/avg/max/mdev = ([\\d.]+)/([\\d.]+)/([\\d.]+)/([\\d.]+) ms")
        val matchResult = regex.findAll(resultStr).iterator()
        if (!matchResult.hasNext()) {
            log4Warn("Failed to match ping result, please issue this case via github.")
        } else {
            val groups = matchResult.next().groups
            if (groups.size != 9) {
                log4Warn("Failed to match regex groups, expected 9 but actually ${groups.size}")
            }

            val intValues = groups.filterNotNull().filterIndexed { index, _ ->
                index in 1..3
            }.map {
                it.value.toIntOrNull() ?: 0
            }
            val floatValues = groups.filterNotNull().filterIndexed { index, _ ->
                index in 5..8
            }.map {
                it.value.toFloatOrNull() ?: 0F
            }

            log4Debug("Parse ping result succeed.")
            return PingResult(intValues[0], intValues[1], intValues[2],
                    floatValues[0], floatValues[1], floatValues[2])

        }
        return PingResult(count, 0, 100, Float.NaN, Float.NaN, Float.NaN)
    }

    override fun cancel() {
        nativeCommand?.destroy()
    }
}

/**
 * @property[transmitted] How many packets was sent to server.
 * @property[received] How many packets received from server.
 * @property[loss] How many percents of packets lost.
 * @property[minRtt] The minimal rtt among the packets.
 * @property[avgRtt] The average rtt among the packets.
 * @property[maxRtt] The maximum rtt among the packets.
 */
data class PingResult(val transmitted: Int, val received: Int, val loss: Int,
                      val minRtt: Float, val avgRtt: Float, val maxRtt: Float)
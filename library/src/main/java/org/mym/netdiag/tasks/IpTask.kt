package org.mym.netdiag.tasks

import android.content.Context
import android.net.wifi.WifiManager
import org.mym.netdiag.ProgressListener
import org.mym.netdiag.Result
import org.mym.netdiag.Task
import org.mym.netdiag.readStrFromUrl

class IpTask(context: Context, private val server: String = SERVER_AKAMAI) : Task<Ip> {

    companion object {
        const val SERVER_IPIFY = "https://api.ipify.org/?format=text"
        const val SERVER_AKAMAI = "http://whatismyip.akamai.com"
    }

    private val appContext: Context = context.applicationContext

    override fun run(progressListener: ProgressListener?): Ip {
        //TODO consider query from server list, not a single server
        val publicIp = readStrFromUrl(server).orEmpty()
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val localIpInt = wifiManager?.dhcpInfo?.ipAddress ?: UNKNOWN_IP_INT
        val localIp = intIpToString(localIpInt)

        return Ip(publicIp, localIp)
    }

    override fun cancel() {
        //currently not supported
    }
}

data class Ip(val publicIp: String, val localIp: String) : Result

private const val UNKNOWN_IP_INT = -1
private const val UNKNOWN_IP_STR = "Unknown IP"

fun intIpToString(ipInt: Int): String {
    if (ipInt == UNKNOWN_IP_INT) {
        return UNKNOWN_IP_STR
    }
    return "${0xff and ipInt}.${0xff and (ipInt shr 8)}.${0xff and (ipInt shr 16)}.${0xff and (ipInt shr 24)}"
}
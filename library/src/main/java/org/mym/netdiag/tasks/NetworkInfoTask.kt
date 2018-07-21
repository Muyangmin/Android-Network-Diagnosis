package org.mym.netdiag.tasks

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import org.mym.netdiag.api.ProgressListener
import org.mym.netdiag.api.Task

/**
 * This task detect the connectivity and network type for current device.
 */
class NetworkInfoTask(context: Context) : Task<NetworkInfoResult> {
    private val appContext: Context = context.applicationContext

    override fun run(progressListener: ProgressListener?): NetworkInfoResult {
        val connectivity = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isConnected = connectivity?.activeNetworkInfo?.isConnected ?: false
        val isWifi = connectivity?.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        progressListener?.invoke(67)

        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val telephonyType = telephonyManager?.networkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN

        return NetworkInfoResult(isConnected, isWifi, telephonyType)
    }

    override fun cancel() {
        //Unsupported
    }
}

/**
 * @property isConnected whether any network is connected (determined by [ConnectivityManager]).
 * @property isWifi whether the current connected network (if exists) is Wi-Fi (determined by [ConnectivityManager]).
 * @property telephonyType the current telephony network type, maybe useful to predicate network speed. You can use [isFastMobileNetwork] utility method for that purpose.
 */
data class NetworkInfoResult(val isConnected: Boolean, val isWifi: Boolean, val telephonyType: Int)

/**
 * A Utility method to decide whether a mobile network type is *fast enough* for modern apps.
 */
fun isFastMobileNetwork(networkType: Int): Boolean {
    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_EVDO_0, // ~ 400-1000 kbps
        TelephonyManager.NETWORK_TYPE_EVDO_A, // ~ 600-1400 kbps
        TelephonyManager.NETWORK_TYPE_EVDO_B, // ~ 5 Mbps
        TelephonyManager.NETWORK_TYPE_HSPA, // ~ 700-1700 kbps
        TelephonyManager.NETWORK_TYPE_HSDPA, //~ 2-14 Mbps
        TelephonyManager.NETWORK_TYPE_HSUPA, // ~ 1-23 Mbps
        TelephonyManager.NETWORK_TYPE_HSPAP,// ~ 10-20 Mbps
        TelephonyManager.NETWORK_TYPE_UMTS, // ~ 400-7000 kbps
        TelephonyManager.NETWORK_TYPE_EHRPD, // ~ 1-2 Mbps
        TelephonyManager.NETWORK_TYPE_LTE // ~ 10+ Mbps
        -> true
        else -> false
    }
}
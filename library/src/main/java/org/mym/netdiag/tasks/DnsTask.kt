package org.mym.netdiag.tasks

import org.mym.netdiag.api.ProgressListener
import org.mym.netdiag.api.Task
import org.mym.netdiag.log4Debug
import org.mym.netdiag.log4Warn
import org.mym.netdiag.readStrFromRuntimeProcess
import org.mym.netdiag.readStrFromUrl

/**
 * Detect public & local dns.
 */
class DnsTask : Task<DnsServer> {
    override fun run(progressListener: ProgressListener?): DnsServer {
        val publicDns = detectPublicDns()
        val privateDns = detectPrivateDns()
        return DnsServer(publicDns, privateDns)
    }

    private fun detectPublicDns(): String {
        val queryServer = "http://ns.pbt.cloudxns.net/fast_tools/fetch_ldns_diag_client.php"
        val response = readStrFromUrl(queryServer).orEmpty().replace("\n", "")
        val matchResult = Regex("iframe src=\"(.+\\.php)").findAll(response).iterator()
        if (!matchResult.hasNext()) {
            log4Warn("Failed to resolve outer frame of dns response.")
        } else {
            val actualUrl = matchResult.next().groupValues[1]
            val actualResponse = readStrFromUrl(actualUrl).orEmpty().replace("\n", "")
            log4Debug(actualResponse)
            val dnsMatch = Regex("DNS.+>(\\d+\\.\\d+\\.\\d+\\.\\d+)").findAll(actualResponse).iterator()
            if (!dnsMatch.hasNext()) {
                log4Warn("Failed to resolve inner response.")
            } else {
                return dnsMatch.next().groupValues[1]
            }
        }
        return ""
    }

    private fun detectPrivateDns(): List<String> {
        val getProp = readStrFromRuntimeProcess("getprop")
        val dnsMatch = Regex("\\[net.dns\\d+].+\\[(\\d+\\.\\d+\\.\\d+\\.\\d+)]").findAll(getProp).toList()
        if (dnsMatch.isNotEmpty()) {
            return dnsMatch.map { it.groupValues[1] }
        }
        return emptyList()
    }

    override fun cancel() {

    }
}

data class DnsServer(val publicDns: String, val localDns: List<String>)
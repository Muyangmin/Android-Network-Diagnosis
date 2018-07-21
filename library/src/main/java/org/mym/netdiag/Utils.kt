package org.mym.netdiag

import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun readStrFromUrl(url: String, readTimeout: Int = 30 * 1000, connTimeout: Int = 30 * 1000): String? {
    val conn = (URL(url).openConnection()
            ?: throw IOException("Failed to open connection from url $url"))

    if (conn !is HttpURLConnection) {
        throw IOException("Unrecognized UrlConnection type $conn")
    }
    conn.readTimeout = readTimeout
    conn.connectTimeout = connTimeout
    if (conn.responseCode != HttpURLConnection.HTTP_OK || conn.contentLength > 1024 * 1024) {
        return null
    }

    val reader = InputStreamReader(conn.inputStream)
    val content = reader.readText()
    reader.close()
    return content
}

fun readStrFromRuntimeProcess(cmdStr: String): String {
    val command = Runtime.getRuntime().exec(cmdStr)
    val exitValue = command.waitFor()
    log4Debug("Process `$cmdStr` exited with exitValue $exitValue")
    val resultStr = InputStreamReader(command.inputStream).readText()
    val errorStr = InputStreamReader(command.errorStream).readText()
    return if (exitValue != 0) {
        log4Warn(errorStr)
        errorStr
    } else {
        log4Debug(resultStr)
        resultStr
    }
}

internal const val LOG_TAG = "NetworkDiagnosis"

internal fun log4Debug(message: String) {
    if (NetworkDiagnosis.debug) {
        NetworkDiagnosis.logger.debug(message)
    }
}

internal fun log4Warn(message: String) = NetworkDiagnosis.logger.warn(message)
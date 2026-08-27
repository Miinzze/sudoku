package com.sudokuai.app.data.update

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** Result of an update check against GitHub Releases. */
sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val version: String,
        val downloadUrl: String,
        val releaseNotes: String,
    ) : UpdateCheckResult()

    object UpToDate : UpdateCheckResult()

    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * The app's one deliberate, opt-in exception to being fully offline (see the comment on the
 * INTERNET permission in AndroidManifest.xml): only called when the user explicitly taps
 * "Nach Updates suchen" in Settings, never automatically. Queries the GitHub Releases API for
 * the newest published release, compares its tag against the installed version, and returns
 * either an update (with a direct APK download link, if the release has one attached, or a
 * fallback link to the release page) or a plain "up to date" / error result.
 *
 * This performs blocking network I/O — callers must run it on a background dispatcher.
 */
object UpdateChecker {
    private const val REPO_OWNER = "Miinzze"
    private const val REPO_NAME = "sudoku"
    private const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
    private const val TIMEOUT_MS = 8000

    fun checkForUpdate(currentVersionName: String): UpdateCheckResult {
        return try {
            val body = fetch(API_URL)
                ?: return UpdateCheckResult.Error("Keine Antwort vom Server.")
            val json = JSONObject(body)

            val tag = json.optString("tag_name").removePrefix("v").removePrefix("V")
            if (tag.isBlank()) return UpdateCheckResult.Error("Ungültige Antwort vom Server.")

            val releaseNotes = json.optString("body")
            val htmlUrl = json.optString("html_url")
            val apkUrl = findApkAssetUrl(json)
            val downloadUrl = apkUrl ?: htmlUrl
            if (downloadUrl.isBlank()) return UpdateCheckResult.Error("Kein Download-Link gefunden.")

            if (isNewerVersion(tag, currentVersionName)) {
                UpdateCheckResult.UpdateAvailable(tag, downloadUrl, releaseNotes)
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Unbekannter Fehler bei der Update-Prüfung.")
        }
    }

    private fun findApkAssetUrl(release: JSONObject): String? {
        val assets = release.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name")
            if (name.endsWith(".apk", ignoreCase = true)) {
                return asset.optString("browser_download_url").ifBlank { null }
            }
        }
        return null
    }

    private fun fetch(urlString: String): String? {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "SudokuAI-Android-App")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Compares dotted version numbers (e.g. "1.10.0" vs "1.9.0") component-wise as integers so
     * that "1.10.0" is correctly treated as newer than "1.9.0" (a plain string comparison would
     * get this wrong).
     */
    internal fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".", "-", "+").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".", "-", "+").mapNotNull { it.toIntOrNull() }
        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }
}

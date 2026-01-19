package service

import kotlinx.coroutines.*
import java.net.URL
import kotlinx.serialization.json.*

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/ranjeetchouhan/compose-code-viewer/releases/latest"
    const val CURRENT_VERSION = "1.0.1"
    
    data class UpdateInfo(
        val isUpdateAvailable: Boolean,
        val latestVersion: String,
        val downloadUrl: String
    )
    
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(GITHUB_API_URL).openConnection()
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = Json.parseToJsonElement(response).jsonObject
            
            val latestVersion = json["tag_name"]?.jsonPrimitive?.content ?: return@withContext null
            val downloadUrl = json["html_url"]?.jsonPrimitive?.content ?: ""
            
            val isNewer = compareVersions(latestVersion.removePrefix("v"), CURRENT_VERSION) > 0
            
            UpdateInfo(
                isUpdateAvailable = isNewer,
                latestVersion = latestVersion,
                downloadUrl = downloadUrl
            )
        } catch (e: Exception) {
            println("Update check failed: ${e.message}")
            null
        }
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrNull(i) ?: 0
            val p2 = parts2.getOrNull(i) ?: 0
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}

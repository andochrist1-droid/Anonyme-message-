package com.example.util

import android.os.Build
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

object SecurityUtils {

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun generateSessionToken(): String {
        return "sess_" + UUID.randomUUID().toString().replace("-", "")
    }

    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android"
        val model = Build.MODEL ?: "Device"
        return "$manufacturer $model"
    }

    fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    fun generateSimulatedIp(): String {
        val r = (10..240).random()
        return "192.168.1.$r (WPA3-Chiffré)"
    }

    fun formatCountdown(remainingMillis: Long): String {
        if (remainingMillis <= 0) return "0j 00h 00m 00s"
        val seconds = (remainingMillis / 1000) % 60
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val hours = (remainingMillis / (1000 * 60 * 60)) % 24
        val days = remainingMillis / (1000 * 60 * 60 * 24)

        return String.format(Locale.getDefault(), "%dj %02dh %02dm %02ds", days, hours, minutes, seconds)
    }

    fun formatDetailedRemaining(remainingMillis: Long): Triple<Long, Long, Long> {
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val hours = (remainingMillis / (1000 * 60 * 60)) % 24
        val days = remainingMillis / (1000 * 60 * 60 * 24)
        return Triple(days, hours, minutes)
    }
}

package com.gemini.calories.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Best-effort on-device Gemini availability check.
 * This does not guarantee the model is downloaded, only that required system components seem present.
 */
@Singleton
class OnDeviceGeminiChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val candidatePackages = listOf(
        "com.google.android.aicore",            // AI Core (Pixel)
        "com.google.android.as"                 // Android System Intelligence
    )

    fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return false
        val pm = context.packageManager
        return candidatePackages.any { pkg ->
            try {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}

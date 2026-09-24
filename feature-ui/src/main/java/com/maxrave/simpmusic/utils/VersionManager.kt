package com.alok.dhunora.ui.utils

import android.content.Context
import org.koin.core.context.GlobalContext

object VersionManager {
    fun getVersionName(): String = removeDevSuffix(readVersionName())

    private fun readVersionName(): String =
        try {
            val context: Context = GlobalContext.get().get()
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }

    private fun removeDevSuffix(versionName: String): String {
        return if (versionName.endsWith("-dev")) {
            versionName.replace("-dev", "")
        } else {
            versionName
        }
    }
}

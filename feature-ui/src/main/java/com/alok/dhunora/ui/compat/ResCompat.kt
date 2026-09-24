package com.alok.dhunora.ui.compat

import android.content.Context
import androidx.annotation.StringRes
import org.koin.mp.KoinPlatform

/**
 * Android replacement for compose-resources' suspend `getString`.
 * Resolves via the application context registered in Koin (androidContext).
 * Works from composables, ViewModels, and plain functions alike.
 */
fun getString(
    @StringRes res: Int,
    vararg args: Any,
): String {
    val context: Context = KoinPlatform.getKoin().get()
    return if (args.isEmpty()) context.getString(res) else context.getString(res, *args)
}

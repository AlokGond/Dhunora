package com.alok.dhunora.ui.expect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import org.koin.core.context.GlobalContext

fun copyToClipboard(
    label: String,
    text: String,
) {
    val context: Context = GlobalContext.get().get()
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
}
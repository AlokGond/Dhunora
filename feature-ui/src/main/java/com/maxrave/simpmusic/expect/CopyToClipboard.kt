package com.alok.dhunora.ui.expect

import androidx.compose.runtime.Composable

fun copyToClipboard(
    label: String,
    text: String,
) {
    val context: Context = getKoin().get()
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
}
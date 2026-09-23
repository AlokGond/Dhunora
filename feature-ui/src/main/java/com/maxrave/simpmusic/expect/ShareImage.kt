package com.alok.dhunora.ui.expect

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

/**
 * Writes [bytes] somewhere the user can find it later: the system gallery on Android.
 *
 * Returns false rather than throwing, because every caller is a button press — the UI has to say
 * something either way, and an exception crossing back into a click handler would take the app
 * down instead.
 */
suspend fun saveImageToDevice(
    bytes: ByteArray,
    fileName: String,
): Boolean =
    runCatching {
        val context: Context = getKoin().get()
        val resolver = context.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Dhunora")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    }.getOrDefault(false)

/**
 * Hands [bytes] to the system share sheet via a FileProvider content URI.
 *
 * Returns false rather than throwing, because every caller is a button press — the UI has to say
 * something either way, and an exception crossing back into a click handler would take the app
 * down instead.
 */
suspend fun shareImage(
    bytes: ByteArray,
    fileName: String,
    chooserTitle: String,
): Boolean =
    runCatching {
        val context: Context = getKoin().get()
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(
            Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        true
    }.getOrDefault(false)

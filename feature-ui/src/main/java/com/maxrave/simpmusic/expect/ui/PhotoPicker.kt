@file:Suppress("ktlint:standard:filename")

package com.alok.dhunora.ui.expect.ui

import androidx.compose.runtime.Composable

interface PhotoPickerLauncher {
    fun launch()
}

@Composable
fun photoPickerResult(onResultUri: (String?) -> Unit): PhotoPickerLauncher {
    val context = LocalContext.current
    val resultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                Logger.d("ID", Build.ID.toString())
                val intentRef = activityResult.data
                val data = intentRef?.data
                if (data != null) {
                    val contentResolver = context.contentResolver

                    val takeFlags: Int =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    // Check for the freshest data.
                    context.grantUriPermission(
                        context.packageName,
                        data,
                        takeFlags,
                    )
                    contentResolver?.takePersistableUriPermission(data, takeFlags)
                    val uri = data.toString()
                    onResultUri(uri)
                }
            }
        }
    return object : PhotoPickerLauncher {
        override fun launch() {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            resultLauncher.launch(intent)
        }
    }
}
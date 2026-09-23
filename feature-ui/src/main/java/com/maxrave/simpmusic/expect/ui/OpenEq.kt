package com.alok.dhunora.ui.expect.ui

import androidx.compose.runtime.Composable
import com.alok.dhunora.ui.R

interface OpenEqLauncher {
    fun launch()
}

@Composable
fun openEqResult(audioSessionId: Int): OpenEqLauncher {
    val context = LocalContext.current
    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    return object : OpenEqLauncher {
        override fun launch() {
            val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            val packageManager = context.packageManager
            val resolveInfo: List<*> = packageManager.queryIntentActivities(eqIntent, 0)
            Logger.d("EQ", resolveInfo.toString())
            if (resolveInfo.isEmpty()) {
                showToast(runBlocking { getString(R.string.no_equalizer) }, ToastGravity.Bottom)
            } else {
                resultLauncher.launch(eqIntent)
            }
        }
    }
}
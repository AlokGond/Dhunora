@file:Suppress("ktlint:standard:filename")

package com.alok.dhunora.ui.expect.ui

import androidx.compose.runtime.Composable

interface PhotoPickerLauncher {
    fun launch()
}

@Composable
fun photoPickerResult(onResultUri: (String?) -> Unit): PhotoPickerLauncher {
    return object : PhotoPickerLauncher {
        override fun launch() {
            // TODO: Implement photo picker
        }
    }
}

package com.alok.dhunora.ui.expect

import android.content.Context
import android.content.res.Configuration
import org.koin.core.context.GlobalContext

enum class Orientation {
    PORTRAIT, LANDSCAPE, UNSPECIFIED
}

fun currentOrientation(): Orientation {
    val context: Context = GlobalContext.get().get()
    val orientation = context.resources.configuration.orientation
    return when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
        Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
        else -> Orientation.UNSPECIFIED
    }
}
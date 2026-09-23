package com.alok.dhunora.ui.expect

enum class Orientation {
    PORTRAIT, LANDSCAPE, UNSPECIFIED
}

fun currentOrientation(): Orientation {
    val context: Context = getKoin().get()
    val orientation = context.resources.configuration.orientation
    return when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
        Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
        else -> Orientation.UNSPECIFIED
    }
}
package com.maxrave.data.io

import okio.FileSystem

fun fileSystem(): FileSystem = FileSystem.SYSTEM

fun fileDir(): String {
    val context = getKoin().get<Context>()
    return context.filesDir.absolutePath
}
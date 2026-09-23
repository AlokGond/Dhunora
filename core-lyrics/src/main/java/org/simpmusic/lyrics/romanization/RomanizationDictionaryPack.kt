package org.simpmusic.lyrics.romanization

/**
 * The Japanese analyzer's dictionary pack, on platforms where it is not part of the app.
 *
 * kuromoji's ipadic dictionary is ~13 MB of `.bin` resources. On Android those are excluded from
 * the APK and fetched once, on demand; on Desktop they still ship on the classpath, and iOS has no
 * analyzer at all — both of those answer [isReady] with true so nothing upstream ever asks them to
 * download. Same expect/actual shape as [PlatformRomanizer], which is this module's existing way
 * of splitting the two platform-bound romanizers from the ten pure-Kotlin ones.
 */
object RomanizationDictionaryPack {
    /** Written once at startup by the repository's constructor, read on every romanize of a Japanese line. */
    @Volatile
    internal var dictionaryDirectory: File? = null
        private set

    fun configure(directoryPath: String) {
        dictionaryDirectory = File(directoryPath)
    }

    fun isReady(): Boolean {
        val directory = dictionaryDirectory ?: return false
        return KuromojiDictionary.isReady(directory)
    }

    actual suspend fun download(): Result<Unit> {
        val directory =
            dictionaryDirectory
                ?: return Result.failure(IllegalStateException("RomanizationDictionaryPack.configure was never called"))
        return KuromojiDictionary.download(directory)
    }
}

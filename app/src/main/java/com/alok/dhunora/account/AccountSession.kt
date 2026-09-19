package com.alok.dhunora.account

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AccountSession {
    private const val PREFS = "dhunora_account"
    private const val KEY_COOKIE = "youtube_music_cookie"
    private const val KEY_IV = "youtube_music_cookie_iv"
    private const val KEY_ALIAS = "DhunoraYouTubeSessionKey"

    fun saveCookie(context: Context, cookie: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(cookie.toByteArray(Charsets.UTF_8))

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COOKIE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun cookie(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encryptedString = prefs.getString(KEY_COOKIE, null) ?: return ""
        val ivString = prefs.getString(KEY_IV, null) ?: return ""

        return runCatching {
            val encrypted = Base64.decode(encryptedString, Base64.NO_WRAP)
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, iv)
            )
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrDefault("")
    }

    fun isLoggedIn(context: Context): Boolean {
        val value = cookie(context)
        return value.contains("SAPISID=") ||
            value.contains("__Secure-3PAPISID=") ||
            value.contains("SID=")
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let {
            return it
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )

        return keyGenerator.generateKey()
    }
}

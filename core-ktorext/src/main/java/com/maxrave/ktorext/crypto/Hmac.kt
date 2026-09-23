package com.maxrave.ktorext.crypto

import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Hmac constructor(algorithm: String, secretKey: String) {
    private var tokenTtl: Long = 300000 // 5 minutes in milliseconds
    private val mac: Mac by lazy {
        try {
            Mac.getInstance(algorithm).apply {
                init(SecretKeySpec(secretKey.toByteArray(), algorithm))
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize HMAC", e)
        }
    }

    fun getMacTimestampPair(uri: String): Pair<String, String> {
        val timestamp = Instant.now().toEpochMilli().toString()
        val data = "$timestamp$uri"
        val hmac = this.generateHmac(data)
        return hmac to timestamp
    }

    fun generateHmac(data: String): String = Base64.getEncoder().encodeToString(mac.doFinal(data.toByteArray()))

    fun validateHmac(
        data: String,
        hmac: String,
    ): Boolean {
        val calculatedHmac = generateHmac(data)
        return calculatedHmac == hmac
    }

    fun isValidTimestamp(timestamp: String): Boolean {
        val requestTime = timestamp.toLongOrNull() ?: return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - requestTime) < tokenTtl
    }
}

object HmacUri {
    const val BASE_HMAC_URI = "/v1"
    const val TRANSLATED_HMAC_URI = "/v1/translated"
    const val VOTE_HMAC_URI = "/v1/vote"
    const val VOTE_TRANSLATED_HMAC_URI = "/v1/translated/vote"
}

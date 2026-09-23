package com.maxrave.ktorext.encoding

import io.ktor.client.plugins.compression.ContentEncodingConfig
import io.ktor.util.ContentEncoder

fun createBrotliEncoder(): ContentEncoder = BrotliEncoder

fun ContentEncodingConfig.brotli(quality: Float? = null) {
    customEncoder(createBrotliEncoder(), quality)
}
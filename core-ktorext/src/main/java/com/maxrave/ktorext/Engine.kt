package com.maxrave.ktorext

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

fun getEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = OkHttp
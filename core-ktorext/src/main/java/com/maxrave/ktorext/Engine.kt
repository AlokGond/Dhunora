package com.maxrave.ktorext

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

fun getEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = OkHttp
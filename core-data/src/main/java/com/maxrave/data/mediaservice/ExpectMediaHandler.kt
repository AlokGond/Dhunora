package com.maxrave.data.mediaservice

import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.repository.AnalyticsRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

fun createMediaServiceHandler(
    dataStoreManager: com.maxrave.domain.manager.DataStoreManager,
    songRepository: com.maxrave.domain.repository.SongRepository,
    streamRepository: com.maxrave.domain.repository.StreamRepository,
    localPlaylistRepository: com.maxrave.domain.repository.LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.maxrave.domain.mediaservice.handler.MediaPlayerHandler =
package com.maxrave.data.repository

import com.maxrave.data.db.MusicDatabase
import com.maxrave.data.db.datasource.LocalDataSource
import com.maxrave.data.io.fileSystem
import com.maxrave.domain.data.entities.NotificationEntity
import com.maxrave.domain.data.model.cookie.CookieItem
import com.maxrave.domain.data.type.RecentlyType
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.repository.CommonRepository
import com.maxrave.kotlinytmusicscraper.YouTube
import com.maxrave.kotlinytmusicscraper.models.YouTubeLocale
import com.maxrave.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class CommonRepositoryImpl(
    private val coroutineScope: CoroutineScope,
    private val database: MusicDatabase,
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
    private val spotify: Spotify,
) : CommonRepository {
    @OptIn(ExperimentalTime::class)
    override fun init(
        cookiePath: String,
        dataStoreManager: DataStoreManager,
    ) {
        youTube.cookiePath = cookiePath.toPath()
        coroutineScope.launch {
            val localeJob =
                launch {
                    combine(dataStoreManager.location, dataStoreManager.language) { location, language ->
                        Pair(location, language)
                    }.collectLatest { (location, language) ->
                        youTube.locale =
                            YouTubeLocale(
                                location,
                                try {
                                    language.substring(0..1)
                                } catch (e: Exception) {
                                    "en"
                                },
                            )
                    }
                }
            val ytCookieJob =
                launch {
                    dataStoreManager.cookie.distinctUntilChanged().collectLatest { cookie ->
                        if (cookie.isNotEmpty()) {
                            youTube.cookie = cookie
                            youTube.visitorData()?.let {
                                youTube.visitorData = it
                            }
                        } else {
                            youTube.cookie = null
                        }
                        Logger.d("YouTube", "New cookie")
                        localDataSource.getUsedGoogleAccount()?.netscapeCookie?.let {
                            writeTextToFile(it, cookiePath)
                            Logger.w("YouTube", "Wrote cookie to file")
                        }
                    }
                }
            val pageIdJob =
                launch {
                    dataStoreManager.pageId.distinctUntilChanged().collectLatest { pageId ->
                        youTube.pageId = pageId.ifEmpty { null }
                        Logger.d("YouTube", "New pageId")
                        localDataSource.getUsedGoogleAccount()?.netscapeCookie?.let {
                            writeTextToFile(it, cookiePath)
                            Logger.w("YouTube", "Wrote cookie to file")
                        }
                    }
                }
            val authUserJob =
                launch {
                    dataStoreManager.authUser.distinctUntilChanged().collectLatest { authUser ->
                        youTube.authUser = authUser
                        Logger.d("YouTube", "New authUser")
                    }
                }
            val usingProxy =
                launch {
                    combine(
                        combine(
                            dataStoreManager.usingProxy,
                            dataStoreManager.proxyType,
                            dataStoreManager.proxyHost,
                            dataStoreManager.proxyPort,
                        ) { usingProxy, proxyType, proxyHost, proxyPort ->
                            (usingProxy == DataStoreManager.TRUE) to ProxyData(proxyType, proxyHost, proxyPort, "", "")
                        },
                        dataStoreManager.proxyUsername,
                        dataStoreManager.proxyPassword,
                    ) { (enabled, baseData), username, password ->
                        enabled to baseData.copy(username = username, password = password)
                    }.distinctUntilChanged().collectLatest { (usingProxy, data) ->
                        if (usingProxy) {
                            withContext(Dispatchers.IO) {
                                // Set SOCKS proxy authenticator if credentials are provided
                                if (data.type == DataStoreManager.ProxyType.PROXY_TYPE_SOCKS &&
                                    data.username.isNotEmpty() && data.password.isNotEmpty()
                                ) {
                                    setProxyAuthenticator(data.username, data.password)
                                } else {
                                    clearProxyAuthenticator()
                                }
                                youTube.setProxy(
                                    data.type == DataStoreManager.ProxyType.PROXY_TYPE_HTTP,
                                    data.host,
                                    data.port,
                                )
                                spotify.setProxy(
                                    data.type == DataStoreManager.ProxyType.PROXY_TYPE_HTTP,
                                    data.host,
                                    data.port,
                                )
                            }
                        } else {
                            clearProxyAuthenticator()
                            youTube.removeProxy()
                            spotify.removeProxy()
                        }
                    }
                }
            val dataSyncIdJob =
                launch {
                    dataStoreManager.dataSyncId.collectLatest { dataSyncId ->
                        youTube.dataSyncId = dataSyncId
                    }
                }
            val visitorDataJob =
                launch {
                    dataStoreManager.visitorData.collectLatest { visitorData ->
                        youTube.visitorData = visitorData
                    }
                }
            // Observe job: push cached TIDAL credentials from DataStore into YouTube.
            // Only override when non-blank — credentials are not hard-coded, so an empty cache
            // just leaves TIDAL disabled until the remote config is fetched.
            val tidalCredentialJob =
                launch {
                    combine(
                        dataStoreManager.tidalClientId,
                        dataStoreManager.tidalClientSecret,
                    ) { id, secret -> id to secret }
                        .distinctUntilChanged()
                        .collectLatest { (id, secret) ->
                            if (id.isNotBlank()) youTube.tidalClientId = id
                            if (secret.isNotBlank()) youTube.tidalClientSecret = secret
                        }
                }
            // Fetch job: pull the latest TIDAL credentials from GitHub raw on each launch
            // (async, non-blocking). On success we persist into DataStore; the observe job
            // above then propagates the new values into YouTube reactively.
            val tidalRemoteConfigJob =
                launch {
                    youTube
                        .getTidalRemoteConfig()
                        .onSuccess { config ->
                            // Persist only non-blank fields so a malformed/partial file never
                            // wipes a previously cached value. No need to diff against the current
                            // value — the observe job's distinctUntilChanged already prevents
                            // redundant pushes into YouTube.
                            config.tidalClientId
                                ?.takeIf { it.isNotBlank() }
                                ?.let { dataStoreManager.setTidalClientId(it) }
                            config.tidalClientSecret
                                ?.takeIf { it.isNotBlank() }
                                ?.let { dataStoreManager.setTidalClientSecret(it) }
                        }.onFailure {
                            Logger.e("RemoteConfig", "TIDAL remote config fetch failed: ${it.message}")
                        }
                }

            localeJob.join()
            ytCookieJob.join()
            pageIdJob.join()
            authUserJob.join()
            usingProxy.join()
            dataSyncIdJob.join()
            visitorDataJob.join()
        }
    }

    // Database
    override fun closeDatabase() {
        database.close()
    }

    override fun getDatabasePath() =
        com.maxrave.data.db
            .getDatabasePath()

    override suspend fun databaseDaoCheckpoint() = localDataSource.checkpoint()

    // Recently data
    override fun getAllRecentData(): Flow<List<RecentlyType>> =
        flow {
            emit(localDataSource.getAllRecentData())
        }.flowOn(Dispatchers.IO)

    // Notifications
    override suspend fun insertNotification(notificationEntity: NotificationEntity) =
        withContext(Dispatchers.IO) {
            localDataSource.insertNotification(notificationEntity)
        }

    override suspend fun getAllNotifications(): Flow<List<NotificationEntity>?> =
        flow {
            emit(localDataSource.getAllNotification())
        }.flowOn(Dispatchers.IO)

    override suspend fun isNotificationExists(link: String): Boolean =
        withContext(Dispatchers.IO) {
            localDataSource.countNotificationByLink(link) > 0
        }

    override suspend fun deleteNotification(id: Long) =
        withContext(Dispatchers.IO) {
            localDataSource.deleteNotification(id)
        }

    override suspend fun writeTextToFile(
        text: String,
        filePath: String,
    ): Boolean {
        try {
            fileSystem().sink(filePath.toPath()).buffer().use { sink ->
                sink.writeUtf8(text)
                sink.close()
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Original from YTDLnis app
     */
    override suspend fun getCookiesFromInternalDatabase(
        url: String,
        packageName: String,
    ): CookieItem =
        withContext(Dispatchers.IO) {
            return@withContext getCookies(
                url,
                packageName,
            )
        }
}

private data class ProxyData(
    val type: DataStoreManager.ProxyType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
)

fun setProxyAuthenticator(username: String, password: String) {
    Authenticator.setDefault(
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                if (requestorType == RequestorType.PROXY) {
                    return PasswordAuthentication(username, password.toCharArray())
                }
                return null
            }
        },
    )
}

fun clearProxyAuthenticator() {
    Authenticator.setDefault(null)
}

fun getCookies(url: String, packageName: String): CookieItem {
        return try {
            val projection =
                arrayOf(
                    CookieItem.HOST,
                    CookieItem.EXPIRY,
                    CookieItem.PATH,
                    CookieItem.NAME,
                    CookieItem.VALUE,
                    CookieItem.SECURE,
                )
            CookieManager.getInstance().flush()
            val cookieList = mutableListOf<CookieItem.Content>()
            val dbPath =
                File("/data/data/${packageName}/").walkTopDown().find { it.name == "Cookies" }
                    ?: throw Exception("Cookies File not found!")

            val db =
                SQLiteDatabase.openDatabase(
                    dbPath.absolutePath,
                    null,
                    OPEN_READONLY,
                )
            db
                .query(
                    "cookies",
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null,
                ).run {
                    while (moveToNext()) {
                        val expiry = getLong(getColumnIndexOrThrow(CookieItem.EXPIRY))
                        val name = getString(getColumnIndexOrThrow(CookieItem.NAME))
                        val value = getString(getColumnIndexOrThrow(CookieItem.VALUE))
                        val path = getString(getColumnIndexOrThrow(CookieItem.PATH))
                        val secure = getLong(getColumnIndexOrThrow(CookieItem.SECURE)) == 1L
                        val hostKey = getString(getColumnIndexOrThrow(CookieItem.HOST))

                        val host = if (hostKey[0] != '.') ".$hostKey" else hostKey
                        cookieList.add(
                            CookieItem.Content(
                                domain = host,
                                name = name,
                                value = value,
                                isSecure = secure,
                                expiresUtc = expiry,
                                hostKey = host,
                                path = path,
                            ),
                        )
                    }
                    close()
                }
            db.close()
            CookieItem(url, cookieList)
        } catch (e: Exception) {
            e.printStackTrace()
            CookieItem(url, emptyList())
        }
}
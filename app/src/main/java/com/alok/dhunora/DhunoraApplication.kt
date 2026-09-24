package com.alok.dhunora

import android.app.Application
import android.database.CursorWindow
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.alok.dhunora.ui.di.viewModelModule
import com.maxrave.common.AppIdentity
import com.maxrave.data.di.loader.loadAllModules
import multiplatform.network.cmptoast.AppContext
import okhttp3.OkHttpClient
import okio.FileSystem
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

class DhunoraApplication : Application(), SingletonImageLoader.Factory {

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(this@DhunoraApplication)
            loadAllModules(
                AppIdentity(
                    applicationId = "com.alok.dhunora",
                    versionName = "0.19.0",
                    platform = "Android ${android.os.Build.VERSION.RELEASE}",
                ),
            )
            loadKoinModules(viewModelModule)
        }
        // Load the Media3 service module (player backend DI)
        com.maxrave.media3.di.loadMediaService()

        try {
            val field: java.lang.reflect.Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AppContext.apply {
            set(applicationContext)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient.Builder()
                                .connectTimeout(15, TimeUnit.SECONDS)
                                .readTimeout(20, TimeUnit.SECONDS)
                                .followRedirects(true)
                                .followSslRedirects(true)
                                .build()
                        }
                    )
                )
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .build()
}

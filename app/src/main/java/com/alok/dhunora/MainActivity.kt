package com.alok.dhunora

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import com.alok.dhunora.ui.App
import com.alok.dhunora.ui.viewModel.SharedViewModel
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.MediaPlayerHandler
import com.maxrave.domain.model.ToastType
import com.maxrave.logger.Logger
import com.maxrave.media3.di.setServiceActivitySession
import com.maxrave.media3.di.startService
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class MainActivity : AppCompatActivity() {
    val viewModel: SharedViewModel by inject()
    val mediaPlayerHandler by inject<MediaPlayerHandler>()
    val dataStoreManager: DataStoreManager by inject()

    private var mBound = false
    private var shouldUnbind = false
    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                setServiceActivitySession(this@MainActivity, MainActivity::class.java, service)
                Logger.w("MainActivity", "onServiceConnected: ")
                mBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Logger.w("MainActivity", "onServiceDisconnected: ")
                mBound = false
            }
        }

    override fun onStart() {
        super.onStart()
        startMusicService()
    }

    override fun onStop() {
        super.onStop()
        if (shouldUnbind) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Logger.e("MainActivity", "unbind failed: ${e.message}")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.d("MainActivity", "onNewIntent: $intent")
        viewModel.setIntent(
            com.maxrave.domain.data.model.intent.GenericIntent(
                action = intent.action,
                data = (intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.let { android.net.Uri.parse(it) })?.toString()?.let { com.eygraber.uri.Uri.parse(it) },
                type = intent.type,
            ),
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(
            module {
                single<AppCompatActivity> { this@MainActivity }
            },
        )
        // Recreate view model to fix the issue of view model not getting data from the service
        unloadKoinModules(com.alok.dhunora.ui.di.viewModelModule)
        loadKoinModules(com.alok.dhunora.ui.di.viewModelModule)
        if (viewModel.recreateActivity.value || viewModel.isServiceRunning) {
            viewModel.activityRecreateDone()
            recreate()
        }
        handleIntent(intent)
        setContent {
            App(viewModel)
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        Logger.d("MainActivity", "handleIntent: $intent")
        viewModel.setIntent(
            com.maxrave.domain.data.model.intent.GenericIntent(
                action = intent.action,
                data = (intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.let { android.net.Uri.parse(it) })?.toString()?.let { com.eygraber.uri.Uri.parse(it) },
                type = intent.type,
            ),
        )
    }

    override fun onDestroy() {
        val shouldStopMusicService = viewModel.shouldStopMusicService()
        Logger.w("MainActivity", "onDestroy: Should stop service $shouldStopMusicService")
        if (shouldUnbind) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Logger.e("MainActivity", "unbind failed: ${e.message}")
            }
            shouldUnbind = false
        }
        super.onDestroy()
    }

    private fun startMusicService() {
        startService(this@MainActivity, serviceConnection)
        mediaPlayerHandler.pushPlayerError = { it ->
            pushPlayerError(it)
        }
        mediaPlayerHandler.showToast = { type ->
            viewModel.makeToast(
                when (type) {
                    is ToastType.ExplicitContent -> {
                        runBlocking { viewModel.getString("explicit_content_blocked") } ?: "Explicit content blocked"
                    }
                    is ToastType.PlayerError -> {
                        runBlocking { viewModel.getString("time_out_error") } ?: "Player error: ${type.error}"
                    }
                },
            )
        }
        viewModel.isServiceRunning = true
        shouldUnbind = true
        Logger.d("Service", "Service started")
    }

    private fun pushPlayerError(error: String) {
        Logger.e("MainActivity", "Player error: $error")
    }
}

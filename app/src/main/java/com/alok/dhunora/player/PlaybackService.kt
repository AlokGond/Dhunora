package com.alok.dhunora.player

import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val loadControl =
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    6_000,
                    25_000,
                    400,
                    800
                )
                .build()

        val exoPlayer =
            ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build()

        player = exoPlayer
        session =
            MediaSession.Builder(this, exoPlayer)
                .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = session

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // Keep audio alive while the user is listening. Media3 will remove
        // the foreground notification after playback is stopped.
        if (player?.playWhenReady != true) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        session?.release()
        session = null
        player?.release()
        player = null
        super.onDestroy()
    }
}

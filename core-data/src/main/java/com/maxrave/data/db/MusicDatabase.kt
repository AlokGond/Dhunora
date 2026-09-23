package com.maxrave.data.db

import DatabaseDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.execSQL
import androidx.room.useWriterConnection
import com.maxrave.common.DB_NAME
import org.koin.core.context.GlobalContext
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.AutoEqCurveEntity
import com.maxrave.domain.data.entities.AutoEqEntryEntity
import com.maxrave.domain.data.entities.AutoEqIndexMetaEntity
import com.maxrave.domain.data.entities.EpisodeEntity
import com.maxrave.domain.data.entities.FollowedArtistSingleAndAlbum
import com.maxrave.domain.data.entities.GoogleAccountEntity
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.LyricsEntity
import com.maxrave.domain.data.entities.NewFormatEntity
import com.maxrave.domain.data.entities.NotificationEntity
import com.maxrave.domain.data.entities.PairSongLocalPlaylist
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.PodcastsEntity
import com.maxrave.domain.data.entities.QueueEntity
import com.maxrave.domain.data.entities.SearchHistory
import com.maxrave.domain.data.entities.SetVideoIdEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.entities.SongInfoEntity
import com.maxrave.domain.data.entities.TranslatedLyricsEntity
import com.maxrave.domain.data.entities.YourYouTubePlaylistList
import com.maxrave.domain.data.entities.analytics.EventArtistEntity
import com.maxrave.domain.data.entities.analytics.PlaybackEventEntity

@Database(
    entities = [
        NewFormatEntity::class, SongInfoEntity::class, SearchHistory::class, SongEntity::class, ArtistEntity::class,
        AlbumEntity::class, PlaylistEntity::class, LocalPlaylistEntity::class, LyricsEntity::class, QueueEntity::class,
        SetVideoIdEntity::class, PairSongLocalPlaylist::class, GoogleAccountEntity::class, FollowedArtistSingleAndAlbum::class,
        NotificationEntity::class, TranslatedLyricsEntity::class, PodcastsEntity::class, EpisodeEntity::class,
        YourYouTubePlaylistList::class, PlaybackEventEntity::class, EventArtistEntity::class,
        AutoEqEntryEntity::class, AutoEqIndexMetaEntity::class, AutoEqCurveEntity::class
    ],
    version = 26,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun getDatabaseDao(): DatabaseDao

    /**
     * Rewrite the database file so the pages a bulk delete freed go back to the filesystem.
     *
     * It lives here rather than on the DAO because the DAO has no way to ask for a **writer**
     * connection. `DatabaseDao.raw()` is the only door out to arbitrary SQL, and Room cannot parse
     * what a `@RawQuery` will do, so it generates `performSuspending(__db, isReadOnly = true, ...)`
     * for it — while a parsed `@Query` that deletes gets `isReadOnly = false`. Reader connections
     * are opened with `PRAGMA query_only = 1`, under which VACUUM fails outright with "attempt to
     * write a readonly database". `PRAGMA wal_checkpoint` is accepted on that very same connection,
     * which is why the sibling `DatabaseDao.checkpoint()` works and hid this for so long.
     *
     * [execSQL] prepares and steps the statement without opening a transaction, which is required:
     * SQLite refuses VACUUM inside one. Do not wrap this call in [androidx.room.Transactor.withTransaction].
     */
    suspend fun vacuum() {
        useWriterConnection { it.execSQL("VACUUM") }
    }
}

fun getDatabaseBuilder(converters: Converters) : RoomDatabase.Builder<MusicDatabase> {
    return Room
        .databaseBuilder(GlobalContext.get().get(), MusicDatabase::class.java, DB_NAME)
        .addTypeConverter(converters)
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    connection.execSQL(
                        "CREATE TRIGGER  IF NOT EXISTS on_delete_pair_song_local_playlist AFTER DELETE ON pair_song_local_playlist\n" +
                            "FOR EACH ROW\n" +
                            "BEGIN\n" +
                            "    UPDATE pair_song_local_playlist\n" +
                            "    SET position = position - 1\n" +
                            "    WHERE playlistId = OLD.playlistId AND position > OLD.position;\n" +
                            "END;",
                    )
                }
            },
        )
}

fun getDatabasePath(): String {
    return GlobalContext.get().get<Context>().getDatabasePath(DB_NAME).path
}
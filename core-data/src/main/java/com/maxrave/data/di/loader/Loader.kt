package com.maxrave.data.di.loader

import com.maxrave.common.AppIdentity
import com.maxrave.data.di.databaseModule
import com.maxrave.data.di.mediaHandlerModule
import com.maxrave.data.di.repositoryModule
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun loadAllModules(appIdentity: AppIdentity) {
    loadKoinModules(
        listOf(
            module { single { appIdentity } },
            databaseModule,
            repositoryModule,
        ),
    )
    loadKoinModules(mediaHandlerModule)
}

/**
 * Android: loads the Media3 service Koin module.
 * Called from the Application after loadAllModules().
 * (In the original KMP source this was an expect/actual; the Android actual
 * lived in the media3 module which core-data cannot depend on.)
 */
fun loadMediaService() {
    // No-op here: the app module calls com.maxrave.media3.di.loadMediaService() directly.
}

/** Login sync is pruned in this build. */
fun loadLoginSyncModule() {
    // No-op
}

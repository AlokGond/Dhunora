package com.alok.dhunora

import android.app.Application
import com.alok.dhunora.network.OkHttpDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class DhunoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(OkHttpDownloader(), Localization("en", "IN"), ContentCountry("IN"))
    }
}

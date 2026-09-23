package com.alok.dhunora.ui.utils

import com.alok.dhunora.ui.compat.getString
import com.alok.dhunora.ui.R

object ComposeResUtils {
    suspend fun getResString(
        type: StringType,
        vararg format: String,
    ): String =
        when (type) {
            StringType.EXPLICIT_CONTENT_BLOCKED -> {
                getString(R.string.explicit_content_blocked)
            }

            StringType.NOTIFICATION_REQUEST -> {
                getString(R.string.this_app_needs_to_access_your_notification)
            }

            StringType.TIME_OUT_ERROR -> {
                getString(R.string.time_out_check_internet_connection_or_change_piped_instance_in_settings, *format)
            }

            StringType.NEW_SINGLES -> {
                getString(R.string.new_singles)
            }

            StringType.NEW_ALBUMS -> {
                getString(R.string.new_albums)
            }
        }

    enum class StringType {
        EXPLICIT_CONTENT_BLOCKED,
        NOTIFICATION_REQUEST,
        TIME_OUT_ERROR,
        NEW_SINGLES,
        NEW_ALBUMS,
    }
}
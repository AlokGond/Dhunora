package com.alok.dhunora.ui.ui.component

import androidx.compose.foundation.layout.Box
import com.alok.dhunora.ui.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maxrave.domain.extension.now
import com.alok.dhunora.ui.ui.theme.typo
import com.alok.dhunora.ui.utils.VersionManager
import androidx.compose.ui.res.stringResource

@Composable
fun EndOfPage(withoutCredit: Boolean = false) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(280.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (!withoutCredit) {
            Text(
                "@${now().year} " + stringResource(R.string.app_name) + " " +
                    stringResource(
                        R.string.version_format,
                        VersionManager.getVersionName(),
                    ) + "\nmaxrave-dev",
                style = typo().bodySmall,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(
                            top = 20.dp,
                        ).alpha(0.8f),
            )
        }
    }
}
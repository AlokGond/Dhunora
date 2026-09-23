package com.alok.dhunora.ui.ui.component

import androidx.compose.material3.AlertDialog
import com.alok.dhunora.ui.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.alok.dhunora.ui.ui.theme.typo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
@ExperimentalMaterial3Api
fun BlogPromoDialog(
    onDismissRequest: () -> Unit,
    onVisitBlog: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        confirmButton = {
            TextButton(onClick = {
                onVisitBlog.invoke()
                uriHandler.openUri("https://maxrave.dev")
            }) {
                Text(
                    stringResource(R.string.visit_blog),
                    style = typo().bodySmall,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest.invoke()
            }) {
                Text(
                    stringResource(R.string.later),
                    style = typo().bodySmall,
                )
            }
        },
        icon = {
            Icon(painterResource(R.drawable.mono), "App Icon")
        },
        title = {
            Text(
                stringResource(R.string.blog_promo_title),
                style = typo().labelSmall,
            )
        },
        text = {
            Text(
                stringResource(R.string.blog_promo_message),
                textAlign = TextAlign.Center,
                style = typo().bodySmall,
            )
        },
    )
}

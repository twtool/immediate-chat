package icu.twtool.chat.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.theme.ElevationTokens

@Composable
fun WindowDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest, properties) {
        Surface(
            modifier,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().padding(4.dp)) {
                    Text(title, Modifier.align(Alignment.Center), style = MaterialTheme.typography.titleSmall)
                    Icon(
                        Icons.Filled.Close, null,
                        Modifier.align(Alignment.CenterEnd)
                            .clickable(onClick = onDismissRequest)
                    )
                }
                Spacer(
                    modifier = Modifier.requiredHeight(1.dp).fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
                )
                content()
            }
        }
    }
}

@Composable
fun TextDialog(title: String, onDismissRequest: () -> Unit = {}, properties: DialogProperties = DialogProperties()) {
    Dialog(onDismissRequest, properties) {
        Column(
            Modifier.size(128.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.requiredHeight(16.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Immutable
class LoadingDialogState(
    val tittle: String,
    val success: Boolean = false,
    val error: Boolean = false
)

@Composable
fun rememberLoadingDialogState(state: LoadingDialogState? = null) =
    remember { mutableStateOf(state) }

@Composable
fun LoadingDialog(
    state: LoadingDialogState,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties()
) {
    Dialog(onDismissRequest, properties) {
        Column(
            Modifier.size(128.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides when {
                    state.success -> MaterialTheme.colorScheme.primary
                    state.error -> MaterialTheme.colorScheme.error
                    else -> LocalContentColor.current
                }
            ) {
                Crossfade(state) {
                    Box(
                        Modifier.size(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (it.success) Icon(
                            Icons.Filled.Check, null,
                            Modifier.size(32.dp),
                        )
                        else if (it.error) Icon(
                            Icons.Filled.Warning, null, Modifier.size(32.dp)
                        )
                        else CircularProgressIndicator()
                    }
                }
                Spacer(Modifier.requiredHeight(16.dp))
                Crossfade(state.tittle) {
                    Text(it, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
package icu.twtool.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.theme.ElevationTokens

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
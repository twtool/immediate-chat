package icu.twtool.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索"
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.requiredWidth(8.dp))
        Icon(
            Icons.Filled.Search, null,
            Modifier.size(20.dp),
            tint = LocalContentColor.current.copy(DisabledAlpha)
        )
        Spacer(Modifier.requiredWidth(8.dp))
        BasicTextField(value, onValueChange, Modifier.weight(1f)) {
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty())
                    Text(placeholder, color = LocalContentColor.current.copy(DisabledAlpha))
                it()
            }
        }
    }
}
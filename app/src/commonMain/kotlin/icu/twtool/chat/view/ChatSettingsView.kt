package icu.twtool.chat.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.ChatRoute

@Composable
private fun ItemGroup(content: @Composable () -> Unit) {
    Column(Modifier.clip(MaterialTheme.shapes.small)) {
        content()
    }
}

@Composable
private fun ButtonItem(onClick: () -> Unit, title: String) {

}

@Composable
private fun SwitchItem(checked: Boolean, onCheckedChange: (Boolean) -> Unit, title: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked, onCheckedChange)
    }
}

@Composable
fun ChatSettingsView(modifier: Modifier = Modifier) {
    val info = ChatRoute.info
    Column(modifier.padding(16.dp)) {
        ItemGroup {
            SwitchItem(false, {}, "设为置顶")
        }
    }
}
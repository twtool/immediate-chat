package icu.twtool.chat.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.components.BackTopAppBar
import icu.twtool.chat.navigation.window.ICWindowWidthSizeClass
import icu.twtool.chat.theme.ElevationTokens
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_emoji
import org.jetbrains.compose.resources.painterResource

@Composable
fun ChatInfoTopAppBar(onBack: () -> Unit, title: String, onClickMenu: () -> Unit) {
    BackTopAppBar(onBack, title) {
        IconButton(onClickMenu) {
            Icon(Icons.Filled.Menu, "Menu")
        }
    }
}

@Composable
fun ChatView(widthSizeClass: ICWindowWidthSizeClass, onBack: () -> Unit, navigateToChatSettingsRoute: () -> Unit) {
    val info = ChatRoute.info ?: return
    val chatSettingFocusRequester = remember { FocusRequester() }
    Column(Modifier.fillMaxSize()) {
        ChatInfoTopAppBar(
            onBack, info.nickname ?: "未命名用户",
            onClickMenu = {
                if (widthSizeClass < ICWindowWidthSizeClass.Expanded) navigateToChatSettingsRoute()
                else chatSettingFocusRequester.requestFocus()
            }
        )
        Box(
            Modifier.fillMaxSize()
                .clickable(remember { MutableInteractionSource() }, null) {
                    chatSettingFocusRequester.freeFocus()
                }
        ) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f)) { }
                if (widthSizeClass < ICWindowWidthSizeClass.Expanded) ChatViewInput()
                else ChatViewExpandedInput()
            }
            ChatSettingsPopup(chatSettingFocusRequester)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewIcon(tooltip: String, icon: Painter) {
    TooltipBox(
        TooltipDefaults.rememberPlainTooltipPositionProvider(),
        {
            PlainTooltip {
                Text(tooltip)
            }
        },
        rememberTooltipState()
    ) {
        Icon(icon, tooltip, Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewExpandedInput() {
    Surface(
        Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1)
    ) {
        Column {
            Row(Modifier.padding(8.dp)) {
                ChatViewIcon("表情", painterResource(Res.drawable.ic_emoji))
            }
            BasicTextField("", {}, Modifier.fillMaxWidth().height(128.dp).padding(8.dp, 0.dp))
            Button(
                {},
                Modifier.align(Alignment.End).padding(8.dp).height(28.dp),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(8.dp, 4.dp)
            ) {
                Text("发送")
            }
        }
    }
}

@Composable
fun ChatViewInput() {

}


@Composable
fun BoxScope.ChatSettingsPopup(focusRequester: FocusRequester) {
    val interactionSource = remember { MutableInteractionSource() }

    val focused by interactionSource.collectIsFocusedAsState()

    val width = animateDpAsState(targetValue = if (focused) 300.dp else 0.dp)

    Surface(
        Modifier.align(Alignment.TopEnd).width(width.value).fillMaxHeight()
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {},
        shadowElevation = ElevationTokens.Level2
    ) {
        if (focused) {
            ChatSettingsView()
        }
    }

}
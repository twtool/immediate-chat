package icu.twtool.chat.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import icu.twtool.chat.app.ChatRoute
import icu.twtool.chat.components.BackTopAppBar

@Composable
fun ChatInfoTopAppBar(onBack: () -> Unit, title: String) {
    BackTopAppBar(onBack, title)
}

@Composable
fun ChatView(onBack: () -> Unit) {
    val info = ChatRoute.info ?: return
    Column(Modifier.fillMaxSize()) {
        ChatInfoTopAppBar(onBack, info.nickname ?: "未命名用户")
    }
}
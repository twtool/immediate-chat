package icu.twtool.chat.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.onIcExternalDrag(onDragFiles: (List<String>) -> Unit) = this
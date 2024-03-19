package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.AccountInfoRoute
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.components.SearchInput
import icu.twtool.chat.components.WindowDialog
import icu.twtool.chat.components.topAppBarColors
import icu.twtool.chat.constants.Platform
import icu.twtool.chat.constants.getPlatform
import icu.twtool.chat.navigation.window.ICWindowSizeClass
import icu.twtool.chat.navigation.window.systemBarWindowInsets
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.theme.ElevationTokens
import icu.twtool.chat.utils.QRCodeCanvas
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_qrcode
import immediatechat.app.generated.resources.ic_scan_code
import immediatechat.app.generated.resources.logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendTopAppBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
        },
        title = {
            Text("添加好友")
        },
        colors = topAppBarColors(),
        windowInsets = systemBarWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}

@Composable
fun AddFriendSearchInput(snackbarHostState: SnackbarHostState, navigateToAccountInfoRoute: () -> Unit) {
    val scope = rememberCoroutineScope()
    var uid by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SearchInput(uid, { uid = it.trim() }, Modifier.weight(1f), placeholder = "UID")
        Button(
            {
                scope.launch {
                    val res = AccountService.get().getInfoByUID(uid)
                    if (res.success) {
                        res.data?.let {
                            AccountInfoRoute.open(it) {
                                navigateToAccountInfoRoute()
                            }
                        }
                    } else snackbarHostState.showSnackbar("用户不存在")
                }
            },
            Modifier.height(32.dp),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(8.dp, 2.dp)
        ) {
            Text("搜索")
        }
    }
}

@Composable
fun AddFriendItem(
    icon: Painter,
    background: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconTint: Color = LocalContentColor.current
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(MaterialTheme.shapes.small).background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, "icon", tint = iconTint)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(DisabledAlpha)
            )
        }
        Icon(
            Icons.AutoMirrored.Default.KeyboardArrowRight, "right",
            tint = LocalContentColor.current.copy(DisabledAlpha)
        )
    }
}

@Composable
private fun DividingLine() {
    Spacer(
        Modifier.requiredHeight(1.dp).fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
    )
}

@Composable
fun QRCodeDialog(onDismissRequest: () -> Unit) {
    val painter = produceImageState(LoggedInState.info?.avatarUrl)
    WindowDialog("个人二维码", onDismissRequest, Modifier.width(IntrinsicSize.Min)) {
        QRCodeCanvas(
            "UID:${LoggedInState.info?.uid}",
            painter.value ?: painterResource(Res.drawable.logo),
            Modifier.padding(16.dp).width(300.dp).aspectRatio(1f),
            color = MaterialTheme.colorScheme.tertiary,
            logoDpSize = 42.dp,
        )
    }
}

@Composable
fun AddFriendView(
    snackbarHostState: SnackbarHostState,
    windowSize: ICWindowSizeClass,
    onBack: () -> Unit,
    navigateToAccountInfoRoute: () -> Unit,
    navigateToScanCodeRoute: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        AddFriendTopAppBar(onBack = onBack)
        AddFriendSearchInput(snackbarHostState, navigateToAccountInfoRoute)
        Spacer(Modifier.requiredHeight(56.dp))
        var showQrCodeDialog by remember { mutableStateOf(false) }
        if (showQrCodeDialog) QRCodeDialog(onDismissRequest = { showQrCodeDialog = false })
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DividingLine()
            AddFriendItem(
                painterResource(Res.drawable.ic_qrcode),
                MaterialTheme.colorScheme.secondaryContainer,
                "个人二维码", "查看个人二维码名片",
                onClick = { showQrCodeDialog = true },
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            DividingLine()
            if (getPlatform() == Platform.Android) {
                AddFriendItem(
                    painterResource(Res.drawable.ic_scan_code),
                    MaterialTheme.colorScheme.primaryContainer,
                    "扫一扫", "通过扫描个人二维码添加",
                    onClick = navigateToScanCodeRoute,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                DividingLine()
            }
        }
    }
}
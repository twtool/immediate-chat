package icu.twtool.chat.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.LoginParam
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.theme.ElevationTokens
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_register
import immediatechat.app.generated.resources.register_account
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginView(
    snackbarState: SnackbarHostState,
    paddingValues: PaddingValues,
    onSuccess: () -> Unit,
    navigateToRegisterRoute: () -> Unit
) {
    LoginContent(
        snackbarState.currentSnackbarData == null,
        onSuccess = {
            onSuccess()
        },
        onError = { msg ->
            snackbarState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Short)
        },
        navigateToRegisterRoute = navigateToRegisterRoute,
        Modifier.padding(paddingValues).fillMaxSize()
    )
}

@Composable
fun LoginContent(
    enabled: Boolean = true,
    onSuccess: () -> Unit,
    onError: suspend (String) -> Unit,
    navigateToRegisterRoute: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    var doLogin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    if (doLogin) {
        Dialog({}, DialogProperties()) {
            Column(
                Modifier.size(128.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.requiredHeight(16.dp))
                Text("登录中...", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var principalValue by remember { mutableStateOf("") }
        var pwdValue by remember { mutableStateOf("") }

        Spacer(Modifier.requiredHeight(32.dp))

        Image(painterResource(DrawableResource("drawable/logo.xml")), "logo")

        Spacer(Modifier.requiredHeight(16.dp))

        PrincipalTextField(principalValue) { principalValue = it }
        PwdTextField(pwdValue) { pwdValue = it }

        Spacer(Modifier.requiredHeight(0.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    if (doLogin || !enabled) return@launch
                    doLogin = true
                    val doDelay = launch {
                        delay(500)
                    }
                    val res = AccountService.get().login(LoginParam(principalValue, pwdValue))
                    if (res.success) {
                        LoggedInState.login(res.data)
                        onSuccess()
                        return@launch
                    }
//                    if (!res.success) {
//                    }
                    doDelay.join()
                    doLogin = false
                    onError(res.msg)
                }
            },
            Modifier.width(ComponentWidth), shape = MaterialTheme.shapes.extraSmall,
            enabled = enabled
        ) {
            Text("登录")
        }

        Spacer(Modifier.requiredHeight(48.dp))

        Row {
            Column(
                Modifier.clickable(MutableInteractionSource(), null) {
                    navigateToRegisterRoute()
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painterResource(Res.drawable.ic_register), "register",
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level1),
                        CircleShape
                    ).padding(16.dp).size(16.dp)
                )
                Spacer(Modifier.requiredHeight(8.dp))
                Text(stringResource(Res.string.register_account), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun LoginTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, password: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)

    BasicTextField(
        value, onValueChange,
        Modifier.width(ComponentWidth).height(56.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.Level2))
            .padding(4.dp),
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = textStyle,
        decorationBox = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    !focused && value.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(placeholder, style = textStyle)
                }
                it()
            }
        },
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun PrincipalTextField(value: String, onValueChange: (String) -> Unit) {
    LoginTextField(value, {
        onValueChange(it.replace(Regex("\\s+"), ""))
    }, "输入 UID / 邮箱")
}

@Composable
fun PwdTextField(value: String, onValueChange: (String) -> Unit) {
    LoginTextField(value, {
        onValueChange(it.replace(Regex("\\s+"), ""))
    }, "输入账号密码", true)
}

private val ComponentWidth = 256.dp
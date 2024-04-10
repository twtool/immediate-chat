package icu.twtool.chat.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import icu.twtool.chat.server.account.AccountService
import icu.twtool.chat.server.account.param.RegisterParam
import icu.twtool.chat.service.get
import icu.twtool.chat.state.LoggedInState
import icu.twtool.chat.utils.ICBackHandler
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.agree_continue
import immediatechat.app.generated.resources.back_login_view
import immediatechat.app.generated.resources.captcha_sent
import immediatechat.app.generated.resources.confirm
import immediatechat.app.generated.resources.ic_back
import immediatechat.app.generated.resources.input_captcha_and_setup_pwd
import immediatechat.app.generated.resources.input_confirm_pwd
import immediatechat.app.generated.resources.input_email
import immediatechat.app.generated.resources.input_pwd
import immediatechat.app.generated.resources.please_read_and_agree_protocol
import immediatechat.app.generated.resources.pwd_inconsistent
import immediatechat.app.generated.resources.register_account_and_bind_email
import immediatechat.app.generated.resources.register_and_login
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

private enum class RegisterStep {
    INPUT_EMAIL, // 输入邮箱
    VERIFY, // 输入密码和验证码
    INPUT_INFO // 修改用户信息
}


@Composable
fun RegisterView(
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    navigateToLoginRoute: () -> Unit,
    navigateToHomeRoute: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    ICBackHandler {
        showConfirmDialog = true
    }

    if (showConfirmDialog) {
        AlertDialog(
            { showConfirmDialog = false },
            title = { Text(stringResource(Res.string.back_login_view)) },
            confirmButton = {
                TextButton(navigateToLoginRoute) {
                    Text(stringResource(Res.string.confirm))
                }
            }
        )
    }

    var step by remember { mutableStateOf(RegisterStep.INPUT_EMAIL) }

    var email: String by remember { mutableStateOf("") }

    Column(Modifier.padding(paddingValues)) {
        IconButton({ showConfirmDialog = true }, Modifier.padding(16.dp)) {
            Icon(painterResource(Res.drawable.ic_back), "返回")
        }

        AnimatedContent(
            step,
            transitionSpec = {
                fadeIn() togetherWith fadeOut() using SizeTransform(false) { _, targetSize ->
                    keyframes {
                        IntSize(targetSize.width, targetSize.height) at 0
                    }
                }
            }
        ) { state ->
            when (state) {
                RegisterStep.INPUT_EMAIL -> InputEmailStep(
                    snackbarHostState,
                    email,
                    { email = it },
                    toNext = {
                        step = RegisterStep.VERIFY
                    }
                )

                RegisterStep.VERIFY -> VerifyStep(
                    snackbarHostState,
                    email,
                    toNext = { step = RegisterStep.INPUT_INFO }
                )

                RegisterStep.INPUT_INFO -> InputInfoStep(
                    toHome = { navigateToHomeRoute() },
                )
            }
        }
    }
}

@Composable
private fun InputEmailStep(
    snackbarHostState: SnackbarHostState,
    email: String, onChangeEmail: (String) -> Unit, toNext: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var checkedProtocol by remember { mutableStateOf(false) }

        Text(
            stringResource(Res.string.register_account_and_bind_email),
            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp)
        )

        Spacer(Modifier.requiredHeight(64.dp))

        TextField(
            email, onChangeEmail,
            Modifier.width(300.dp),
            label = { Text(stringResource(Res.string.input_email)) },
            singleLine = true
        )

        Spacer(Modifier.requiredHeight(32.dp))

        val checkedProtocolTip = stringResource(Res.string.please_read_and_agree_protocol)

        Button(
            {
                scope.launch {
                    if (!checkedProtocol) {
                        snackbarHostState.showSnackbar(
                            checkedProtocolTip,
                            withDismissAction = true,
                        )
                        return@launch
                    }

                    val res = AccountService.get().getRegisterCaptcha(email)
                    if (res.success) toNext()
                    else snackbarHostState.showSnackbar(res.msg, withDismissAction = true)
                }
            },
            Modifier.width(300.dp),
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(stringResource(Res.string.agree_continue))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checkedProtocol, { checkedProtocol = it })
            Text(buildAnnotatedString {
                append("已阅读并同意")
                withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
                    append("服务协议")
                }
            })
        }
    }
}

@Composable
private fun VerifyStep(
    snackbarHostState: SnackbarHostState,
    email: String, toNext: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(Res.string.input_captcha_and_setup_pwd),
            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp)
        )

        Spacer(Modifier.requiredHeight(64.dp))

        var captcha by remember { mutableStateOf("") }
        var pwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }

        Text(buildAnnotatedString {
            append(stringResource(Res.string.captcha_sent))
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(email)
            }
        }, Modifier.width(300.dp))

        Spacer(Modifier.requiredHeight(8.dp))

        CaptchaInputField(captcha, onChangeCaptcha = { captcha = it }, Modifier.width(300.dp))

        Spacer(Modifier.requiredHeight(32.dp))

        TextField(
            pwd, { pwd = it },
            Modifier.width(300.dp),
            label = { Text(stringResource(Res.string.input_pwd)) },
            singleLine = true
        )

        Spacer(Modifier.requiredHeight(16.dp))

        TextField(
            confirmPwd, { confirmPwd = it },
            Modifier.width(300.dp),
            label = { Text(stringResource(Res.string.input_confirm_pwd)) },
            singleLine = true
        )

        Spacer(Modifier.requiredHeight(32.dp))

        val pwdInconsistentTip = stringResource(Res.string.pwd_inconsistent)

        Button(
            {
                scope.launch {
                    RegisterParam.verifyPwd(pwd)?.let {
                        snackbarHostState.showSnackbar(it)
                        return@launch
                    }
                    if (pwd != confirmPwd) {
                        snackbarHostState.showSnackbar(pwdInconsistentTip)
                        return@launch
                    }
                    LoggedInState.info = null
                    val res = AccountService.get().register(
                        RegisterParam(email, captcha, pwd)
                    )
                    if (res.success) {
                        LoggedInState.login(res.data)
                        toNext()
                    } else {
                        snackbarHostState.showSnackbar(res.msg)
                    }
                }
            },
            Modifier.width(300.dp),
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(stringResource(Res.string.register_and_login))
        }
    }
}

@Composable
private fun InputInfoStep(toHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.width(300.dp)) {
            ChangeAccountInfoView(
                PaddingValues(0.dp), onBack = toHome,
                hideCancel = true,
                saveButtonText = "进入"
            ) {
                toHome()
            }
        }
    }
}

@Composable
private fun CaptchaInputField(captcha: String, onChangeCaptcha: (String) -> Unit, modifier: Modifier = Modifier) {
    val chars = captcha.toCharArray()

    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState()
    BasicTextField(
        captcha,
        {
            it.filter { c -> c.isDigit() }.run {
                onChangeCaptcha(dropLast(max(length - 6, 0)))
            }
        },
        modifier, interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(6) {
                Box(
                    Modifier.weight(1f).aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .run {
                            if (focused.value && captcha.length == it || (it == 5 && captcha.length == 6))
                                border(2.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            else this
                        },
                    contentAlignment = Alignment.Center
                ) {
                    chars.getOrNull(it)?.let {
                        Text(it.toString())
                    }
                }
            }
        }
    }
}
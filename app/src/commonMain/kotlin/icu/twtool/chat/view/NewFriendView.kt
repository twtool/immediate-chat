package icu.twtool.chat.view

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.AcceptFriendRequestRoute
import icu.twtool.chat.components.Avatar
import icu.twtool.chat.navigation.NavRoute
import icu.twtool.chat.server.account.model.FriendRequest
import icu.twtool.chat.server.account.model.FriendRequestStatus
import icu.twtool.chat.server.account.vo.FriendRequestVO
import icu.twtool.chat.server.common.datetime.now
import icu.twtool.chat.state.NewFriendViewState
import icu.twtool.chat.theme.DisabledAlpha
import icu.twtool.chat.utils.ICBackHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

@Composable
private fun FriendRequestItem(
    request: FriendRequestVO,
    modifier: Modifier = Modifier,
    expireSecond: Int = LocalDateTime.now().second - FriendRequest.VALID_SECONDS,
    acceptRequest: () -> Unit
) {
    val alpha = remember { AnimationState(0f) }
    val offsetX = remember { AnimationState(Dp.VectorConverter, 100.dp) }
    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(1f)
        }
        launch {
            offsetX.animateTo(0.dp)
        }
    }
    Row(
        modifier.alpha(alpha.value).offset(offsetX.value).fillMaxWidth().height(IntrinsicSize.Min).clickable {}
            .padding(16.dp, 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(request.info.avatarUrl, 42.dp)

        Column(
            Modifier.fillMaxHeight().weight(1f, true),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                request.info.nickname ?: "未命名用户",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                request.msg,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = DisabledAlpha)
            )
        }
        val style = MaterialTheme.typography.labelLarge
        val width = with(LocalDensity.current) { style.fontSize.toDp() * 4 }
        if (request.status == FriendRequestStatus.REQUEST && request.createAt.second > expireSecond) {
            Button(
                acceptRequest, Modifier.widthIn(width).heightIn(24.dp, 38.dp),
                shape = MaterialTheme.shapes.extraSmall,
                contentPadding = PaddingValues(4.dp, 4.dp)
            ) {
                Text("同意", style = MaterialTheme.typography.labelMedium)
            }
        } else {
            Text(
                when (request.status) {
                    FriendRequestStatus.REQUEST -> "已过期"
                    FriendRequestStatus.ACCEPT -> "已添加"
                    FriendRequestStatus.REJECT -> "已拒绝"
                },
                Modifier.width(width),
                style = style.copy(textAlign = TextAlign.Center),
                color = LocalContentColor.current.copy(alpha = DisabledAlpha)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewFriendView(
    snackbarState: SnackbarHostState,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    navigateTo: (NavRoute) -> Unit
) {
    ICBackHandler(onBack = onBack)
    val state = remember { NewFriendViewState() }
    val expireSecond = remember { LocalDateTime.now().second - FriendRequest.VALID_SECONDS }

    LaunchedEffect(state) {
        state.loadFriendRequestList { snackbarState.showSnackbar(it) }
    }

    LazyColumn(Modifier.padding(paddingValues)) {
        items(state.friendRequestList, { it.id }) {
            FriendRequestItem(it, Modifier.animateItemPlacement(), expireSecond) {
                AcceptFriendRequestRoute.request = it
                navigateTo(AcceptFriendRequestRoute)
            }
        }
    }
}
package icu.twtool.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import icu.twtool.chat.app.NewFriendRoute
import icu.twtool.chat.navigation.NavRoute
import immediatechat.app.generated.resources.Res
import immediatechat.app.generated.resources.ic_channel
import immediatechat.app.generated.resources.ic_friend
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun FriendListItem(name: String, onClick: () -> Unit, avatar: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp, 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        avatar()
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun InternalAvatar(
    resource: DrawableResource,
    descriptionContent: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current
) {
    Box(
        modifier.then(Modifier.background(containerColor)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(resource), descriptionContent, tint = contentColor)
    }
}

@Composable
fun FriendsView(paddingValues: PaddingValues, navigateTo: (NavRoute) -> Unit) {
    val avatarModifier = Modifier.clip(MaterialTheme.shapes.extraSmall).size(42.dp)

    LazyColumn(Modifier.padding(paddingValues)) {
        item {
            FriendListItem("好友验证", { navigateTo(NewFriendRoute) }) {
                InternalAvatar(
                    Res.drawable.ic_friend, "好友验证", avatarModifier,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        item {
            FriendListItem("频道", {}) {
                InternalAvatar(
                    Res.drawable.ic_channel, "频道", avatarModifier,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
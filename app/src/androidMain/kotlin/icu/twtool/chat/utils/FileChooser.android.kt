package icu.twtool.chat.utils

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import icu.twtool.chat.io.ICFile
import icu.twtool.chat.io.ICFileImpl

class AndroidFileChooser(
    private val launcher: ManagedActivityResultLauncher<String, Uri?>
) : FileChooser {

    override suspend fun launch() {
        launcher.launch("image/*")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberFileChooser(onImageSelected: (List<ICFile>) -> Unit): FileChooser {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
            Manifest.permission.READ_MEDIA_IMAGES
        ) else listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    if (!permissionState.allPermissionsGranted) {
        LaunchedEffect(permissionState) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onImageSelected(uri?.let { listOf(ICFileImpl(context, it)) } ?: emptyList())
        }

    return AndroidFileChooser(launcher)
}
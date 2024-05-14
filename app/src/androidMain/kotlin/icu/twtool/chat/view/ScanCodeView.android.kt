package icu.twtool.chat.view

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

private const val TAG = "ScanCodeView"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraPermissionState(): PermissionState {
    val permissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (!permissionState.status.isGranted) {
        LaunchedEffect(permissionState) {
            if (!permissionState.status.shouldShowRationale)
                permissionState.launchPermissionRequest()
        }
    }

    return permissionState
}

@OptIn(ExperimentalPermissionsApi::class)
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
actual fun CameraView(onScanUID: (String) -> Unit) {
    val cameraPermissionState = rememberCameraPermissionState()

    if (!cameraPermissionState.status.isGranted) return NotGrantedView()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = {
            PreviewView(it).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.BLACK)
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_START

                controller = cameraController.also { controller ->

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                        )
                        .build()

                    val detector = BarcodeScanning.getClient(options)
                    var closed = false

                    controller.setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor()) analyze@{ imageProxy ->
                        val mediaImage = imageProxy.image ?: run { imageProxy.close();return@analyze }

                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        if (closed) return@analyze
                        detector.process(image)
                            .addOnSuccessListener { barCodes ->
                                if (barCodes.size > 0) {
                                    for (barCode in barCodes) {
                                        if (barCode.rawValue?.startsWith("UID:") == true) {
                                            onScanUID((barCode.rawValue!!.replaceFirst("UID:", "")))
                                            closed = true
                                            detector.close()
                                            return@addOnSuccessListener
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { res -> Log.d(TAG, "Error: ${res.message}") }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                    controller.bindToLifecycle(lifecycleOwner)
                }
            }
        },
        onReset = {},
        onRelease = {
            cameraController.unbind()
        }
    )
}

@Composable
private fun NotGrantedView() {

}
package icu.twtool.chat.components.file

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.min
import icu.twtool.chat.animate.VectorConverter
import icu.twtool.chat.animate.toIntSize
import icu.twtool.chat.cache.produceImageState
import icu.twtool.chat.navigation.window.calculateWindowSize
import icu.twtool.image.compose.ICAsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LookFile(file: FileRes?, onDismiss: () -> Unit) {
    if (file is ImageRes) {
        LookImageFile(file, onDismiss)
    }
}


private fun calculateOffset(widthSize: IntSize, size: IntSize, offset: IntOffset?): IntOffset {
    if (offset == null) return IntOffset.Zero
    return IntOffset(
        offset.x + size.width / 2 - widthSize.width / 2,
        offset.y + size.height / 2 - widthSize.height / 2
    )
}

@Composable
private fun LookImageFile(file: ImageRes, onDismiss: () -> Unit) {
    val windowSize = calculateWindowSize()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // 目标大小
    val targetSize = remember(file.position, windowSize) {
        (file.position?.targetSize ?: DpSize.Unspecified).let {
            if (it == DpSize.Unspecified) windowSize
            else DpSize(min(it.width, windowSize.width), min(it.height, windowSize.height))
        }
    }
    // 目标偏移
    val targetOffset = remember(windowSize, targetSize) {
        with(density) {
            val size = targetSize.toIntSize()
            windowSize.toIntSize().let {
                IntOffset(it.width / 2, it.height / 2)
                    .minus(IntOffset(size.width / 2, size.height / 2))
            }
        }
    }
    // 原始偏移量
    val originOffset = remember(file.position, windowSize) {
        file.position?.offset ?: with(density) {
            windowSize.toIntSize().let { IntOffset(it.width / 2, it.height / 2) }
        }
    }
    // 原始大小
    val originSize = remember(file.position) {
        val size = file.position?.size ?: DpSize.Unspecified
        if (size == DpSize.Unspecified) DpSize.Zero
        else size
    }

    // 透明度动画属性
    val alpha = remember(file) { Animatable(0f) }
    // 偏移动画属性
    val offset = remember(file) {
        Animatable(originOffset, IntOffset.VectorConverter)
    }
    // 大小动画属性
    val size = remember(file) {
        Animatable(originSize, DpSize.VectorConverter)
    }

    var scale by remember(file) {
        mutableStateOf(1f)
    }

    val showMax: CoroutineScope.() -> Unit = {
        launch {
            // 移动到中心
            offset.animateTo(targetOffset)
        }
        launch {
            // 缩放到最大大小
            size.animateTo(targetSize)
        }
    }

    LaunchedEffect(file) {
        delay(50)
        alpha.animateTo(1f)
    }
    LaunchedEffect(windowSize, file) {
        delay(50)
        showMax()
    }

    Box(
        Modifier.fillMaxSize()
            .alpha(alpha.value)
            .background(MaterialTheme.colorScheme.scrim.copy(0.4f))
            .onScale {
                scale = (scale + it).coerceIn(0.5f, 2f)
            }
            .pointerInput(file) {
                detectTapGestures {
                    val am1 = scope.launch {
                        offset.animateTo(originOffset)
                    }
                    val am2 = scope.launch {
                        size.animateTo(originSize)
                    }
                    val am3 = scope.launch {
                        alpha.animateTo(0f)
                    }
                    scope.launch {
                        am1.join()
                        am2.join()
                        am3.join()
                        onDismiss()
                    }
                }
            },
//        contentAlignment = Alignment.Center
    ) {
//        val painter = produceImageState(file.url, keys = arrayOf(file))
        val painter = file.painter()

        ICAsyncImage(
            painter, file.desc, null,
            Modifier
                .offset { offset.value }
                .size(size.value)
                .scale(scale),
            contentScale = ContentScale.Inside
        )
    }
}

expect fun Modifier.onScale(onChange: (Float) -> Unit): Modifier
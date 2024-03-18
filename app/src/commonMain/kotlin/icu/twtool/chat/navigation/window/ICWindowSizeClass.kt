package icu.twtool.chat.navigation.window

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp


@Immutable
class ICWindowSizeClass(
    val widthSizeClass: ICWindowWidthSizeClass,
    val heightSizeClass: ICWindowHeightSizeClass
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ICWindowSizeClass

        if (widthSizeClass != other.widthSizeClass) return false
        if (heightSizeClass != other.heightSizeClass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = widthSizeClass.hashCode()
        result = 31 * result + heightSizeClass.hashCode()
        return result
    }

    override fun toString() = "ICWindowSizeClass($widthSizeClass, $heightSizeClass)"

    companion object {

        fun calculateFromSize(
            size: DpSize,
        ): ICWindowSizeClass {
            val windowWidthSizeClass = ICWindowWidthSizeClass.fromWidth(
                size.width,
            )
            val windowHeightSizeClass = ICWindowHeightSizeClass.fromHeight(
                size.height,
            )
            return ICWindowSizeClass(windowWidthSizeClass, windowHeightSizeClass)
        }
    }
}

@Immutable
@JvmInline
value class ICWindowWidthSizeClass private constructor(private val value: Int) : Comparable<ICWindowWidthSizeClass> {

    override fun compareTo(other: ICWindowWidthSizeClass): Int =
        breakpoint().compareTo(other.breakpoint())

    override fun toString(): String {
        return "ICWindowWidthSizeClass." + when (this) {
            Compact -> "Compact"
            Medium -> "Medium"
            Expanded -> "Expanded"
            else -> ""
        }
    }

    companion object {
        val Compact = ICWindowWidthSizeClass(0)
        val Medium = ICWindowWidthSizeClass(1)
        val Expanded = ICWindowWidthSizeClass(2)

        private fun ICWindowWidthSizeClass.breakpoint(): Dp {
            return when {
                this == Expanded -> 840.dp
                this == Medium -> 600.dp
                else -> 0.dp
            }
        }

        internal fun fromWidth(
            width: Dp,
        ): ICWindowWidthSizeClass {
            require(width >= 0.dp) { "Width must not be negative" }
            if (width >= Expanded.breakpoint()) return Expanded
            if (width >= Medium.breakpoint()) return Medium
            return Compact
        }
    }
}

@Immutable
@JvmInline
value class ICWindowHeightSizeClass private constructor(val value: Int) : Comparable<ICWindowHeightSizeClass> {

    override fun compareTo(other: ICWindowHeightSizeClass): Int =
        breakpoint().compareTo(other.breakpoint())

    override fun toString(): String {
        return "ICWindowHeightSizeClass." + when (this) {
            Compact -> "Compact"
            Medium -> "Medium"
            Expanded -> "Expanded"
            else -> ""
        }
    }

    companion object {
        val Compact = ICWindowHeightSizeClass(0)
        val Medium = ICWindowHeightSizeClass(1)
        val Expanded = ICWindowHeightSizeClass(2)

        private fun ICWindowHeightSizeClass.breakpoint(): Dp {
            return when {
                this == Expanded -> 900.dp
                this == Medium -> 480.dp
                else -> 0.dp
            }
        }

        internal fun fromHeight(
            height: Dp,
        ): ICWindowHeightSizeClass {
            require(height >= 0.dp) { "Width must not be negative" }
            if (height >= Expanded.breakpoint()) return Expanded
            if (height >= Medium.breakpoint()) return Medium
            return Compact
        }
    }
}
package icu.twtool.chat.utils

import icu.twtool.chat.constants.ApplicationDir
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

fun libFileName(name: String): String = name + when (hostOs) {
    OS.Android, OS.Linux -> ".so"
    OS.Windows -> ".dll"
    OS.MacOS -> TODO()
    OS.Ios -> TODO()
    else -> TODO()
}

@Suppress("UnsafeDynamicallyLoadedCode")
fun loadLibrary(name: String) {
    System.load(ApplicationDir + "\\" + libFileName(name))
}
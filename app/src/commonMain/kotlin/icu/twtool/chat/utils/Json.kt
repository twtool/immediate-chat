package icu.twtool.chat.utils

import kotlinx.serialization.json.Json

val JSON = Json {
    // 对 Kotlin 属性的默认值进行编码
    encodeDefaults = true
    // 删除 JSON 规范限制 (RFC-4627)。允许使用带引号的布尔文字和不带引号的字符串文字。更加宽容输入中的无效值，用默认值替换它们。
    isLenient = true
    allowStructuredMapKeys = true
}
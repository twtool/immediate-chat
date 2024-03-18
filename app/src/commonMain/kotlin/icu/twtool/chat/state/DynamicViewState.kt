package icu.twtool.chat.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import icu.twtool.chat.server.dynamic.DynamicService
import icu.twtool.chat.server.dynamic.param.GetTimelinePageParam
import icu.twtool.chat.server.dynamic.vo.DynamicDetailsVO
import icu.twtool.chat.service.get
import icu.twtool.logger.getLogger

@Stable
class DynamicViewState {

    private val log = getLogger("DynamicViewState")
    private val service: DynamicService by lazy { DynamicService.get() }

    private val _data = mutableStateListOf<DynamicDetailsVO>()
    val data: List<DynamicDetailsVO> get() = _data

    private var currentPage = 0
    private var total: Long? = null

    var loading by mutableStateOf(false)
        private set
    var more by mutableStateOf(true)
        private set

    suspend fun load(refresh: Boolean = false) {
        if (!refresh && !more) return
        if (loading) return
        loading = true
        log.info("loading")

        // TODO: 需要解决可能会重复的问题（有人新发了）
        val param = GetTimelinePageParam().apply {
            currentPage = this@DynamicViewState.currentPage + 1
        }
        val res = service.getTimelines(param)
        if (res.success) {
            res.data?.let { _data.addAll(it.record) }
        } else {

        }
        loading = false
        more = data.size == (total ?: 0).toInt()
    }
}
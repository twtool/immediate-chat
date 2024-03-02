package icu.twtool.cache.compose

import icu.twtool.cache.getCache

abstract class CacheState {

    internal val cache = getCache()
}
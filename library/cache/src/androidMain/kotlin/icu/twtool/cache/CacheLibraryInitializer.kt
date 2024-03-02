package icu.twtool.cache

import android.content.Context
import androidx.startup.Initializer

class CacheLibraryInitializer : Initializer<AndroidCache> {

    override fun create(context: Context): AndroidCache {
        AndroidCache.initialize(context)
        return AndroidCache
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
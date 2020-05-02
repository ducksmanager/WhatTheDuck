package com.squareup.picasso

class PicassoCache {
    companion object {
        fun clearCache(p: Picasso) {
            p.cache.clear()
        }
    }
}
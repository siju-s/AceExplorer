package com.siju.acexplorer.common.customglide

import com.bumptech.glide.load.Key
import java.security.MessageDigest

internal class CustomKey(private val path: String) : Key {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}
    override fun equals(other: Any?): Boolean {
        return if (other is String) {
            path == other
        }
        else super.equals(other)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
package com.sarahisweird.weirdchat.data

import com.google.gson.reflect.TypeToken

data class EmojiAlphaCode(
    val alias: String,
    val page: Int,
    val row: Int,
    val column: Int,
) {
    companion object {
        val listTypeToken = object : TypeToken<List<EmojiAlphaCode>>() {}
    }
}

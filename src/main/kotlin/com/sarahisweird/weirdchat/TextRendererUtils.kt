package com.sarahisweird.weirdchat

import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text

class TextRendererUtils {
    companion object {
        @JvmStatic
        fun getTextToStylesMap(text: OrderedText): Map<String, Style> {
            val texts = mutableListOf<Pair<String, Style>>()

            text.accept { _, style, codePoint ->
                texts += if (texts.lastOrNull()?.second != style) {
                    Char(codePoint).toString() to style
                } else {
                    val last = texts.removeLast()
                    (last.first + Char(codePoint)) to style
                }

                true
            }

            return texts.toMap()
        }

        @JvmStatic
        fun couldContainEmojiShortcode(text: String) =
            Regex("(:[a-zA-Z0-9+\\-_~]+:)").find(text)

        @JvmStatic
        fun stringAsOrderedText(string: String, style: Style): OrderedText {
            val text = Text.empty()

            text.append(string)
            text.style = style

            return text.asOrderedText()
        }

        @JvmStatic
        fun findEmojiByAlias(alias: String) =
            WeirdChatClient.emojiAlphaCodes.find { it.alias == alias }
    }
}
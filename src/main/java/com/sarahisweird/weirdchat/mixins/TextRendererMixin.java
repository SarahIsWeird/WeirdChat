package com.sarahisweird.weirdchat.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sarahisweird.weirdchat.TextRendererUtils;
import com.sarahisweird.weirdchat.data.EmojiAlphaCode;
import kotlin.text.MatchResult;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {
    @Shadow
    protected abstract int draw(OrderedText text, float x, float y, int color, Matrix4f matrix4f, boolean shadow);
    
    @Inject(
            method = "drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
            at = @At("HEAD"),
            cancellable = true
    )
    public void drawWithShadow(MatrixStack matrices, OrderedText text, float x, float y, int color, CallbackInfoReturnable<Integer> cir) {
        Map<String, Style> texts = TextRendererUtils.getTextToStylesMap(text);

        float newX = drawStringStyleMapReplacingEmojiShortcodes(texts, matrices, x, y, color, true);

        cir.setReturnValue((int) newX);
    }

    @Inject(
            method = "drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
            at = @At("HEAD"),
            cancellable = true
    )
    public void drawWithShadow(MatrixStack matrices, Text text, float x, float y, int color, CallbackInfoReturnable<Integer> cir) {
        Map<String, Style> texts = TextRendererUtils.getTextToStylesMap(text.asOrderedText());

        float newX = drawStringStyleMapReplacingEmojiShortcodes(texts, matrices, x, y, color, true);

        cir.setReturnValue((int) newX);
    }

    @Inject(
            method = "drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
            at = @At("HEAD"),
            cancellable = true
    )
    public void draw(MatrixStack matrices, OrderedText text, float x, float y, int color, CallbackInfoReturnable<Integer> cir) {
        Map<String, Style> texts = TextRendererUtils.getTextToStylesMap(text);

        float newX = drawStringStyleMapReplacingEmojiShortcodes(texts, matrices, x, y, color, false);

        cir.setReturnValue((int) newX);
    }

    @Inject(
            method = "draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
            at = @At("HEAD"),
            cancellable = true
    )
    public void draw(MatrixStack matrices, Text text, float x, float y, int color, CallbackInfoReturnable<Integer> cir) {
        Map<String, Style> texts = TextRendererUtils.getTextToStylesMap(text.asOrderedText());

        float newX = drawStringStyleMapReplacingEmojiShortcodes(texts, matrices, x, y, color, false);

        cir.setReturnValue((int) newX);
    }

    private float drawStringStyleMapReplacingEmojiShortcodes(Map<String, Style> texts, MatrixStack matrices, float x, float y, int color, boolean shadow) {
        float newX = x;

        for (Map.Entry<String, Style> entry : texts.entrySet()) {
            String content = entry.getKey();
            Style style = entry.getValue();

            newX = drawStyledStringReplacingEmojiShortcodes(content, style, matrices, newX, y, color, shadow);
        }

        return newX;
    }

    private float drawStyledStringReplacingEmojiShortcodes(String content, Style style, MatrixStack matrices, float x, float y, int color, boolean shadow) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        MatchResult match = TextRendererUtils.couldContainEmojiShortcode(content);

        if (match == null) {
            OrderedText text = TextRendererUtils.stringAsOrderedText(content, style);

            return draw(text, x, y, color, positionMatrix, shadow);
        }

        EmojiAlphaCode emojiData = TextRendererUtils.findEmojiByAlias(match.getValue());

        if (emojiData == null) {
            OrderedText text = TextRendererUtils.stringAsOrderedText(content, style);

            return draw(text, x, y, color, positionMatrix, shadow);
        }

        float newX = x;

        if (match.getRange().getFirst() > 0) {
            OrderedText text = TextRendererUtils.stringAsOrderedText(content.substring(0, match.getRange().getFirst()), style);

            newX = draw(text, x, y, color, positionMatrix, shadow);
        }

        RenderSystem.setShaderTexture(0, new Identifier("weirdchat", "emoji_page_" + emojiData.getPage()));
        DrawableHelper.drawTexture(matrices, (int) newX, (int) y, 7, 7, emojiData.getColumn() * 72f, emojiData.getRow() * 72f, 72, 72, 1152, 1152);

        newX += 7;

        if (match.getRange().getLast() == content.length() - 1) {
            return newX;
        }

        return drawStyledStringReplacingEmojiShortcodes(content.substring(match.getRange().getLast() + 1), style, matrices, newX, y, color, shadow);
    }
}

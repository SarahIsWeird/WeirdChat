package com.sarahisweird.weirdchat

import com.google.gson.GsonBuilder
import com.sarahisweird.morechatsuggestions.client.MoreChatSuggestions
import com.sarahisweird.weirdchat.data.EmojiAlphaCode
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.measureTimeMillis

@Environment(EnvType.CLIENT)
object WeirdChatClient : ModInitializer {
    var emojiAlphaCodes: List<EmojiAlphaCode> = listOf()
        private set

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun onInitialize() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(dataReloadListener)
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(clientResourceReloadListener)

        logger.info("Initialized WeirdChat.")
    }

    private val dataReloadListener = object : SimpleSynchronousResourceReloadListener {
        override fun reload(manager: ResourceManager) {
            try {
                val fileId = Identifier("weirdchat", "emoji.json")
                val resource = manager.getResource(fileId).get()

                resource.reader.use {
                    val gson = GsonBuilder().setLenient().create()

                    emojiAlphaCodes = gson.fromJson(it, EmojiAlphaCode.listTypeToken.type)
                }
            } catch (e: Exception) {
                logger.error("An error occurred while trying to load the emoji alpha code resource", e)
                return
            }

            val suggestionClass = Identifier("weirdchat", "emoji")
            val suggestions = emojiAlphaCodes.map(EmojiAlphaCode::alias)

            MoreChatSuggestions.registerSuggestions(suggestionClass, suggestions) { it.startsWith(":") }

            logger.info("Registered ${suggestions.size} chat suggestions for ID \"${suggestionClass}\".")
        }

        override fun getFabricId(): Identifier {
            return Identifier("weirdchat", "emoji_codes")
        }
    }

    private val clientResourceReloadListener = object : SimpleSynchronousResourceReloadListener {
        override fun reload(manager: ResourceManager) {
            val textureManager = MinecraftClient.getInstance().textureManager
            var count = 0

            val time = measureTimeMillis {
                manager.findResources("textures/emoji") { it.path.endsWith(".png") }
                    .forEach { (id, resource) ->
                        resource.inputStream.use {
                            val textureName = id.path.removePrefix("textures/emoji/").removeSuffix(".png")
                            val textureId = Identifier(id.namespace, textureName)
                            val texture = NativeImageBackedTexture(NativeImage.read(it))

                            textureManager.registerTexture(textureId, texture)
                        }

                        count++
                    }
            }

            logger.info("Loaded $count emoji pages in ${time}ms.")
        }

        override fun getFabricId(): Identifier {
            return Identifier("weirdchat", "emoji_textures")
        }
    }
}
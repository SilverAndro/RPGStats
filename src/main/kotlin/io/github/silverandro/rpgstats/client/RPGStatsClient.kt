package io.github.silverandro.rpgstats.client

import com.mojang.blaze3d.platform.InputUtil
import io.github.silverandro.rpgstats.Constants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.option.KeyBind
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.Pair
import org.lwjgl.glfw.GLFW
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

object RPGStatsClient : ClientModInitializer {
    var nameMap = HashMap<Identifier, String>()
    var currentStats = HashMap<Identifier, Pair<Int, Int>>()
    private lateinit var openGUIKeybind: KeyBind

    override fun onInitializeClient(mod: ModContainer) {
        ClientPlayNetworking.registerGlobalReceiver(Constants.SYNC_NAMES_PACKET_ID) { _, _, byteBuf, _ ->
            // Clear data
            nameMap.clear()

            // Get the amount of stats to read
            val count = byteBuf.readInt()

            // Read each stat in turn
            for (i in 0 until count) {
                // Read the identifier
                val statId = byteBuf.readIdentifier()
                // Read the name
                val name = byteBuf.readString()
                nameMap[statId] = name
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(Constants.SYNC_STATS_PACKET_ID) { _, _, byteBuf: PacketByteBuf, _ ->
            // Clear data
            currentStats.clear()

            // Get the amount of stats to read
            val count = byteBuf.readInt()

            // Read each stat in turn
            for (i in 0 until count) {
                // Read the identifier
                val statId = byteBuf.readIdentifier()
                // Read the level and xp
                val level = byteBuf.readInt()
                val xp = byteBuf.readInt()
                currentStats[statId] = Pair(level, xp)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(Constants.OPEN_GUI) { client: MinecraftClient, handler: ClientPlayNetworkHandler?, byteBuf: PacketByteBuf?, packetSender: PacketSender? ->

        }

        openGUIKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBind(
                "key.rpgstats.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.rpgstats.keybinds"
            )
        )

        ClientTickEvents.END.register(ClientTickEvents.End { client: MinecraftClient ->
            while (openGUIKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.player?.sendMessage(buildText { color(Color.RED) { literal("Sorry, RPGStats GUI is still in development for this version") } }, false)
                }
            }
        })
    }
}
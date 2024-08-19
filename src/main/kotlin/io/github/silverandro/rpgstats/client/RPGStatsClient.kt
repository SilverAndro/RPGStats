/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.client

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.Identifier
import net.minecraft.util.Pair

object RPGStatsClient : ClientModInitializer {
    private val nameMap = HashMap<Identifier, String>()
    private val currentStats = HashMap<Identifier, Pair<Int, Int>>()
    private lateinit var openGUIKeybind: KeyBinding

    override fun onInitializeClient() {
        /*
        ClientPlayNetworking.registerGlobalReceiver(Constants.SYNC_NAMES_PACKET_ID) { _, _, byteBuf, _ ->
            // Clear data
            nameMap.clear()

            // Get the amount of stats to read
            val count = byteBuf.readInt()

            // Read each stat in turn
            for (i in 0 until count) {
                // Read the identifier
                val statId = byteBuf.readIdentifier.of()
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

        ClientPlayNetworking.registerGlobalReceiver(Constants.OPEN_GUI) { client: MinecraftClient, handler: ClientPlayNetworkHandler?, byteBuf: PacketByteBuf?, _ ->

        }

        openGUIKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.rpgstats.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.rpgstats.keybinds"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client: MinecraftClient ->
            while (openGUIKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.player?.sendMessage(buildText { color(Color.RED) { literal("Sorry, RPGStats GUI is still in development for this version") } }, false)
                }
            }
        }

         */
    }
}
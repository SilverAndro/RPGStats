/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.datadrive.stats

import io.github.silverandro.rpgstats.Constants
import io.github.silverandro.rpgstats.stats.Components
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier


object StatsManager : SimpleSynchronousResourceReloadListener {
    override fun getFabricId(): Identifier {
        return Identifier.of(Constants.MOD_ID, "stat_loader")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun reload(manager: ResourceManager) {
        Components.components.clear()

        manager.findResources("rpgstats_stats") { it.path.endsWith(".json") }.forEach { (_, resource) ->
            resource.inputStream.use {
                val map: Map<String, StatEntry> = Constants.json.decodeFromStream(it)
                map.forEach { (key, value) ->
                    val statId = Identifier.of(key)

                    if (statId.path.startsWith("_")) {
                        throw IllegalArgumentException("Attempt to register a stat ID starting with an underscore! $statId")
                    }

                    if (!value.shouldRemove) {
                        Components.components[statId] = value
                    } else {
                        Components.components.remove(statId)
                    }
                }
            }
        }
    }

    fun register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
    }
}

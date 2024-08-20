package io.github.silverandro.rpgstats.datadrive.xp

import io.github.silverandro.rpgstats.Constants
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object XPLoader : SimpleSynchronousResourceReloadListener {
    @Serializable
    data class XpDataWrapper(val values: Map<String, XpData.XpEntry>)

    override fun getFabricId(): Identifier {
        return Identifier.of(Constants.MOD_ID, "xp_loader")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun reload(manager: ResourceManager) {
        manager.findResources("attachments/minecraft") { it.path.endsWith(".json") }.forEach { (id, resource) ->
            val type = id.path.split("/").dropLast(1).last()
            resource.inputStream.use {
                val map: XpDataWrapper = Constants.json.decodeFromStream(it)

                when (type) {
                    "block" -> {
                        map.values.mapKeys { (k, _) -> Registries.BLOCK.get(Identifier.of(k)) }.forEach { block, entry ->
                            XpData.BLOCK_XP.computeIfAbsent(block) { mutableListOf() }.add(entry)
                        }
                    }
                    "entity_type" -> {
                        map.values.mapKeys { (k, _) -> Registries.ENTITY_TYPE.get(Identifier.of(k)) }.forEach { entity, entry ->
                            XpData.ENTITY_XP_OVERRIDE.computeIfAbsent(entity) { mutableListOf() }.add(entry)
                        }
                    }
                    else -> throw IllegalArgumentException("Unknown attachment type $type")
                }
            }
        }
    }

    fun register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
    }
}
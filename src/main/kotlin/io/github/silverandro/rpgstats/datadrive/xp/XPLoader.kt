package io.github.silverandro.rpgstats.datadrive.xp

import io.github.silverandro.rpgstats.Constants
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object XPLoader : SimpleSynchronousResourceReloadListener {
    override fun getFabricId(): Identifier {
        return Identifier.of(Constants.MOD_ID, "xp_loader")
    }

    override fun reload(manager: ResourceManager) {


        manager.findResources("attachments/minecraft") { it.path.endsWith(".json") }.forEach { (_, resource) ->
            println(resource)
            resource.inputStream.use {

            }
        }
    }

    fun register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
    }
}
package io.github.silverandro.rpgstats.datadrive.stats

import io.github.silverandro.rpgstats.Constants
import io.github.silverandro.rpgstats.datadrive.findAllResources
import io.github.silverandro.rpgstats.stats.Components
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.quiltmc.qsl.resource.loader.api.ResourceLoader
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object StatsManager: IdentifiableResourceReloader {
    override fun getQuiltId(): Identifier {
        return Identifier("rpgstats", "stat_loader")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun reload(
        synchronizer: ResourceReloader.Synchronizer,
        manager: ResourceManager,
        prepareProfiler: Profiler,
        applyProfiler: Profiler,
        prepareExecutor: Executor,
        applyExecutor: Executor
    ): CompletableFuture<Void> {
        return synchronizer.whenPrepared(Unit).thenRun {
            Components.components.clear()
            manager.findAllResources("rpgstats_stats").forEach { (id, list) ->
                Constants.LOG.info("Loading stats file $id")
                list.forEach {
                    it.open().use {
                        val map: Map<String, StatEntry> = Constants.json.decodeFromStream(it)
                        map.forEach { (key, value) ->
                            val statId = Identifier(key)
                            if (!value.shouldRemove) {
                                Components.components[statId] = value
                            } else {
                                Components.components.remove(statId)
                            }
                        }
                    }
                }
            }
        }
    }

    fun register() {
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(this)
    }
}

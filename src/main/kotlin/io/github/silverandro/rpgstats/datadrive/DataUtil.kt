package io.github.silverandro.rpgstats.datadrive

import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier


internal fun ResourceManager.findAllResources(s: String): MutableMap<Identifier, MutableList<Resource>> {
    return findAllResources(s) {true}
}
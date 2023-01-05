/*
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.github.silverandro.rpgstats.mixin_logic;

import io.github.silverandro.rpgstats.LevelUtils;
import io.github.silverandro.rpgstats.RPGStatsMain;
import io.github.silverandro.rpgstats.stats.Components;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.HoeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class OnSneakLogic {
    public static void doLogic(boolean isSneaking, ServerPlayerEntity playerEntity) {
        Random random = new Random();

        // Check if all conditions met
        if (isSneaking && playerEntity.getMainHandStack().getItem() instanceof HoeItem && new Random().nextBoolean()) {
            int level = LevelUtils.INSTANCE.getComponentLevel(Components.FARMING, playerEntity);
            int amount = 0;

            if (level >= 50 && RPGStatsMain.levelConfig.farming.enableLv50Buff) {
                amount = 5;
            } else if (level >= 25 && RPGStatsMain.levelConfig.farming.enableLv25Buff) {
                amount = 3;
            }

            // If we should do stuff
            // In a 3x3 or 5x5 area, there is a 90% chance to grow any Fertilizable blocks
            if (amount > 0) {
                World world = playerEntity.world;
                BlockPos blockPos = playerEntity.getBlockPos();
                for (int y = -1; y <= 1; y++) {
                    for (int x = -amount; x <= amount; x++) {
                        for (int z = -amount; z <= amount; z++) {
                            BlockPos nextPos = blockPos.add(x, y, z);
                            BlockState bs = world.getBlockState(nextPos);
                            if (bs.getBlock() instanceof Fertilizable) {
                                if (random.nextDouble() > 0.9) {
                                    ((Fertilizable) bs.getBlock()).grow((ServerWorld) world, world.random, nextPos, bs);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
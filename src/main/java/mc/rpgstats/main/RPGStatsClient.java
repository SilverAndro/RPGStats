package mc.rpgstats.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashMap;

public class RPGStatsClient implements ClientModInitializer {
    public static HashMap<Identifier, Pair<Integer, Integer>> currentStats = new HashMap<>();
    
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.SYNC_STATS_PACKET_ID, (client, handler, byteBuf, packetSender) -> {
            // Read data
    
            // Get the amount of stats to read
            int count = byteBuf.readInt();
    
            // Read each stat in turn
            for (int i = 0; i < count; i++) {
                Identifier statIdent = byteBuf.readIdentifier();
                int level = byteBuf.readInt();
                int xp = byteBuf.readInt();
                currentStats.put(statIdent, new Pair<>(level, xp));
            }
        });
    }
}

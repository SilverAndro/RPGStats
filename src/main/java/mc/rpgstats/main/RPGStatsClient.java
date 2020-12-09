package mc.rpgstats.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashMap;

public class RPGStatsClient implements ClientModInitializer {
    public static HashMap<Identifier, Pair<Integer, Integer>> currentStats = new HashMap<>();
    
    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(RPGStats.SYNC_STATS_PACKET_ID,
            (packetContext, attachedData) -> {
                // Read data
                
                // Get the amount of stats to read
                int count = attachedData.readInt();
                
                // Read each stat in turn
                for (int i = 0; i < count; i++) {
                    Identifier statIdent = attachedData.readIdentifier();
                    int level = attachedData.readInt();
                    int xp = attachedData.readInt();
                    currentStats.put(statIdent, new Pair<>(level, xp));
                }
            }
        );
    }
}

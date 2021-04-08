package mc.rpgstats.main;

import mc.rpgstats.client.screen.RPGStatDisplayGUI;
import mc.rpgstats.client.screen.RPGStatDisplayScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class RPGStatsClient implements ClientModInitializer {
    public static HashMap<Identifier, String> nameMap = new HashMap<>();
    
    public static HashMap<Identifier, Pair<Integer, Integer>> currentStats = new HashMap<>();
    
    private static KeyBinding openGUIKeybind;
    
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.SYNC_NAMES_PACKET_ID, (client, handler, byteBuf, packetSender) -> {
            // Clear data
            nameMap.clear();
        
            // Get the amount of stats to read
            int count = byteBuf.readInt();
        
            // Read each stat in turn
            for (int i = 0; i < count; i++) {
                // Read the identifier
                Identifier statId = byteBuf.readIdentifier();
                // Read the name
                String name = byteBuf.readString();
                
                nameMap.put(statId, name);
            }
        });
        
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.SYNC_STATS_PACKET_ID, (client, handler, byteBuf, packetSender) -> {
            // Clear data
            currentStats.clear();
            
            // Get the amount of stats to read
            int count = byteBuf.readInt();
    
            // Read each stat in turn
            for (int i = 0; i < count; i++) {
                // Read the identifier
                Identifier statId = byteBuf.readIdentifier();
                // Read the level and xp
                int level = byteBuf.readInt();
                int xp = byteBuf.readInt();
                
               currentStats.put(statId, new Pair<>(level, xp));
            }
        });
    
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.OPEN_GUI, (client, handler, byteBuf, packetSender) -> {
            client.openScreen(new RPGStatDisplayScreen(new RPGStatDisplayGUI()));
        });
        
        openGUIKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rpgstats.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.rpgstats.keybinds"
        ));
    
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGUIKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.openScreen(new RPGStatDisplayScreen(new RPGStatDisplayGUI()));
                }
            }
        });
    }
}

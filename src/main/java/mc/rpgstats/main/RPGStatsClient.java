package mc.rpgstats.main;

import com.mojang.blaze3d.platform.InputUtil;
import mc.rpgstats.client.screen.RPGStatDisplayGUI;
import mc.rpgstats.client.screen.RPGStatDisplayScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBind;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.HashMap;

public class RPGStatsClient implements ClientModInitializer {
    public static HashMap<Identifier, String> nameMap = new HashMap<>();
    
    public static HashMap<Identifier, Pair<Integer, Integer>> currentStats = new HashMap<>();
    
    private static KeyBind openGUIKeybind;
    
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
        
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.OPEN_GUI, (client, handler, byteBuf, packetSender) -> client.send(() -> client.setScreen(new RPGStatDisplayScreen(new RPGStatDisplayGUI()))));
        
        openGUIKeybind = KeyBindingHelper.registerKeyBinding(new KeyBind(
            "key.rpgstats.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.rpgstats.keybinds"
        ));
        
        ClientTickEvents.END.register(client -> {
            while (openGUIKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new RPGStatDisplayScreen(new RPGStatDisplayGUI()));
                }
            }
        });
    }
}
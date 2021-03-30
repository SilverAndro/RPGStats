package mc.rpgstats.main;

import mc.rpgstats.client.screen.RPGStatDisplayGUI;
import mc.rpgstats.client.screen.RPGStatDisplayScreen;
import mc.rpgstats.component.IStatComponent;
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
import java.util.Optional;

public class RPGStatsClient implements ClientModInitializer {
    public static HashMap<IStatComponent, Pair<Integer, Integer>> currentStats = new HashMap<>();
    
    private static KeyBinding openGUIKeybind;
    
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RPGStats.SYNC_STATS_PACKET_ID, (client, handler, byteBuf, packetSender) -> {
            // Clear data
            currentStats.clear();
            
            // Get the amount of stats to read
            int count = byteBuf.readInt();
    
            // Read each stat in turn
            for (int i = 0; i < count; i++) {
                // Read the identifier
                Identifier statIdent = byteBuf.readIdentifier();
                // Read the level and xp
                int level = byteBuf.readInt();
                int xp = byteBuf.readInt();
    
                Optional<? extends IStatComponent> possible = RPGStats.statFromID(statIdent).maybeGet(client.player);
                possible.ifPresent(iStatComponent -> currentStats.put(iStatComponent, new Pair<>(level, xp)));
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

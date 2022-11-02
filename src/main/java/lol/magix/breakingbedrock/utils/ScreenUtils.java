package lol.magix.breakingbedrock.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.text.Text;

/**
 * Utility class for handling screen rendering.
 */
public interface ScreenUtils {
    /**
     * Displays the disconnection screen.
     * Call from {@link MinecraftClient#execute(Runnable)}
     */
    static void disconnect(Text reason) {
        var client = MinecraftClient.getInstance();
        var screen = new DisconnectedScreen(client.currentScreen,
                Text.translatable("disconnect.closed"), reason);
        client.disconnect(screen);
    }
}

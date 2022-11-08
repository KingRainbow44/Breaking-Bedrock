package lol.magix.breakingbedrock.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

/**
 * Utility methods for interacting with the user's device.
 */
public interface DeviceUtils {
    /**
     * Copies the specified content to the user's clipboard.
     * @param content The content to copy.
     */
    static void copyToClipboard(String content) {
        MinecraftClient.getInstance().keyboard.setClipboard(content);
    }

    /**
     * Attempts to open the specified URI in the user's default browser.
     * @param url The URI to open.
     */
    static void openUrl(String url) {
        Util.getOperatingSystem().open(url);
    }
}

package lol.magix.breakingbedrock.utils;

import com.nukkitx.network.util.DisconnectReason;
import com.sun.net.httpserver.HttpServer;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.Pair;
import lombok.SneakyThrows;
import net.minecraft.text.Text;
import tech.xigam.express.Express;
import tech.xigam.express.Router;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.Desktop.Action;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Utility class for network-related functions.
 */
public interface NetworkUtils {
    /**
     * Finds an open port on the local machine.
     * Uses Minecraft's internal method + a fallback.
     * @return A port number. (1-65535)
     */
    static int getOpenPort() {
        var port = net.minecraft.client.util.NetworkUtils.findLocalPort();
        return port == 25564 ? 2021 : port;
    }

    /**
     * Converts a {@link DisconnectReason} to a text object.
     * @param reason The reason to convert.
     * @return A friendly reason for disconnecting.
     */
    static Text getDisconnectReason(DisconnectReason reason) {
        return switch (reason) {
            case CLOSED_BY_REMOTE_PEER -> Text.translatable("disconnect.genericReason", "Connection closed by remote peer");
            case SHUTTING_DOWN -> Text.translatable("disconnect.genericReason", "Server closed");
            case DISCONNECTED -> Text.translatable("disconnect.disconnected");
            case TIMED_OUT -> Text.translatable("disconnect.timeout");
            case CONNECTION_REQUEST_FAILED -> Text.translatable("connect.failed");
            case ALREADY_CONNECTED -> Text.translatable("disconnect.genericReason", "Already connected to this server");
            case NO_FREE_INCOMING_CONNECTIONS -> Text.translatable("disconnect.genericReason", "Unable to connect to server");
            case INCOMPATIBLE_PROTOCOL_VERSION -> Text.translatable("disconnect.genericReason", "Outdated server!");
            case IP_RECENTLY_CONNECTED -> Text.translatable("disconnect.loginFailedInfo", "Connection throttled!");
            case BAD_PACKET -> Text.translatable("disconnect.genericReason", "Bad packet");
        };
    }

    /**
     * Returns a time offset from the current time.
     * The offset is specific to Windows.
     * @return The current time in milliseconds, offset to adjust for Windows servers.
     */
    static long getOffsetTimestamp() {
        return (Instant.now().getEpochSecond() + 11644473600L) * 10000000L;
    }

    /**
     * Generates a proof key from a public key.
     * @return A proof key's X and Y affine.
     */
    static Pair<String, String> getProofKey(ECPublicKey key) {
        var w = key.getW();
        // Get the X & Y affine.
        var xAffine = w.getAffineX();
        var yAffine = w.getAffineY();
        // Convert to byte arrays.
        var xBytes = EncodingUtils.longToBytes(xAffine);
        var yBytes = EncodingUtils.longToBytes(yAffine);
        // Encode to Base64.
        var encoder = Base64.getUrlEncoder().withoutPadding();
        var xEncoded = encoder.encodeToString(xBytes);
        var yEncoded = encoder.encodeToString(yBytes);
        // Return the pair.
        return new Pair<>(xEncoded, yEncoded);
    }

    /**
     * Parses and validates a response.
     * @param request The request to send.
     */
    static void checkResponse(HttpsURLConnection request) {
        try {
            // Check the response code.
            var responseCode = request.getResponseCode();
            if (responseCode != 200) {
                // Throw an exception.
                System.out.println("Response code: " + responseCode);
                // throw new RuntimeException("Invalid response code: " + responseCode);
            }

            // Check the response message.
            var responseMessage = request.getResponseMessage();
            System.out.println(responseMessage);
        } catch (Exception ignored) {
            BreakingBedrock.getLogger().warn("Unable to check response");
        }
    }

    /**
     * Directs the browser to go to a URL.
     * When the URL is visited, it waits for a callback on the local machine.
     * The callback parameter is then passed to the provided consumer.
     * @param url The URL to visit.
     * @param callback The callback parameter.
     */
    @SneakyThrows
    static void awaitCallback(String url, int port, Consumer<String> callback) {
        // Start a basic HTTP server.
        var httpServer = HttpServer.create();
        var express = new Express(new InetSocketAddress(port));
        var router = new Router();
        router.get("/callback", request -> {
            // Get the callback parameter.
            var callbackParam = request.getArgument("callback");
            // Call the consumer.
            callback.accept(callbackParam);

            // Return a response.
            request.respond("You can close this window now.");
            // Stop the server.
            httpServer.stop(0);
        });

        express.router(router);
        express.hook(httpServer);
        express.listen();

        // Direct the browser to the URL.
        if (!Desktop.isDesktopSupported()) return;

        var desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.BROWSE)) return;

        desktop.browse(new URI(url));
    }
}
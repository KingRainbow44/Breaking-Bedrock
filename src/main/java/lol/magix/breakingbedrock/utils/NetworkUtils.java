package lol.magix.breakingbedrock.utils;

import com.google.common.net.HostAndPort;
import com.sun.net.httpserver.HttpServer;
import io.netty.buffer.Unpooled;
import lol.magix.breakingbedrock.objects.Pair;
import lombok.SneakyThrows;
import net.minecraft.text.Text;
import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import tech.xigam.express.Express;
import tech.xigam.express.Router;

import java.awt.*;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.*;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Utility class for network-related functions.
 */
public interface NetworkUtils {
    short[] OFFLINE_DATA = {0x00, 0xFF, 0xFF, 0x00, 0xFE, 0xFE, 0xFE, 0xFE, 0xFD, 0xFD, 0xFD, 0xFD, 0x12, 0x34, 0x56, 0x78};

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
     * Converts a {@link RakDisconnectReason} to a text object.
     * @param reason The reason to convert.
     * @return A friendly reason for disconnecting.
     */
    static Text getDisconnectReason(RakDisconnectReason reason) {
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

    /**
     * Pings a Bedrock server.
     *
     * @param address The address to ping.
     * @return The server's response.
     */
    static BedrockPong pingServer(HostAndPort address)
            throws IOException {
        // Prepare the socket.
        var socket = new DatagramSocket();
        var socketAddress = new InetSocketAddress(
                address.getHost(), address.getPort());

        // Write the ping data.
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x01);
        buffer.writeLong(System.currentTimeMillis() / 1000L);
        for (var bit : OFFLINE_DATA) {
            buffer.writeByte(bit);
        }
        buffer.writeLong(2);

        // Send the ping.
        var input = buffer.array();
        socket.send(new DatagramPacket(input,
                buffer.writerIndex(), socketAddress));
        // Receive the pong.
        var output = new byte[4096];
        var received = new DatagramPacket(output, output.length);
        socket.receive(received);

        // Skip the first 35 bytes.
        var pongBuffer = Unpooled.wrappedBuffer(
                received.getData(), 35, received.getLength() - 35);

        // Parse the pong.
        var pong = BedrockPong.fromRakNet(pongBuffer);
        // Release the buffer.
        buffer.release();
        pongBuffer.release();

        return pong;
    }
}

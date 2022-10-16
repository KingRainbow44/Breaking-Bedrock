package lol.magix.breakingbedrock.network.packets.login;

import com.google.gson.JsonParser;
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.EncodingUtils;

@Translate(PacketType.BEDROCK)
public final class S2CHandshakeTranslator extends Translator<ServerToClientHandshakePacket> {
    @Override
    public Class<ServerToClientHandshakePacket> getPacketClass() {
        return ServerToClientHandshakePacket.class;
    }

    @Override
    public void translate(ServerToClientHandshakePacket packet) {
        try {
            // Decode the JWT token.
            var jwt = packet.getJwt().split("\\.");
            var header = EncodingUtils.base64Decode(jwt[0]);
            var payload = EncodingUtils.base64Decode(jwt[1]);
            // Parse the objects.
            var headerObject = JsonParser.parseString(header).getAsJsonObject();
            var payloadObject = JsonParser.parseString(payload).getAsJsonObject();

            // Create an encryption key from the payload.
            var salt = EncodingUtils.base64Decode(payloadObject.get("salt").getAsString());
            var privateKey = this.bedrockClient.getAuthentication().getPrivateKey();
            var publicKey = EncryptionUtils.generateKey(headerObject.get("x5u").getAsString());
            var secretKey = EncryptionUtils.getSecretKey(privateKey, publicKey, salt.getBytes());
            // Set the encryption key.
            BedrockNetworkClient.getSession().enableEncryption(secretKey);

            if (this.shouldLog)
                this.logger.info("Handshake with server successful.");
        } catch (Exception exception) {
            this.logger.warn("Failed to decode handshake packet.", exception);
        }

        // Send handshake response packet.
        var response = new ClientToServerHandshakePacket();
        this.bedrockClient.sendPacket(response, true);
    }
}

package lol.magix.breakingbedrock.network.packets.login;

import com.nukkitx.protocol.bedrock.packet.NetworkSettingsPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.translation.Translator;

@Translate
public final class NetworkSettingsTranslator extends Translator<NetworkSettingsPacket> {
    @Override
    public Class<NetworkSettingsPacket> getPacketClass() {
        return NetworkSettingsPacket.class;
    }

    @Override
    public void translate(NetworkSettingsPacket packet) {
        var session = BedrockNetworkClient.getSession();

        // Set compression values.
        session.setCompressionLevel(packet.getCompressionThreshold());
        session.setCompression(packet.getCompressionAlgorithm());

        this.logger.info("enabled compression");

        // Log in to sever.
        this.bedrockClient.loginToServer();
    }
}

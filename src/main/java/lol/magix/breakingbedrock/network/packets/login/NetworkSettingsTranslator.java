package lol.magix.breakingbedrock.network.packets.login;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket;

@Translate(PacketType.BEDROCK)
public final class NetworkSettingsTranslator extends Translator<NetworkSettingsPacket> {
    @Override
    public Class<NetworkSettingsPacket> getPacketClass() {
        return NetworkSettingsPacket.class;
    }

    @Override
    public void translate(NetworkSettingsPacket packet) {
        var session = BedrockNetworkClient.getHandle();

        // Set compression values.
        session.setCompression(packet.getCompressionAlgorithm());
        session.setCompressionLevel(NetworkConstants.COMPRESSION_LEVEL);

        // Log in to sever.
        this.bedrockClient.loginToServer();
    }
}

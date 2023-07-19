package lol.magix.breakingbedrock.network.packets.bedrock.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.SkinTranslator;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;

@Translate(PacketType.BEDROCK)
public final class PlayerSkinTranslator extends Translator<PlayerSkinPacket> {
    @Override
    public Class<PlayerSkinPacket> getPacketClass() {
        return PlayerSkinPacket.class;
    }

    @Override
    public void translate(PlayerSkinPacket packet) {
        SkinTranslator.addSerializedSkin(
                packet.getUuid(), packet.getSkin());
    }
}

package lol.magix.breakingbedrock.network.packets.player;

import com.nukkitx.protocol.bedrock.packet.UpdatePlayerGameTypePacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

@Translate(PacketType.BEDROCK)
public final class UpdatePlayerGameTypeTranslator extends Translator<UpdatePlayerGameTypePacket> {
    @Override
    public Class<UpdatePlayerGameTypePacket> getPacketClass() {
        return UpdatePlayerGameTypePacket.class;
    }

    @Override
    public void translate(UpdatePlayerGameTypePacket packet) {
        var gameMode = ConversionUtils.convertBedrockGameMode(packet.getGameType());
        this.javaClient().processPacket(new GameStateChangeS2CPacket(
                GameStateChangeS2CPacket.GAME_MODE_CHANGED, gameMode.getId()));
    }
}

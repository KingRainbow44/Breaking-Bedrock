package lol.magix.breakingbedrock.network.packets.bedrock.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.world.GameMode;
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;

@Translate(PacketType.BEDROCK)
public final class SetPlayerGameTypeTranslator extends Translator<SetPlayerGameTypePacket> {
    @Override
    public Class<SetPlayerGameTypePacket> getPacketClass() {
        return SetPlayerGameTypePacket.class;
    }

    @Override
    public void translate(SetPlayerGameTypePacket packet) {
        var gameMode = switch (packet.getGamemode()) {
            case 0 -> GameMode.SURVIVAL;
            case 1 -> GameMode.CREATIVE;
            case 2 -> GameMode.ADVENTURE;
            case 3 -> GameMode.SPECTATOR;
            default -> throw new IllegalArgumentException(
                    "Invalid game mode: " + packet.getGamemode());
        };
        this.javaClient().processPacket(new GameStateChangeS2CPacket(
                GameStateChangeS2CPacket.GAME_MODE_CHANGED, gameMode.getId()));
    }
}

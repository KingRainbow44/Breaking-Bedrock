package lol.magix.breakingbedrock.network.packets.bedrock.scoreboard;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard.UpdateMode;
import org.cloudburstmc.protocol.bedrock.packet.SetScorePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetScorePacket.Action;

import java.util.Objects;

@Translate(PacketType.BEDROCK)
public final class SetScoreTranslator extends Translator<SetScorePacket> {
    @Override
    public Class<SetScorePacket> getPacketClass() {
        return SetScorePacket.class;
    }

    @Override
    public void translate(SetScorePacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var action = packet.getAction();

        var scoreboard = world.getScoreboard();
        for (var entry : packet.getInfos()) {
            var objective = scoreboard.getObjective(
                    entry.getObjectiveId());
            if (objective == null) continue;

            this.javaClient().processPacket(new ScoreboardPlayerUpdateS2CPacket(
                    action == Action.SET ? UpdateMode.CHANGE : UpdateMode.REMOVE,
                    entry.getObjectiveId(),
                    Objects.requireNonNullElse(entry.getName(), ""),
                    15 - entry.getScore()
            ));
        }
    }
}

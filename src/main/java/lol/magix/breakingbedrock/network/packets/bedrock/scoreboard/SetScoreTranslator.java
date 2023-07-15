package lol.magix.breakingbedrock.network.packets.bedrock.scoreboard;

import lol.magix.breakingbedrock.BreakingBedrock;
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
        var holder = this.bedrockClient.getScoreboardHolder();

        for (var entry : packet.getInfos()) {
            // Get the objective from the Java scoreboard.
            var objective = scoreboard.getObjective(entry.getObjectiveId());
            if (objective == null) continue;

            // Get the objective from the Bedrock scoreboard.
            var container = holder.getScoreboard(entry.getObjectiveId());
            if (container == null) continue;

            // Try to resolve a previous name.
            var name = entry.getName();
            if (name == null) {
                name = container.getId2Name().get(entry.getScoreboardId());
            }

            // Set new name.
            if (action == Action.SET && name != null) {
                container.getId2Name().put(entry.getScoreboardId(), name);
            }

            BreakingBedrock.getLogger().warn("Setting score of {} (scoreboard ID: {}) for scoreboard objective {} with target player {}.",
                    entry.getScore(), entry.getScoreboardId(), entry.getObjectiveId(), name);

            this.javaClient().processPacket(new ScoreboardPlayerUpdateS2CPacket(
                    action == Action.SET ? UpdateMode.CHANGE : UpdateMode.REMOVE,
                    entry.getObjectiveId(),
                    Objects.requireNonNullElse(name, ""),
                    15 - entry.getScore()
            ));
        }
    }
}

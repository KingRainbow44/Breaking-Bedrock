package lol.magix.breakingbedrock.network.packets.bedrock.scoreboard;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveObjectivePacket;

@Translate(PacketType.BEDROCK)
public final class RemoveObjectiveTranslator extends Translator<RemoveObjectivePacket> {
    @Override
    public Class<RemoveObjectivePacket> getPacketClass() {
        return RemoveObjectivePacket.class;
    }

    @Override
    public void translate(RemoveObjectivePacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var scoreboard = world.getScoreboard();

        var objectiveId = packet.getObjectiveId();
        var objective = scoreboard.getObjective(objectiveId);
        if (objective == null) return;

        // Remove the objective from the world.
        scoreboard.removeObjective(objective);
        // Handle the objective removal on the client.
        this.javaClient().processPacket(new ScoreboardObjectiveUpdateS2CPacket(
                objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE
        ));
    }
}

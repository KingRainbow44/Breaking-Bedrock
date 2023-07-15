package lol.magix.breakingbedrock.network.packets.bedrock.scoreboard;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.game.scoreboards.ScoreboardContainer;
import lol.magix.breakingbedrock.game.scoreboards.ScoreboardHolder;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.SetDisplayObjectivePacket;

@Translate(PacketType.BEDROCK)
public final class SetDisplayObjectiveTranslator extends Translator<SetDisplayObjectivePacket> {
    @Override
    public Class<SetDisplayObjectivePacket> getPacketClass() {
        return SetDisplayObjectivePacket.class;
    }

    @Override
    public void translate(SetDisplayObjectivePacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var scoreboard = world.getScoreboard();
        var holder = this.bedrockClient.getScoreboardHolder();

        var objectiveId = packet.getObjectiveId();
        var displaySlot = switch (packet.getDisplaySlot()) {
            default -> throw new RuntimeException("Unknown display slot: " + packet.getDisplaySlot());
            case "list" -> Scoreboard.LIST_DISPLAY_SLOT_ID;
            case "sidebar" -> Scoreboard.SIDEBAR_DISPLAY_SLOT_ID;
            case "belowname" -> Scoreboard.BELOW_NAME_DISPLAY_SLOT_ID;
        };

        var objective = scoreboard.getObjective(objectiveId);
        if (objective == null) {
            objective = scoreboard.addObjective(objectiveId,
                    ScoreboardCriterion.create(packet.getCriteria()),
                    Text.literal(packet.getDisplayName()),
                    ScoreboardCriterion.RenderType.INTEGER);
            scoreboard.setObjectiveSlot(displaySlot, objective);

            // Add the scoreboard to the holder.
            var bedrockObjective = ScoreboardContainer.of(objectiveId);
            holder.addScoreboard(bedrockObjective);

            // Set the scoreboard holder's scoreboard.
            switch (displaySlot) {
                case Scoreboard.LIST_DISPLAY_SLOT_ID -> holder.setPlayerList(bedrockObjective);
                case Scoreboard.SIDEBAR_DISPLAY_SLOT_ID -> holder.setSidebar(bedrockObjective);
                case Scoreboard.BELOW_NAME_DISPLAY_SLOT_ID -> holder.setBelowName(bedrockObjective);
            }
        }

        // Show the scoreboard to the player.
        this.javaClient().processPacket(new ScoreboardDisplayS2CPacket(
                displaySlot, objective
        ));
    }
}

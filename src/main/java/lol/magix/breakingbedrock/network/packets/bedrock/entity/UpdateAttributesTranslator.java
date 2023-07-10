package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;

@Translate(PacketType.BEDROCK)
public final class UpdateAttributesTranslator extends Translator<UpdateAttributesPacket> {
    @Override
    public Class<UpdateAttributesPacket> getPacketClass() {
        return UpdateAttributesPacket.class;
    }

    @Override
    public void translate(UpdateAttributesPacket packet) {
        var player = this.player();
        if (player == null) return;

        var runtimeId = packet.getRuntimeEntityId();
        if (runtimeId != player.getId()) return;

        this.run(() -> {
            var health = 20f;
            var food = 20;
            var saturation = 5f;
            var level = 0;
            var experience = 0f;

            for (var attribute : packet.getAttributes()) {
                switch (attribute.getName()) {
                    case "minecraft:health" -> health = attribute.getValue();
                    case "minecraft:player.hunger" -> food = (int) attribute.getValue();
                    case "minecraft:player.saturation" -> saturation = attribute.getValue();
                    case "minecraft:player.level" -> level = (int) attribute.getValue();
                    case "minecraft:player.experience" -> experience = attribute.getValue();
                    case "minecraft:absorption" -> {
                        player.setAbsorptionAmount(attribute.getValue());

                        // TODO: Update the entity tracker.
                        // this.javaClient().processPacket(new EntityTrackerUpdateS2CPacket(
                        //         runtimeId, List.of(DataTracker.Entry)
                        // ));
                    }
                }
            }

            // Send the client packets to update the attributes.
            this.javaClient().processPacket(new HealthUpdateS2CPacket(
                    health, food, saturation));
            this.javaClient().processPacket(new ExperienceBarUpdateS2CPacket(
                    experience, UpdateAttributesTranslator.total(level), level
            ));
        });
    }

    /**
     * Calculates the total amount of experience required to reach the given level.
     * <a href="https://minecraft.gamepedia.com/Experience#Leveling_up">Source</a>
     *
     * @param xpLevel The level to reach.
     * @return The total amount of experience required to reach the given level.
     */
    private static int total(int xpLevel) {
        if (xpLevel <= 16) {
            return MathHelper.square(xpLevel) + 6 * xpLevel;
        } else if (xpLevel <= 31) {
            return (int) (2.5 * MathHelper.square(xpLevel) - 40.5 * xpLevel + 360);
        }

        return (int) (4.5 * MathHelper.square(xpLevel) - 162.5 * xpLevel + 2220);
    }
}

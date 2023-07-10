package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;

@Translate(PacketType.BEDROCK)
public final class AnimateTranslator extends Translator<AnimatePacket> {
    @Override
    public Class<AnimatePacket> getPacketClass() {
        return AnimatePacket.class;
    }

    @Override
    public void translate(AnimatePacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var entity = world.getEntityById(
                (int) packet.getRuntimeEntityId());
        if (entity == null) return;

        var action = switch (packet.getAction()) {
            default -> -1;
            case WAKE_UP -> EntityAnimationS2CPacket.WAKE_UP;
            case SWING_ARM -> EntityAnimationS2CPacket.SWING_MAIN_HAND;
            case CRITICAL_HIT -> EntityAnimationS2CPacket.CRIT;
            case MAGIC_CRITICAL_HIT -> EntityAnimationS2CPacket.ENCHANTED_HIT;
        }; if (action == -1) return;

        this.javaClient().processPacket(
                    new EntityAnimationS2CPacket(entity, action));
    }
}

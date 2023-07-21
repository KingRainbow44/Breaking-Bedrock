package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket;

@Translate(PacketType.BEDROCK)
public final class EntityEventTranslator extends Translator<EntityEventPacket> {
    @Override
    public Class<EntityEventPacket> getPacketClass() {
        return EntityEventPacket.class;
    }

    @Override
    public void translate(EntityEventPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var entity = world.getEntityById(
                (int) packet.getRuntimeEntityId());
        if (entity == null) return;

        switch (packet.getType()) {
            case ATTACK_START -> this.javaClient().processPacket(
                    new EntityAnimationS2CPacket(entity, EntityAnimationS2CPacket.SWING_MAIN_HAND));
            case HURT -> this.javaClient().processPacket(
                    new EntityDamageS2CPacket(entity, world.getDamageSources().generic()));
            case DEATH -> this.javaClient().processPacket(
                    new EntityStatusS2CPacket(entity, EntityStatuses.ADD_DEATH_PARTICLES));
        }
    }
}

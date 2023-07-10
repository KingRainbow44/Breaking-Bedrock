package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket;

@Translate(PacketType.BEDROCK)
public final class MobEffectTranslator extends Translator<MobEffectPacket> {
    @Override
    public Class<MobEffectPacket> getPacketClass() {
        return MobEffectPacket.class;
    }

    @Override
    public void translate(MobEffectPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var statusEffect = StatusEffect.byRawId(packet.getEffectId());
        if (statusEffect == null) return;

        var amplifier = packet.getAmplifier();
        var instance = new StatusEffectInstance(statusEffect,
                packet.getDuration(), amplifier, false,
                packet.isParticles(), true);

        this.run(() -> {
            var entity = world.getEntityById(
                    (int) packet.getRuntimeEntityId());
            if (!(entity instanceof LivingEntity livingEntity)) return;

            var event = packet.getEvent();
            var attributes = livingEntity.getAttributes();

            switch (event) {
                case ADD, MODIFY -> {
                    var changed = livingEntity.addStatusEffect(instance);
                    if (changed) {
                        if (event == MobEffectPacket.Event.MODIFY)
                            statusEffect.onRemoved(livingEntity, attributes, amplifier);
                        statusEffect.onApplied(livingEntity, attributes, amplifier);
                    }
                }
                case REMOVE -> {
                    var removed = livingEntity.removeStatusEffect(statusEffect);
                    if (removed)
                        statusEffect.onRemoved(livingEntity, attributes, amplifier);
                }
            }
        });
    }
}

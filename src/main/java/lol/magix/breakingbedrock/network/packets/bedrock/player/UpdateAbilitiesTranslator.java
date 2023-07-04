package lol.magix.breakingbedrock.network.packets.bedrock.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;

@Translate(PacketType.BEDROCK)
public final class UpdateAbilitiesTranslator extends Translator<UpdateAbilitiesPacket> {
    @Override
    public Class<UpdateAbilitiesPacket> getPacketClass() {
        return UpdateAbilitiesPacket.class;
    }

    @Override
    public void translate(UpdateAbilitiesPacket packet) {
        if (packet.getUniqueEntityId() != this.data().getRuntimeId())
            return;

        var abilities = new PlayerAbilities();
        for (var layer : packet.getAbilityLayers()) {
            var values = layer.getAbilityValues();
            for (var ability : layer.getAbilitiesSet()) {
                var enabled = values.contains(ability);

                switch (ability) {
                    case FLY_SPEED -> {
                        if (enabled) abilities.setFlySpeed(layer.getFlySpeed());
                    }
                    case WALK_SPEED -> {
                        if (enabled) abilities.setWalkSpeed(layer.getWalkSpeed());
                    }
                    case MAY_FLY -> abilities.allowFlying = enabled;
                    case FLYING -> abilities.flying = enabled;
                    case INVULNERABLE -> abilities.invulnerable = enabled;
                }
            }

            abilities.allowModifyWorld =
                    values.contains(Ability.BUILD) &&
                            values.contains(Ability.MINE);
        }

        this.javaClient().processPacket(
                new PlayerAbilitiesS2CPacket(abilities));
    }
}

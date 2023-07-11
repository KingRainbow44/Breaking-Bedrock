package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket;

@Translate(PacketType.BEDROCK)
public final class SetEntityMotionTranslator extends Translator<SetEntityMotionPacket> {
    @Override
    public Class<SetEntityMotionPacket> getPacketClass() {
        return SetEntityMotionPacket.class;
    }

    @Override
    public void translate(SetEntityMotionPacket packet) {
        var client = this.client();
        var world = client.world;
        if (world == null) return;

        var runtimeId = (int) packet.getRuntimeEntityId();
        var velocity = GameUtils.convert(packet.getMotion());

        this.javaClient().processPacket(
                new EntityVelocityUpdateS2CPacket(runtimeId, velocity));
    }
}

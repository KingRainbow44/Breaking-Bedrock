package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;

@Translate(PacketType.BEDROCK)
public final class RemoveEntityTranslator extends Translator<RemoveEntityPacket> {
    @Override
    public Class<RemoveEntityPacket> getPacketClass() {
        return RemoveEntityPacket.class;
    }

    @Override
    public void translate(RemoveEntityPacket packet) {
        var uniqueId = (int) packet.getUniqueEntityId();
        this.javaClient().processPacket(new EntitiesDestroyS2CPacket(uniqueId));
    }
}

package lol.magix.breakingbedrock.network.packets.world;

import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@Translate(PacketType.BEDROCK)
public final class SetTimeTranslator extends Translator<SetTimePacket> {
    @Override
    public Class<SetTimePacket> getPacketClass() {
        return SetTimePacket.class;
    }

    @Override
    public void translate(SetTimePacket packet) {
        var timePacket = new WorldTimeUpdateS2CPacket(packet.getTime(),
                packet.getTime(), true);
        this.javaClient().processPacket(timePacket);
    }
}

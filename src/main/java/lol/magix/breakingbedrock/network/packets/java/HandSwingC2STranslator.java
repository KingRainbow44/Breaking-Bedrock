package lol.magix.breakingbedrock.network.packets.java;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket.Action;

@Translate(PacketType.JAVA)
public final class HandSwingC2STranslator extends Translator<HandSwingC2SPacket> {
    @Override
    public Class<HandSwingC2SPacket> getPacketClass() {
        return HandSwingC2SPacket.class;
    }

    @Override
    public void translate(HandSwingC2SPacket packet) {
        if (this.player() == null) return;

        var animatePacket = new AnimatePacket();
        animatePacket.setAction(Action.SWING_ARM);
        animatePacket.setRuntimeEntityId(this.data().getRuntimeId());
        this.bedrockClient.sendPacket(animatePacket);
    }
}

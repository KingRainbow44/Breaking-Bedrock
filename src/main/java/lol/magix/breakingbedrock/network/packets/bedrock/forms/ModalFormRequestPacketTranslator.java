package lol.magix.breakingbedrock.network.packets.bedrock.forms;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.FormTranslator;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;

@Translate(PacketType.BEDROCK)
public final class ModalFormRequestPacketTranslator extends Translator<ModalFormRequestPacket> {
    @Override
    public Class<ModalFormRequestPacket> getPacketClass() {
        return ModalFormRequestPacket.class;
    }

    @Override
    public void translate(ModalFormRequestPacket packet) {
        FormTranslator.translate(packet);
    }
}

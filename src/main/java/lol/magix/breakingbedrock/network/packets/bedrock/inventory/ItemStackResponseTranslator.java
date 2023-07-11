package lol.magix.breakingbedrock.network.packets.bedrock.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponse;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackResponsePacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Translate(PacketType.BEDROCK)
public final class ItemStackResponseTranslator extends Translator<ItemStackResponsePacket> {
    public static final Map<Integer, Consumer<ItemStackResponse>> HANDLERS = new ConcurrentHashMap<>();

    @Override
    public Class<ItemStackResponsePacket> getPacketClass() {
        return ItemStackResponsePacket.class;
    }

    @Override
    public void translate(ItemStackResponsePacket packet) {
        for (var response : packet.getEntries()) {
            var handler = HANDLERS.get(response.getRequestId());
            if (handler != null) handler.accept(response);
        }
    }
}

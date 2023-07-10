package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket;

@Translate(PacketType.BEDROCK)
public final class TakeItemEntityTranslator extends Translator<TakeItemEntityPacket> {
    @Override
    public Class<TakeItemEntityPacket> getPacketClass() {
        return TakeItemEntityPacket.class;
    }

    @Override
    public void translate(TakeItemEntityPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var collectorId = (int) packet.getRuntimeEntityId();
        var stackEntityId = (int) packet.getItemRuntimeEntityId();

        // Get the item stack entity.
        var stackEntity = world.getEntityById(stackEntityId);
        if (!(stackEntity instanceof ItemEntity itemStack)) return;

        this.javaClient().processPacket(new ItemPickupAnimationS2CPacket(
                stackEntityId, collectorId, (int) Math.floor(itemStack.getStack().getCount() / 64f)
        ));
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;

@Translate(PacketType.BEDROCK)
public final class SetEntityDataTranslator extends Translator<SetEntityDataPacket> {
    @Override
    public Class<SetEntityDataPacket> getPacketClass() {
        return SetEntityDataPacket.class;
    }

    @Override
    public void translate(SetEntityDataPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var runtimeId = (int) packet.getRuntimeEntityId();

        this.run(() -> {
            var entity = world.getEntityById(runtimeId);
            if (entity == null) return;

            EntityMetadataTranslator.translate(
                    new Pair<>(entity, packet.getMetadata()));

            this.javaClient().processPacket(new EntityTrackerUpdateS2CPacket(
                    entity.getId(), entity.getDataTracker().getChangedEntries()
            ));
        });
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket;

@Translate(PacketType.BEDROCK)
public final class AddItemEntityTranslator extends Translator<AddItemEntityPacket> {
    @Override
    public Class<AddItemEntityPacket> getPacketClass() {
        return AddItemEntityPacket.class;
    }

    @Override
    public void translate(AddItemEntityPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var uniqueId = (int) packet.getUniqueEntityId();

        this.run(() -> {
            var entity = EntityType.ITEM.create(world);
            if (entity == null) return;

            // Set entity properties.
            entity.setId(uniqueId);
            entity.setPosition(WorldUtils.convert(packet.getPosition()));
            entity.setVelocity(WorldUtils.convert(packet.getMotion()));

            // Spawn the entity on the client.
            this.javaClient().processPacket(entity.createSpawnPacket());

            // Update the entity's metadata.
            EntityMetadataTranslator.translate(new Pair<>(entity, packet.getMetadata()));
            this.javaClient().processPacket(new EntityTrackerUpdateS2CPacket(
                    entity.getId(), entity.getDataTracker().getChangedEntries()
            ));

            this.javaClient().processPacket(entity.createSpawnPacket());
        });
    }
}

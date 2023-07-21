package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
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
            entity.setPosition(GameUtils.convert(packet.getPosition()));
            entity.setVelocity(GameUtils.convert(packet.getMotion()));
            entity.setStack(ItemTranslator.bedrock2Java(packet.getItemInHand()));

            // Spawn the entity on the client.
            this.javaClient().processPacket(entity.createSpawnPacket());

            // Update the entity's metadata.
            EntityMetadataTranslator.translate(new Pair<>(entity, packet.getMetadata()));

            var entries = entity.getDataTracker().getChangedEntries();
            if (entries != null) this.javaClient().processPacket(
                    new EntityTrackerUpdateS2CPacket(entity.getId(), entries));

            this.javaClient().processPacket(entity.createSpawnPacket());
        });
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;

@Translate(PacketType.BEDROCK)
public final class AddEntityTranslator extends Translator<AddEntityPacket> {
    @Override
    public Class<AddEntityPacket> getPacketClass() {
        return AddEntityPacket.class;
    }

    @Override
    public void translate(AddEntityPacket packet) {
        var type = EntityTranslator.bedrock2Entity
                .get(packet.getIdentifier());
        if (type == null) {
            this.logger.error("Invalid entity type: {}.", packet.getIdentifier());
            return;
        }

        // Create the entity on the client.
        var entityId = (int) packet.getUniqueEntityId();
        var rotation = packet.getRotation();

        this.run(() -> {
            var entity = type.create(this.client().world);
            if (entity == null) {
                this.logger.error("Failed to create entity: {}.", type);
                return;
            }

            // Set entity properties.
            entity.setId(entityId);
            entity.setPosition(GameUtils.convert(packet.getPosition()));
            entity.setPitch(rotation.getX());
            entity.setYaw(rotation.getY());
            entity.setHeadYaw(packet.getHeadRotation());
            entity.setVelocity(GameUtils.convert(packet.getMotion()));

            // Spawn the entity on the client.
            this.javaClient().processPacket(entity.createSpawnPacket());

            // Update the entity's metadata.
            EntityMetadataTranslator.translate(new Pair<>(entity, packet.getMetadata()));

            var changedEntries = entity.getDataTracker().getChangedEntries();
            if (changedEntries != null) this.javaClient().processPacket(
                    new EntityTrackerUpdateS2CPacket(entity.getId(),changedEntries));
        });
    }
}

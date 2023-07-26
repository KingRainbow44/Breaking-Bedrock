package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinTntEntity;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
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
            this.logger.warn("Invalid entity type: {}.", packet.getIdentifier());
            type = EntityType.ZOMBIE;
        }

        // Create the entity on the client.
        var entityId = (int) packet.getUniqueEntityId();
        var rotation = packet.getRotation();

        final var typeF = type;
        this.run(() -> {
            var world = this.client().world;
            if (world == null) {
                this.logger.error("Failed to create entity: {}.", typeF);
                return;
            }

            var entity = typeF.create(world);
            if (entity == null) {
                this.logger.error("Failed to create entity: {}.", typeF);
                return;
            }

            // Set entity properties.
            entity.setId(entityId);
            entity.setPosition(GameUtils.convert(packet.getPosition()));
            entity.setPitch(rotation.getX());
            entity.setYaw(rotation.getY());
            entity.setHeadYaw(packet.getHeadRotation());
            entity.setVelocity(GameUtils.convert(packet.getMotion()));

            // Check if the entity has an owner.
            var ownerId = packet.getMetadata().get(EntityDataTypes.OWNER_EID);
            if (ownerId != null) {
                var owner = world.getEntityById(Math.toIntExact(ownerId));

                // This handles most entities which have owners.
                // Some mobs also have owners, they should be added here when implemented.
                if (entity instanceof ProjectileEntity projectile)
                    projectile.setOwner(owner);
                else if (owner instanceof LivingEntity livingOwner &&
                        entity instanceof IMixinTntEntity tnt)
                    tnt.setCausingEntity(livingOwner);
            }

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

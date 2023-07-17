package lol.magix.breakingbedrock.game.nametag;

import com.google.common.base.Preconditions;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.utils.TextUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Data
@ToString(exclude = {"entity", "handle"})
public final class NameTagLine {
    private final NameTag handle;
    @NonNull private Text text;

    private boolean spawned = false;
    private ArmorStandEntity entity;
    private Vec3d position;

    /**
     * Sets the text object from a raw string.
     *
     * @param rawText The raw string to set the text object to.
     */
    public void setText(String rawText) {
        this.text = TextUtils.translate(rawText);
        if (this.entity != null && this.spawned) {
            this.entity.setCustomName(this.getText());
        }
    }

    /**
     * Sets the position of this line.
     *
     * @param position The position to set.
     */
    public void setPosition(Vec3d position) {
        this.position = position;
        if (this.entity != null && this.spawned) {
            this.entity.setPosition(position);

            // Force the client to update the entity position.
            BedrockNetworkClient.getInstance().getJavaNetworkClient()
                    .processPacket(new EntityPositionS2CPacket(this.entity));
        }
    }

    /**
     * Shows this line.
     *
     * @param world The world to show this line in.
     */
    public void show(World world) {
        // Check if the entity is already spawned.
        if (this.entity != null || this.spawned) return;

        // Create the entity instance.
        this.entity = EntityType.ARMOR_STAND.create(world);
        Preconditions.checkNotNull(this.entity);

        // Set entity properties.
        this.entity.setNoGravity(true);
        this.entity.setInvisible(true);
        this.entity.setInvulnerable(true);
        this.entity.setPosition(this.getPosition());
        this.entity.setCustomName(this.getText());
        this.entity.setCustomNameVisible(true);

        // Spawn the entity to the client.
        var client = BedrockNetworkClient.getInstance().getJavaNetworkClient();
        if (client != null) {
            client.processPacket(this.entity.createSpawnPacket());

            // Update the entity's metadata.
            client.processPacket(new EntityTrackerUpdateS2CPacket(
                    entity.getId(), entity.getDataTracker().getChangedEntries()
            ));
        }

        this.spawned = true;
    }

    /**
     * Removes this line.
     */
    public void hide() {
        if (this.entity == null) return;

        // Remove the entity from the client.
        var client = BedrockNetworkClient.getInstance().getJavaNetworkClient();
        if (client != null) client.processPacket(
                new EntitiesDestroyS2CPacket(this.entity.getId()));

        // Remove the entity from the world.
        this.entity.kill();
        this.entity = null;

        this.spawned = false;
    }
}

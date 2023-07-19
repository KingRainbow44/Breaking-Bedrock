package lol.magix.breakingbedrock.game.nametag;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.utils.IntervalUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Manages the name tags of players & entities. */
public final class NameTagManager {
    private static final Map<Integer, NameTag> nameTags
            = new ConcurrentHashMap<>();

    /**
     * Initializes the name tag manager.
     */
    public static void initialize() {
        // Create a ticking thread.
        new Thread(() -> {
            while (true) {
                IntervalUtils.sleep(100);
                NameTagManager.tick();
            }
        }).start();
    }

    /**
     * Updates an entity's position.
     *
     * @param entity The entity to update.
     */
    public static void updateEntity(Entity entity) {
        if (entity instanceof PlayerEntity) return;

        var nameTag = nameTags.get(entity.getId());
        if (nameTag == null) return;

        // Update the position of the name tag.
        nameTag.setPosition(entity.getPos());
    }

    /**
     * Removes an entity from the name tag manager.
     *
     * @param entity The entity to remove.
     */
    public static void removeEntity(Entity entity) {
        if (entity instanceof PlayerEntity) return;

        var nameTag = nameTags.remove(entity.getId());
        if (nameTag == null) return;

        // Hide the name tag.
        nameTag.hide();
    }

    /**
     * Sets the name of an entity.
     *
     * @param entity The entity to set the name of.
     * @param name The name to set.
     */
    public static void setName(Entity entity, String name) {
        if (entity instanceof PlayerEntity) return;

        nameTags.computeIfAbsent(entity.getId(),
                        k -> new NameTag(entity))
                .setNameTag(name);
    }

    /**
     * Run every 100ms.
     */
    public static void tick() {
        // Check if there are any name tags.
        if (nameTags.isEmpty()) return;

        // Check if the client is connected.
        var client = BedrockNetworkClient.getInstance();
        if (!client.isConnected()) {
            // Remove all name tags.
            nameTags.forEach((entity, nameTag) -> nameTag.hide());
            nameTags.clear();
            return;
        }

        // Update all name tags.
        nameTags.forEach((entityId, nameTag) -> {
            var entity = nameTag.getEntity();

            if (!entity.isAlive() && nameTag.isDisplayed()) {
                nameTag.hide(); return;
            } else if (entity.isAlive() && !nameTag.isDisplayed()) {
                nameTag.show(); return;
            }

            if (entity.isSpectator() && nameTag.isDisplayed()) {
                nameTag.hide(); return;
            } else if (!entity.isSpectator() && !nameTag.isDisplayed()) {
                nameTag.show(); return;
            }

            if (entity.isSneaking() && nameTag.isDisplayed()) {
                nameTag.hide(); return;
            } else if (!entity.isSneaking() && !nameTag.isDisplayed()) {
                nameTag.show(); return;
            }
        });
    }
}

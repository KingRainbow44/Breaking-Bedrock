package lol.magix.breakingbedrock.game.containers;

import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Basic container implementation. */
public abstract class Container {
    protected Map<Integer, ItemData> items = new HashMap<>();
    @Getter protected int size;

    /**
     * Creates an empty container.
     *
     * @param size The size of the container.
     */
    public Container(int size) {
        this.size = size;
        for (var i = 0; i < size; i++)
            this.items.put(i, ItemData.AIR);
    }

    /**
     * Sets an item in the container.
     *
     * @param slot The Bedrock slot ID.
     * @param item The item data.
     */
    public void setBedrockItem(int slot, ItemData item) {
        this.items.put(slot, item);
    }

    /**
     * Fetches an item from the container.
     *
     * @param slot The Bedrock slot ID.
     * @return The item data.
     */
    public ItemData getItem(int slot) {
        return this.items.get(slot);
    }

    /**
     * @return The contents of the container.
     */
    public List<ItemData> getContents() {
        return List.copyOf(this.items.values());
    }

    /**
     * @param bedrockSlotId The slot ID in Bedrock.
     * @return The slot ID in Java.
     */
    public abstract int getJavaSlotId(int bedrockSlotId);

    /**
     * @param javaSlotId The slot ID in Java.
     * @return The slot ID in Bedrock.
     */
    public abstract int getBedrockSlotId(int javaSlotId);

    /**
     * @return If the inventory is static.
     */
    public abstract boolean isStatic();

    /**
     * When invoked, the inventory should be synced and updated.
     */
    public abstract void updateInventory();
}

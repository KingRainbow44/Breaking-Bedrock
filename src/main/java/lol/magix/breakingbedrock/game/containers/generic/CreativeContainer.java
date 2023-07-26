package lol.magix.breakingbedrock.game.containers.generic;

import lol.magix.breakingbedrock.game.containers.Container;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class CreativeContainer extends Container {
    private final List<ItemData> items = new LinkedList<>();
    private final Map<String, ItemData> itemMap = new HashMap<>();

    /**
     * Creates a container with all registered creative items.
     */
    public CreativeContainer() {
        super(0);
    }

    @Override
    public List<ItemData> getContents() {
        return this.items;
    }

    @Override
    public ItemData getItem(int slot) {
        return this.items.get(slot);
    }

    /**
     * Performs a lookup of the specified item.
     *
     * @param identifier The item identifier.
     * @return The item.
     */
    public int getItem(String identifier) {
        var item = this.itemMap.get(identifier);
        if (item == null) throw new RuntimeException("Invalid item " + identifier);

        return item.getNetId();
    }

    @Override
    public void setBedrockItem(int slot, ItemData item) {
        // This inventory cannot be updated.
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return bedrockSlotId;
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return javaSlotId;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void updateInventory() {
        this.itemMap.clear();

        // Update the item map.
        for (var item : this.items) {
            this.itemMap.put(item.getDefinition().getIdentifier(), item);
        }
    }
}

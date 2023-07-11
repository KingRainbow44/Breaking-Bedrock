package lol.magix.breakingbedrock.game.containers;

import lol.magix.breakingbedrock.game.containers.player.ArmorContainer;
import lol.magix.breakingbedrock.game.containers.player.CursorContainer;
import lol.magix.breakingbedrock.game.containers.player.InventoryContainer;
import lol.magix.breakingbedrock.game.containers.player.OffhandContainer;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerContainerHolder {
    private final Map<Integer, Container> containers
            = new ConcurrentHashMap<>();

    @Getter private int currentContainerId = 0;

    public PlayerContainerHolder() {
        this.containers.put(ContainerId.INVENTORY, new InventoryContainer());
        this.containers.put(ContainerId.OFFHAND, new OffhandContainer());
        this.containers.put(ContainerId.ARMOR, new ArmorContainer());
        this.containers.put(ContainerId.UI, new CursorContainer());
    }

    /**
     * @param container The container ID.
     * @return The container.
     */
    public Container getContainer(int container) {
        return this.containers.get(container);
    }

    /**
     * @param type The container type.
     * @return The container.
     */
    public Container getContainer(ContainerSlotType type) {
        return this.getContainer(ConversionUtils.typeToContainer(
                type, this.getCurrentContainerId()));
    }

    /**
     * @return The inventory container.
     */
    public Container getInventory() {
        return this.getContainer(ContainerId.INVENTORY);
    }

    /**
     * @return The offhand container.
     */
    public Container getOffhand() {
        return this.getContainer(ContainerId.OFFHAND);
    }

    /**
     * @return The armor container.
     */
    public Container getArmor() {
        return this.getContainer(ContainerId.ARMOR);
    }

    /**
     * @return The cursor container.
     */
    public Container getCursor() {
        return this.getContainer(ContainerId.UI);
    }

    /**
     * @return The currently open container.
     */
    public Container getCurrentContainer() {
        return this.getContainer(this.currentContainerId);
    }

    /**
     * Sets the currently open container.
     *
     * @param containerId The container ID.
     * @param current The currently open container.
     */
    public void setOpenContainer(int containerId, Container current) {
        if (current != null) {
            if (!current.isStatic()) {
                this.containers.put(containerId, current);
            }

            this.currentContainerId = containerId;
            return;
        }

        var container = this.getCurrentContainer();
        if (container != null) {
            if (!container.isStatic()) {
                this.containers.remove(containerId);
            }
        }

        this.currentContainerId = -1;
    }

    /**
     * @return The containers.
     */
    public Map<Integer, Container> getContainers() {
        return Collections.unmodifiableMap(this.containers);
    }
}

package lol.magix.breakingbedrock.game.containers.action;

import lol.magix.breakingbedrock.game.containers.Container;
import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.Collections;
import java.util.List;

public final class ReadOnlyContainer extends Container {
    /**
     * Wrap a container in a read-only container.
     *
     * @param container The container to wrap.
     * @return The read-only container.
     */
    public static ReadOnlyContainer wrap(Container container) {
        return new ReadOnlyContainer(container);
    }

    @Getter private final Container wrapped;

    /**
     * Create a new read-only container.
     *
     * @param container The container to wrap.
     */
    public ReadOnlyContainer(Container container) {
        super(container.getSize());

        this.wrapped = container;
    }

    @Override
    public void setBedrockItem(int slot, ItemData item) {
        throw new UnsupportedOperationException("Container is read-only.");
    }

    @Override
    public ItemData getItem(int slot) {
        return this.getWrapped().getItem(slot)
                .toBuilder().build();
    }

    @Override
    public List<ItemData> getContents() {
        return Collections.unmodifiableList(
                this.getWrapped().getContents());
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return this.getWrapped().getJavaSlotId(bedrockSlotId);
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return this.getWrapped().getBedrockSlotId(javaSlotId);
    }

    @Override
    public boolean isStatic() {
        return this.getWrapped().isStatic();
    }

    @Override
    public void updateInventory() {
        this.getWrapped().updateInventory();
    }
}

package lol.magix.breakingbedrock.game.containers.action;

import lol.magix.breakingbedrock.game.containers.Container;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource;

import java.util.*;

public final class ActionBuilder {
    private final InventorySource source;
    private final Container container;
    private final Map<Integer, ItemData> actions;

    ActionBuilder(InventorySource source, Container container, Map<Integer, ItemData> actions) {
        this.source = source;
        this.container = container;
        this.actions = actions;
    }

    public List<InventoryActionData> execute() {
        var actions = new ArrayList<InventoryActionData>();

        var container = this.container;
        if (container instanceof ReadOnlyContainer readOnlyContainer) {
            container = readOnlyContainer.getWrapped();
        }

        for(var entry : this.actions.entrySet()) {
            actions.add(new InventoryActionData(
                    this.source, entry.getKey(),
                    container.getItem(entry.getKey()), entry.getValue()));
            container.setBedrockItem(entry.getKey(), entry.getValue());
        }

        return actions;
    }

    public void revert() {
        this.container.updateInventory();
    }

    public static ActionBuilderBuilder builder() {
        return new ActionBuilderBuilder();
    }

    public static class ActionBuilderBuilder {
        private InventorySource source;
        private Container container;
        private final Map<Integer, ItemData> actions = new HashMap<>();

        ActionBuilderBuilder() { }

        public ActionBuilderBuilder container(InventorySource source, Container container) {
            this.source = source;
            this.container = container;
            return this;
        }

        public Container wrapped() {
            return this.container;
        }

        public ActionBuilderBuilder action(int slot, ItemData action) {
            this.actions.put(slot, action);
            return this;
        }

        public ActionBuilderBuilder clearActions() {
            this.actions.clear();
            return this;
        }

        public ActionBuilder build() {
            if (this.container == null) {
                throw new NullPointerException("container");
            }

            var actions = Collections.unmodifiableMap(this.actions);
            return new ActionBuilder(this.source, this.container, actions);
        }
    }
}

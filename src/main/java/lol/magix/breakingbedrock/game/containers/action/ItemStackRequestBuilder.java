package lol.magix.breakingbedrock.game.containers.action;

import lol.magix.breakingbedrock.game.containers.Container;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.packets.bedrock.inventory.ItemStackResponseTranslator;
import lol.magix.breakingbedrock.objects.Triplet;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequestSlotData;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.PlaceAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.SwapAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.TakeAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponseStatus;

import java.util.*;

@RequiredArgsConstructor
public final class ItemStackRequestBuilder {
    /**
     * @param requestId The item stack request ID.
     * @return A new instance of {@link ItemStackRequestBuilder}.
     */
    public static ItemStackRequestBuilder builder(int requestId) {
        return new ItemStackRequestBuilder(requestId);
    }

    private Runnable callback = () -> {};
    private boolean shouldUpdate = true;

    private final List<ItemStackRequestAction> actions = new LinkedList<>();
    private final List<Triplet<Container, Integer, ItemData>> handleActions = new LinkedList<>();
    private final List<Triplet<Container, Integer, ItemData>> revertActions = new LinkedList<>();

    private final int requestId;

    /**
     * Sets the callback to be called when the request is completed.
     *
     * @param callback The callback.
     * @return This instance.
     */
    public ItemStackRequestBuilder callback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Sets whether the client should update the container.
     *
     * @param shouldUpdate Whether the client should update the container.
     * @return This instance.
     */
    public ItemStackRequestBuilder update(boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
        return this;
    }

    /**
     * Takes items from the source slot to the destination slot.
     *
     * @param amount The amount of items to take.
     * @param source The source container.
     * @param sourceType The source container type.
     * @param sourceSlot The source slot.
     * @param dest The destination container.
     * @param destType The destination container type.
     * @param destSlot The destination slot.
     * @return This instance.
     */
    public ItemStackRequestBuilder take(int amount,
                                        Container source, ContainerSlotType sourceType, int sourceSlot,
                                        Container dest, ContainerSlotType destType, int destSlot) {
        // Get the item instances.
        var destItem = dest.getItem(destSlot);
        var sourceItem = source.getItem(sourceSlot);

        // Create the action.
        this.actions.add(new TakeAction(amount,
                new ItemStackRequestSlotData(sourceType, sourceSlot, sourceItem.getNetId()),
                new ItemStackRequestSlotData(destType, destSlot, destItem.getNetId())));

        // Add the actions to the containers.
        this.handleActions.add(new Triplet<>(source, sourceSlot, destItem));
        this.handleActions.add(new Triplet<>(dest, destSlot, sourceItem));
        return this;
    }

    /**
     * Places items from the source slot to the destination slot.
     *
     * @param amount The amount of items to place.
     * @param source The source container.
     * @param sourceType The source container type.
     * @param sourceSlot The source slot.
     * @param dest The destination container.
     * @param destType The destination container type.
     * @param destSlot The destination slot.
     * @return This instance.
     */
    public ItemStackRequestBuilder place(int amount,
                                         Container source, ContainerSlotType sourceType, int sourceSlot,
                                         Container dest, ContainerSlotType destType, int destSlot) {
        // Get the item instances.
        var destItem = dest.getItem(destSlot);
        var sourceItem = source.getItem(sourceSlot);

        // Create the action.
        this.actions.add(new PlaceAction(amount,
                new ItemStackRequestSlotData(sourceType, sourceSlot, sourceItem.getNetId()),
                new ItemStackRequestSlotData(destType, destSlot, destItem.getNetId())));

        // Add the actions to the containers.
        this.handleActions.add(new Triplet<>(source, sourceSlot, destItem));
        this.handleActions.add(new Triplet<>(dest, destSlot, sourceItem));
        return this;
    }

    /**
     * Swaps two items in the inventory.
     *
     * @param first The first item.
     * @param firstType The first item container type.
     * @param firstSlot The first item slot.
     * @param second The second item.
     * @param secondType The second item container type.
     * @param secondSlot The second item slot.
     * @return This instance.
     */
    public ItemStackRequestBuilder swap(Container first, ContainerSlotType firstType, int firstSlot,
                                        Container second, ContainerSlotType secondType, int secondSlot) {
        // Get the item instances.
        var firstItem = first.getItem(firstSlot);
        var secondItem = second.getItem(secondSlot);

        // Create the actions.
        this.actions.add(new SwapAction(
                new ItemStackRequestSlotData(firstType, firstSlot, firstItem.getNetId()),
                new ItemStackRequestSlotData(secondType, secondSlot, secondItem.getNetId())
        ));

        // Add the actions to the containers.
        this.handleActions.add(new Triplet<>(first, firstSlot, secondItem));
        this.handleActions.add(new Triplet<>(second, secondSlot, firstItem));
        return this;
    }

    /**
     * Runs pending container actions.
     * This is used to update the container state before sending the request.
     *
     * @return This instance.
     */
    public ItemStackRequestBuilder update() {
        // Run pending actions.
        for (var action : this.handleActions) {
            var container = action.a();
            var slot = action.b();
            var newItem = action.c();

            // Save the old item state.
            var oldItem = container.getItem(slot);
            this.revertActions.add(new Triplet<>(container, slot, oldItem));

            // Set the new item.
            container.setBedrockItem(slot, newItem);
        }

        // Clear pending actions.
        this.handleActions.clear();

        return this;
    }

    /**
     * Builds all item stack requests.
     * Handles each container action.
     *
     * @return The item stack requests.
     */
    public ItemStackRequestAction[] execute() {
        // Check if containers need to be updated.
        if (!this.handleActions.isEmpty())
            this.update();

        // Register a response handler.
        var containers = BedrockNetworkClient.getInstance().getContainerHolder();
        ItemStackResponseTranslator.HANDLERS.put(this.requestId, response -> {
            if (response.getResult() != ItemStackResponseStatus.OK) {
                // Revert the item changes.
                for (var previous : this.revertActions) {
                    var container = previous.a();
                    var slot = previous.b();
                    var item = previous.c();

                    container.setBedrockItem(slot, item);
                }
            } else for (var result : response.getContainers()) {
                var container = containers.getContainer(result.getContainer());
                if (container == null) continue;

                // Update the container.
                for (var item : result.getItems()) {
                    var slot = item.getSlot() == 0 ?
                            item.getHotbarSlot() : item.getSlot();
                    var newItem = container.getItem(slot).toBuilder()
                            .usingNetId(true)
                            .netId(item.getStackNetworkId())
                            .count(item.getCount())
                            .build();
                    container.setBedrockItem(slot, newItem);
                }

                if (this.shouldUpdate) container.updateInventory();
            }

            // Invoke the callback.
            this.callback.run();
            // Remove the handler.
            ItemStackResponseTranslator.HANDLERS.remove(this.requestId);
        });

        return this.actions.toArray(new ItemStackRequestAction[0]);
    }
}

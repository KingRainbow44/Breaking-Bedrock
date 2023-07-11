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

    private final List<ItemStackRequestAction> actions = new LinkedList<>();
    private final List<Triplet<Container, Integer, ItemData>> handleActions = new LinkedList<>();

    private final int requestId;

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

        System.out.println("place");
        System.out.println("Destination network ID: " + destItem.getNetId());
        System.out.println("Source network ID: " + sourceItem.getNetId());

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
     * Builds all item stack requests.
     * Handles each container action.
     *
     * @return The item stack requests.
     */
    public ItemStackRequestAction[] execute() {
        // Handle actions.
        var oldState = new LinkedList<Triplet<Container, Integer, ItemData>>();
        for (var action : this.handleActions) {
            var container = action.a();
            var slot = action.b();
            var newItem = action.c();

            // Save the old item state.
            var oldItem = container.getItem(slot);
            oldState.add(new Triplet<>(container, slot, oldItem));

            // Set the new item.
            container.setBedrockItem(slot, newItem);
        }

        // Register a response handler.
        var containers = BedrockNetworkClient.getInstance().getContainerHolder();
        ItemStackResponseTranslator.HANDLERS.put(this.requestId, response -> {
            if (response.getResult() != ItemStackResponseStatus.OK) {
                // Revert the item changes.
                for (var previous : oldState) {
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

                container.updateInventory();
            }

            // Remove the handler.
            ItemStackResponseTranslator.HANDLERS.remove(this.requestId);
        });

        return this.actions.toArray(new ItemStackRequestAction[0]);
    }
}

package lol.magix.breakingbedrock.network.packets.java.inventory;

import lol.magix.breakingbedrock.game.containers.PlayerContainerHolder;
import lol.magix.breakingbedrock.game.containers.action.ActionBuilder;
import lol.magix.breakingbedrock.game.containers.action.ActionBuilder.ActionBuilderBuilder;
import lol.magix.breakingbedrock.game.containers.action.ReadOnlyContainer;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.translators.screen.ScreenHandlerTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

import java.util.List;
import java.util.function.Function;

// @Translate(PacketType.JAVA)
public final class ClickSlotC2STranslator extends Translator<ClickSlotC2SPacket> {
    @Override
    public Class<ClickSlotC2SPacket> getPacketClass() {
        return ClickSlotC2SPacket.class;
    }

    @Override
    public void translate(ClickSlotC2SPacket packet) {
        var transactionPacket = new InventoryTransactionPacket();
        transactionPacket.setTransactionType(InventoryTransactionType.NORMAL);

        Function<Integer, ActionBuilderBuilder> container = id -> ActionBuilder.builder()
                .container(InventorySource.fromContainerWindowId(id),
                        ReadOnlyContainer.wrap(this.containers().getContainer(id)));
        var actions = switch (packet.getActionType()) {
            default -> {
                this.logger.debug("Unhandled slot action: " + packet.getActionType());
                yield null;
            }
            case PICKUP -> ClickSlotC2STranslator.pickup(packet, container);
            case SWAP -> ClickSlotC2STranslator.swap(packet, container);
        };

        // Check if the actions are null.
        if (actions == null) {
            // Revert inventory changes.
            ClickSlotC2STranslator.revert(this.containers());
            return;
        }

        // Add all actions to the transaction packet.
        transactionPacket.getActions().addAll(actions.stream()
                .flatMap(builder -> builder.execute().stream())
                .toList());

        this.bedrockClient.sendPacket(transactionPacket);
    }

    /**
     * Translates the pickup action.
     * This is when a player places an item into the cursor.
     *
     * @param packet The packet.
     * @param container A function that returns an {@link ActionBuilderBuilder} for a container.
     */
    private static List<ActionBuilder> pickup(
            ClickSlotC2SPacket packet,
            Function<Integer, ActionBuilderBuilder> container
    ) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Player is null");

        // Get the appropriate container ID.
        var containerId = ScreenHandlerTranslator.bedrock2Java(
                player.currentScreenHandler, packet.getSlot());
        if (containerId == -1) throw new RuntimeException("Container is null");

        var inventory = container.apply(containerId);
        var cursor = container.apply(ContainerId.UI);

        // Apply changed item stacks to the cursor/inventory.
        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            var slotId = inventory.wrapped().getBedrockSlotId(entry.getIntKey());
            inventory.action(slotId, ItemTranslator.java2Bedrock(entry.getValue()));
        }
        cursor.action(0, ItemTranslator.java2Bedrock(packet.getStack()));

        return List.of(inventory.build(), cursor.build());
    }

    /**
     * Translates the swap action.
     * This is when a player places a cursor item onto a slot.
     *
     * @param packet The packet.
     * @param container A function that returns an {@link ActionBuilderBuilder} for a container.
     * @return A list of actions.
     */
    private static List<ActionBuilder> swap(
            ClickSlotC2SPacket packet,
            Function<Integer, ActionBuilderBuilder> container
    ) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Player is null");

        var fromContainerId = ScreenHandlerTranslator.bedrock2Java(
                player.currentScreenHandler, packet.getSlot());
        if (fromContainerId == -1) throw new RuntimeException("Invalid container");

        var fromContainer = container.apply(fromContainerId);
        var fromSlotId = fromContainer.wrapped()
                .getBedrockSlotId(packet.getSlot());

        if (packet.getModifiedStacks().size() != 2)
            throw new RuntimeException("Invalid modified stacks size");

        int targetContainerId, targetSlotId = -1;
        ActionBuilderBuilder targetContainer = null;

        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            if (packet.getSlot() == entry.getIntKey()) continue;

            targetContainerId = ScreenHandlerTranslator.bedrock2Java(
                    player.currentScreenHandler, entry.getIntKey());
            if (targetContainerId == -1) return null;

            targetContainer = container.apply(targetContainerId);
            targetSlotId = targetContainer.wrapped()
                    .getBedrockSlotId(entry.getIntKey());
        }

        if (targetSlotId == -1) return null;

        fromContainer.action(fromSlotId, ItemTranslator.java2Bedrock(packet.getStack()));
        targetContainer.action(targetSlotId, ItemTranslator.java2Bedrock(packet.getModifiedStacks().get(targetSlotId)));

        return List.of(fromContainer.build(), targetContainer.build());
    }

    /**
     * Invokes update on all containers.
     *
     * @param containers The containers.
     */
    private static void revert(PlayerContainerHolder containers) {
        for (var container : containers.getContainers().values()) {
            container.updateInventory();
        }
    }
}

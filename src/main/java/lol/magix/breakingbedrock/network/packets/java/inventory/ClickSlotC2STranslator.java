package lol.magix.breakingbedrock.network.packets.java.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.game.containers.Container;
import lol.magix.breakingbedrock.game.containers.PlayerContainerHolder;
import lol.magix.breakingbedrock.game.containers.action.ItemStackRequestBuilder;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.screen.ScreenHandlerTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestAction;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackRequestPacket;

import java.util.Objects;
import java.util.Random;

@Translate(PacketType.JAVA)
public final class ClickSlotC2STranslator extends Translator<ClickSlotC2SPacket> {
    private static final Random RANDOM = new Random();

    @Override
    public Class<ClickSlotC2SPacket> getPacketClass() {
        return ClickSlotC2SPacket.class;
    }

    @Override
    public void translate(ClickSlotC2SPacket packet) {
        var requestPacket = new ItemStackRequestPacket();

        try {
            // Get a request ID for handling the response.
            var requestId = RANDOM.nextInt(1000);
            var actions = switch (packet.getActionType()) {
                default -> {
                    this.logger.debug("Unhandled slot action: " + packet.getActionType());
                    yield null;
                }
                case PICKUP -> this.pickup(packet, requestId);
                case SWAP -> this.swap(packet, requestId);
                case QUICK_MOVE -> this.quickMove(packet, requestId);
            };

            // Check if the actions are null.
            if (actions == null) {
                // Revert inventory changes.
                ClickSlotC2STranslator.revert(this.containers());
                return;
            }

            // Add all actions to the transaction packet.
            requestPacket.getRequests().add(new ItemStackRequest(
                    requestId, actions, new String[0]
            ));

            this.bedrockClient.sendPacket(requestPacket);
        } catch (Exception ignored) { }
    }

    /**
     * Translates the pickup action.
     * This is when a player places an item into the cursor.
     *
     * @param packet The packet.
     * @param requestId The item stack request ID.
     *                  This will be passed to the builder.
     */
    private ItemStackRequestAction[] pickup(ClickSlotC2SPacket packet, int requestId) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Player is null");

        // Get the appropriate container ID.
        var containerId = ScreenHandlerTranslator.bedrock2Java(
                player.currentScreenHandler, packet.getSlot());
        if (containerId == -1) throw new RuntimeException("Container is null");

        var inventory = this.containers().getContainer(containerId);
        var cursor = this.containers().getContainer(ContainerId.UI);
        var clientCursorStack = cursor.getItem(0);
        // Check if the cursor has items.
        var clientHasCursor = !GameUtils.isAir(clientCursorStack);

        // Get the appropriate slot type.
        var slotType = ScreenHandlerTranslator.bedrockSlotType(
                player.currentScreenHandler, packet.getSlot());

        // Apply the changed item stacks.
        // NOTE: Items referenced here are the item states *after* the click.
        var javaCursorStack = packet.getStack();
        var javaHasCursor = javaCursorStack.getItem() != Items.AIR;

        var builder = ItemStackRequestBuilder.builder(requestId);
        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            var slotId = inventory.getBedrockSlotId(entry.getIntKey());
            var javaInventoryStack = entry.getValue();

            // Check if the two materials are the same.
            if (clientHasCursor && javaCursorStack.getItem() == Items.AIR) {
                // This should perform a place action.
                builder.place(clientCursorStack.getCount(),
                        cursor, ContainerSlotType.CURSOR, 0,
                        inventory, slotType, slotId);
            } else if (javaHasCursor &&
                    javaInventoryStack.getItem() != Items.AIR &&
                    javaCursorStack.getItem() != javaInventoryStack.getItem()) {
                // This should perform a swap action.
                builder.swap(
                        cursor, ContainerSlotType.CURSOR, 0,
                        inventory, slotType, slotId
                );
            } else {
                // This should perform a take action.
                if (clientHasCursor) {
                    builder.take(entry.getValue().getCount(),
                            cursor, ContainerSlotType.CURSOR, 0,
                            inventory, slotType, slotId);
                } else {
                    builder.take(javaCursorStack.getCount(),
                            inventory, slotType, slotId,
                            cursor, ContainerSlotType.CURSOR, 0);
                }
            }
        }

        return builder.execute();
    }

    /**
     * Translates the swap action.
     * This is when a player places a cursor item onto a slot.
     *
     * @param packet The packet.
     * @param requestId The item stack request ID.
     *                  This will be passed to the builder.
     * @return A list of requests.
     */
    private ItemStackRequestAction[] swap(ClickSlotC2SPacket packet, int requestId) {
        // Validate the action.
        if (packet.getModifiedStacks().size() != 2)
            throw new RuntimeException("Invalid amount of modified stacks");

        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Player is null");

        // Get the source container.
        var sourceContainerId = ScreenHandlerTranslator.bedrock2Java(
                player.currentScreenHandler, packet.getSlot());
        if (sourceContainerId == -1) throw new RuntimeException("Container is null");

        var sourceContainer = this.containers().getContainer(sourceContainerId);
        var sourceSlotType = ScreenHandlerTranslator.bedrockSlotType(
                player.currentScreenHandler, packet.getSlot());

        int targetContainerId, targetSlotId = -1;
        Container targetContainer = null;
        ContainerSlotType targetSlotType = null;
        ItemStack javaTargetStack = null;

        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            if (packet.getSlot() == entry.getIntKey()) continue;

            targetContainerId = ScreenHandlerTranslator.bedrock2Java(
                    player.currentScreenHandler, entry.getIntKey());
            if (targetContainerId == -1) throw new RuntimeException("Container is null");

            targetContainer = this.containers().getContainer(targetContainerId);
            targetSlotId = targetContainer.getBedrockSlotId(entry.getIntKey());
            targetSlotType = ScreenHandlerTranslator.bedrockSlotType(
                    player.currentScreenHandler, entry.getIntKey());
            javaTargetStack = entry.getValue();
        }
        if (targetSlotId == -1) return new ItemStackRequestAction[0];

        var sourceSlotId = sourceContainer.getBedrockSlotId(packet.getSlot());
        var clientSourceStack = sourceContainer.getItem(sourceSlotId);
        var clientTargetStack = targetContainer.getItem(targetSlotId);

        var builder = ItemStackRequestBuilder.builder(requestId);
        if (javaTargetStack.getItem() != Items.AIR &&
                clientSourceStack != ItemData.AIR &&
                clientTargetStack == ItemData.AIR) {
            // This is when an item in the inventory is switched into the hotbar.
            builder.place(clientSourceStack.getCount(),
                    sourceContainer, sourceSlotType, sourceSlotId,
                    targetContainer, targetSlotType, targetSlotId);
        } else if (javaTargetStack.getItem() == Items.AIR &&
                clientTargetStack != ItemData.AIR) {
            // This is when an item in the hotbar is switched into the inventory.
            builder.place(clientTargetStack.getCount(),
                    targetContainer, targetSlotType, targetSlotId,
                    sourceContainer, sourceSlotType, sourceSlotId);
        } else {
            // Two items are being switched into each other.
            // This requires three individual inventory request actions.

            final var targetContainerF = targetContainer;
            final var targetSlotTypeF = targetSlotType;
            final var targetSlotIdF = targetSlotId;

            // To handle this, we have to place the source item into the cursor.
            var cursor = this.containers().getCursor();
            builder.take(clientSourceStack.getCount(),
                    sourceContainer, sourceSlotType, sourceSlotId, // diamond axe 1
                    cursor, ContainerSlotType.CURSOR, 0)
                    .update(false); // air 0
            builder.callback(() -> this.client().execute(() -> {
                // Then, we perform a swap to place the cursor item into the target slot.
                var builder2 = new ItemStackRequestBuilder(requestId);
                builder2.swap(
                        cursor, ContainerSlotType.CURSOR, 0, // diamond axe 1
                        targetContainerF, targetSlotTypeF, targetSlotIdF // diamond pickaxe 1
                ).update(false);

                builder2.callback(() -> this.client().execute(() -> {
                    var builder3 = new ItemStackRequestBuilder(requestId);
                    builder3.place(clientTargetStack.getCount(),
                            cursor, ContainerSlotType.CURSOR, 0, // diamond pickaxe 1
                            sourceContainer, sourceSlotType, sourceSlotId); // air 0

                    // Send another request.
                    var requestPacket = new ItemStackRequestPacket();
                    requestPacket.getRequests().add(new ItemStackRequest(
                            requestId, builder3.execute(), new String[0]
                    ));
                    this.bedrockClient.sendPacket(requestPacket);
                }));

                // Send another request.
                var requestPacket = new ItemStackRequestPacket();
                requestPacket.getRequests().add(new ItemStackRequest(
                        requestId, builder2.execute(), new String[0]
                ));
                this.bedrockClient.sendPacket(requestPacket);
            }));

            // Source -> Cursor -> Target (source) -> Cursor -> Source (target)
        }

        return builder.execute();
    }

    /**
     * Translates the quick move action.
     *
     * @param packet The packet.
     * @param requestId The item stack request ID.
     * @return A list of requests.
     */
    private ItemStackRequestAction[] quickMove(ClickSlotC2SPacket packet, int requestId) {
        var player = MinecraftClient.getInstance().player;
        Objects.requireNonNull(player, "Player is null");

        // Get the source container ID.
        var sourceContainerId = ScreenHandlerTranslator.bedrock2Java(
                player.currentScreenHandler, packet.getSlot());
        if (sourceContainerId == -1) throw new RuntimeException("Container is null");

        var sourceContainer = this.containers().getContainer(sourceContainerId);
        var sourceSlotType = ScreenHandlerTranslator.bedrockSlotType(
                player.currentScreenHandler, packet.getSlot());

        int targetContainerId, targetSlotId = -1;
        Container targetContainer = null;
        ContainerSlotType targetSlotType = null;

        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            if (packet.getSlot() == entry.getIntKey()) continue;

            targetContainerId = ScreenHandlerTranslator.bedrock2Java(
                    player.currentScreenHandler, entry.getIntKey());
            if (targetContainerId == -1) throw new RuntimeException("Container is null");

            targetContainer = this.containers().getContainer(targetContainerId);
            targetSlotId = targetContainer.getBedrockSlotId(entry.getIntKey());
            targetSlotType = ScreenHandlerTranslator.bedrockSlotType(
                    player.currentScreenHandler, entry.getIntKey());
        }
        if (targetSlotId == -1) return new ItemStackRequestAction[0];

        var sourceSlotId = sourceContainer.getBedrockSlotId(packet.getSlot());
        var clientSourceStack = sourceContainer.getItem(sourceSlotId);

        var builder = ItemStackRequestBuilder.builder(requestId);
        builder.place(clientSourceStack.getCount(),
                sourceContainer, sourceSlotType, sourceSlotId,
                targetContainer, targetSlotType, targetSlotId);

        return builder.execute();
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

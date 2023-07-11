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
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestAction;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackRequestPacket;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Translate(PacketType.JAVA)
public final class ClickSlotC2STranslator2 extends Translator<ClickSlotC2SPacket> {
    private static final Random RANDOM = new Random();

    @Override
    public Class<ClickSlotC2SPacket> getPacketClass() {
        return ClickSlotC2SPacket.class;
    }

    @Override
    public void translate(ClickSlotC2SPacket packet) {
        var requestPacket = new ItemStackRequestPacket();

        // Get a request ID for handling the response.
        var requestId = RANDOM.nextInt(1000);
        var actions = switch (packet.getActionType()) {
            default -> {
                this.logger.debug("Unhandled slot action: " + packet.getActionType());
                yield null;
            }
            case PICKUP -> this.pickup(packet, requestId);
            case SWAP -> this.swap(packet, requestId);
        };

        // Check if the actions are null.
        if (actions == null) {
            // Revert inventory changes.
            ClickSlotC2STranslator2.revert(this.containers());
            return;
        }

        // Add all actions to the transaction packet.
        requestPacket.getRequests().add(new ItemStackRequest(
                requestId, actions, new String[0]
        ));

        this.bedrockClient.sendPacket(requestPacket);
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
        // Check if the cursor is the source.
        var cursorSource = !GameUtils.isAir(cursor.getItem(0));

        // Get the appropriate slot type.
        var slotType = ScreenHandlerTranslator.bedrockSlotType(
                player.currentScreenHandler, packet.getSlot());

        // Apply the changed item stacks.
        var sourceStack = packet.getStack();
        var builder = ItemStackRequestBuilder.builder(requestId);
        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            var slotId = inventory.getBedrockSlotId(entry.getIntKey());

            if (cursorSource) {
                builder.take(entry.getValue().getCount(),
                        cursor, ContainerSlotType.CURSOR, 0,
                        inventory, slotType, slotId);
            } else {
                builder.take(sourceStack.getCount(),
                        inventory, slotType, slotId,
                        cursor, ContainerSlotType.CURSOR, 0);
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
     * @return A list of actions.
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

        for (var entry : packet.getModifiedStacks().int2ObjectEntrySet()) {
            System.out.println("Modified stack: " + entry.getIntKey());
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

        System.out.println("-------------------- TARGET --------------------");
        System.out.println("Target slot: " + targetSlotId);
        System.out.println("Target slot type: " + targetSlotType);
        System.out.println("Target slot quantity: " + targetContainer.getItem(targetSlotId).getCount());
        System.out.println("-------------------- SOURCE --------------------");
        System.out.println("Source slot: " + packet.getSlot());
        System.out.println("Source slot type: " + sourceSlotType);
        System.out.println("Source slot quantity: " + sourceContainer.getItem(packet.getSlot()).getCount());
        System.out.println("-------------------- END --------------------");

        var builder = ItemStackRequestBuilder.builder(requestId)
                .place(sourceContainer.getItem(packet.getSlot()).getCount(),
                        sourceContainer, sourceSlotType, packet.getSlot(),
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

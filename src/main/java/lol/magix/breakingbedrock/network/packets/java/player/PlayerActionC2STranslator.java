package lol.magix.breakingbedrock.network.packets.java.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.events.defaults.PlayerTickEvent;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.Direction;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.data.PlayerBlockActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Translate(PacketType.JAVA)
public final class PlayerActionC2STranslator extends Translator<PlayerActionC2SPacket> {
    @Getter private static List<Vector3i> brokenBlocks
            = Collections.synchronizedList(new LinkedList<>());

    private long ticks = 0;
    private Direction lastDirection;
    private Vector3i lastBlockPos;

    @Override
    public Class<PlayerActionC2SPacket> getPacketClass() {
        return PlayerActionC2SPacket.class;
    }

    @Override
    public void translate(PlayerActionC2SPacket packet) {
        var client = this.client();
        var world = client.world;
        var interactionManager = client.interactionManager;
        if (world == null || interactionManager == null) return;

        var player = this.player();
        if (player == null) return;

        var gameMode = interactionManager.getCurrentGameMode();
        var blockPos = GameUtils.toBlockPos(packet.getPos());

        switch (packet.getAction()) {
            case START_DESTROY_BLOCK -> {
                // Check if the player is in creative.
                if (gameMode.isCreative()) {
                    // Send the creative interaction.
                    var creativeActionPacket = new PlayerActionPacket();
                    creativeActionPacket.setRuntimeEntityId(this.data().getRuntimeId());
                    creativeActionPacket.setAction(PlayerActionType.DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK);
                    creativeActionPacket.setBlockPosition(blockPos);
                    creativeActionPacket.setResultPosition(blockPos);
                    creativeActionPacket.setFace(packet.getDirection().ordinal());

                    this.bedrockClient.sendPacket(creativeActionPacket);
                }

                this.lastDirection = packet.getDirection();
                this.lastBlockPos = blockPos;

                var actionPacket = new PlayerActionPacket();
                actionPacket.setAction(PlayerActionType.START_BREAK);
                actionPacket.setBlockPosition(blockPos);
                actionPacket.setResultPosition(blockPos);
                actionPacket.setFace(packet.getDirection().ordinal());

                this.sendPacket(actionPacket);

                // Send continue breaking packets.
                this.bedrockClient.getEventManager().registerListener(
                        PlayerTickEvent.class, this::onPlayerTick);

                if (gameMode.isCreative()) {
                    // Queue a stop destroy block.
                    this.translate(new PlayerActionC2SPacket(
                            Action.STOP_DESTROY_BLOCK,
                            packet.getPos(),
                            packet.getDirection()));
                }
            }
            case STOP_DESTROY_BLOCK -> {
                // Stop breaking the block.
                var actionPacket = new PlayerActionPacket();
                actionPacket.setAction(PlayerActionType.STOP_BREAK);
                actionPacket.setBlockPosition(blockPos);
                actionPacket.setResultPosition(blockPos);
                actionPacket.setFace(packet.getDirection().ordinal());

                this.sendPacket(actionPacket);

                // Stop breaking the block.
                this.bedrockClient.getEventManager().removeListener(
                        PlayerTickEvent.class, this::onPlayerTick);
                this.lastDirection = null;
                this.lastBlockPos = null;

                // Get inventory properties.
                var selectedSlot = player.getInventory().selectedSlot;
                var item = this.containers().getInventory().getItem(selectedSlot);

                // Send transaction packet.
                var transactionPacket = new InventoryTransactionPacket();
                transactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
                transactionPacket.setActionType(2);
                transactionPacket.setBlockPosition(blockPos);
                transactionPacket.setBlockFace(packet.getDirection().ordinal());
                transactionPacket.setHotbarSlot(selectedSlot);
                transactionPacket.setItemInHand(item);
                transactionPacket.setPlayerPosition(GameUtils.convert(player.getPos()));
                transactionPacket.setClickPosition(Vector3f.ZERO);
                transactionPacket.setHeadPosition(Vector3f.ZERO);
                transactionPacket.setBlockDefinition(BlockStateTranslator.translate(
                        world.getBlockState(packet.getPos())));

                this.bedrockClient.sendPacket(transactionPacket);

                // Remove the block from the client.
                this.javaClient().processPacket(new BlockUpdateS2CPacket(
                        packet.getPos(), Blocks.AIR.getDefaultState()
                ));
            }
            case ABORT_DESTROY_BLOCK -> {
                var actionPacket = new PlayerActionPacket();
                actionPacket.setAction(PlayerActionType.ABORT_BREAK);
                actionPacket.setBlockPosition(blockPos);
                actionPacket.setFace(packet.getDirection().ordinal());

                this.sendPacket(actionPacket);

                // Stop breaking the block.
                this.bedrockClient.getEventManager().removeListener(
                        PlayerTickEvent.class, this::onPlayerTick);
                this.lastDirection = null;
                this.lastBlockPos = null;
            }
        }
    }

    /**
     * Sends the action packet.
     * Wraps it for server-authoritative movement if needed.
     * Applies the player's runtime ID.
     *
     * @param packet The packet to send.
     */
    private void sendPacket(PlayerActionPacket packet) {
        packet.setRuntimeEntityId(this.data().getRuntimeId());
        if (this.data().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
            this.bedrockClient.sendPacket(packet);
        } else {
            // Create the action data.
            var actionData = new PlayerBlockActionData();
            actionData.setAction(packet.getAction());
            actionData.setFace(packet.getFace());
            actionData.setBlockPosition(packet.getBlockPosition());

            // Add the data to the input handler.
            this.bedrockClient.addInputData(PlayerAuthInputData.PERFORM_BLOCK_ACTIONS);
            this.bedrockClient.addBlockAction(actionData);
        }
    }

    /**
     * Invoked when the player ticks.
     *
     * @param event The event.
     */
    private void onPlayerTick(PlayerTickEvent event) {
        if (this.ticks++ % 5 != 0) return; // Run every 5 ticks.

        var client = this.client();
        var player = client.player;
        if (player == null) return;

        if (this.lastDirection == null ||
                this.lastBlockPos == null) return;

        // Check if the block was broken.
        var copy = Collections.unmodifiableList(brokenBlocks);
        for (var block : copy) {
            if (GameUtils.equals(block, this.lastBlockPos)) {
                this.translate(new PlayerActionC2SPacket(
                        Action.STOP_DESTROY_BLOCK,
                        GameUtils.toBlockPos(block), this.lastDirection
                ));

                // Remove the block from the list.
                brokenBlocks.remove(block);
                return;
            }
        }

        var actionPacket = new PlayerActionPacket();
        actionPacket.setAction(PlayerActionType.CONTINUE_BREAK);
        actionPacket.setBlockPosition(this.lastBlockPos);
        actionPacket.setFace(this.lastDirection.ordinal());

        this.sendPacket(actionPacket);
    }
}

package lol.magix.breakingbedrock.network.packets.java;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.events.defaults.PlayerTickEvent;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.data.PlayerBlockActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

@Translate(PacketType.JAVA)
public final class PlayerActionC2STranslator extends Translator<PlayerActionC2SPacket> {
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

        var blockPos = WorldUtils.toBlockPos(packet.getPos());
        switch (packet.getAction()) {
            case START_DESTROY_BLOCK -> {
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
            }
            case STOP_DESTROY_BLOCK -> {
                var actionPacket = new PlayerActionPacket();
                actionPacket.setAction(PlayerActionType.STOP_BREAK);
                actionPacket.setBlockPosition(blockPos);
                actionPacket.setResultPosition(blockPos);
                actionPacket.setFace(packet.getDirection().ordinal());

                this.sendPacket(actionPacket);

                // Send a different packet if the player is in creative.
                if (interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                    var creativeActionPacket = new PlayerActionPacket();
                    creativeActionPacket.setRuntimeEntityId(this.data().getRuntimeId());
                    creativeActionPacket.setAction(PlayerActionType.DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK);
                    creativeActionPacket.setBlockPosition(blockPos);
                    creativeActionPacket.setResultPosition(blockPos);
                    creativeActionPacket.setFace(packet.getDirection().ordinal());

                    this.bedrockClient.sendPacket(creativeActionPacket);
                }

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
                transactionPacket.setPlayerPosition(WorldUtils.convert(player.getPos()));
                transactionPacket.setClickPosition(Vector3f.ZERO);
                transactionPacket.setHeadPosition(Vector3f.ZERO);
                transactionPacket.setBlockDefinition(BlockStateTranslator.translate(
                        world.getBlockState(packet.getPos())));

                this.bedrockClient.sendPacket(transactionPacket);
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
        var player = this.player();
        if (player == null) return;

        if (this.lastDirection == null ||
                this.lastBlockPos == null) return;

        var actionPacket = new PlayerActionPacket();
        actionPacket.setAction(PlayerActionType.CONTINUE_BREAK);
        actionPacket.setBlockPosition(this.lastBlockPos);
        actionPacket.setFace(this.lastDirection.ordinal());

        this.sendPacket(actionPacket);
    }
}

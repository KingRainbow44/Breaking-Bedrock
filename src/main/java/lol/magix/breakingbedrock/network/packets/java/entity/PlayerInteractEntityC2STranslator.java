package lol.magix.breakingbedrock.network.packets.java.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.commands.DebugCommand;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinPlayerInteractEntityC2SPacket;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import lol.magix.breakingbedrock.utils.ReflectionUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

import java.lang.reflect.Field;
import java.util.Objects;

@Translate(PacketType.JAVA)
public final class PlayerInteractEntityC2STranslator extends Translator<PlayerInteractEntityC2SPacket> {
    private static final Field TYPE_FIELD = ReflectionUtils.getField(PlayerInteractEntityC2SPacket.class, "type");

    @Override
    public Class<PlayerInteractEntityC2SPacket> getPacketClass() {
        return PlayerInteractEntityC2SPacket.class;
    }

    @Override
    public void translate(PlayerInteractEntityC2SPacket packet) {
        // Check the packet type.
        if (!Objects.equals(getInteractType(packet).toString(), "ATTACK")) return;

        var player = this.player();
        var world = this.client().world;
        if (player == null || world == null) return;

        var selectedSlot = player.getInventory().selectedSlot;
        var inventory = this.containers().getInventory();

        // Get the block the player is looking at.
        var lookingAtPos = player.raycast(
                5, 0, false);

        var sourcePos = lookingAtPos.getPos();
        var blockPos = BlockPos.ofFloored(sourcePos);
        var block = world.getBlockState(blockPos);

        var entityId = ((IMixinPlayerInteractEntityC2SPacket) packet).getEntityId();

        // Prepare the transaction packet.
        var transactionPacket = new InventoryTransactionPacket();
        transactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE_ON_ENTITY);
        transactionPacket.setActionType(1);
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setRuntimeEntityId(entityId);
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setItemInHand(inventory.getItem(selectedSlot));
        transactionPacket.setPlayerPosition(this.javaClient().getPlayerPosition());
        transactionPacket.setClickPosition(GameUtils.convert(sourcePos.subtract(
                blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        transactionPacket.setBlockDefinition(BlockStateTranslator.translate(block));

        // Check if entity logging is enabled.
        if (DebugCommand.ENTITY_DEBUG.get()) {
            DebugCommand.ENTITY_LOGGER.accept(
                    world.getEntityById(entityId));
        }

        this.bedrockClient.sendPacket(transactionPacket);
    }

    /**
     * Fetches the type of interaction.
     *
     * @param packet The packet.
     * @return The type of interaction.
     */
    private static Object getInteractType(PlayerInteractEntityC2SPacket packet) {
        try {
            // Get the type class.
            var type = TYPE_FIELD.get(packet);
            // Get the accessor.
            var typeClass = type.getClass();
            var accessor = typeClass.getDeclaredMethod("getType");
            accessor.setAccessible(true);

            // Get the type.
            return accessor.invoke(type);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new Object();
        }
    }
}

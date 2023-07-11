package lol.magix.breakingbedrock.network.packets.java.entity;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinPlayerInteractEntityC2SPacket;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

@Translate(PacketType.JAVA)
public final class PlayerInteractEntityC2STranslator extends Translator<PlayerInteractEntityC2SPacket> {
    @Override
    public Class<PlayerInteractEntityC2SPacket> getPacketClass() {
        return PlayerInteractEntityC2SPacket.class;
    }

    @Override
    public void translate(PlayerInteractEntityC2SPacket packet) {
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

        var transactionPacket = new InventoryTransactionPacket();
        transactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE_ON_ENTITY);
        transactionPacket.setActionType(1);
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setRuntimeEntityId(
                ((IMixinPlayerInteractEntityC2SPacket) packet).getEntityId()
        );
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setItemInHand(inventory.getItem(selectedSlot));
        transactionPacket.setPlayerPosition(this.javaClient().getPlayerPosition());
        transactionPacket.setClickPosition(GameUtils.convert(sourcePos.subtract(
                blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        transactionPacket.setBlockDefinition(BlockStateTranslator.translate(block));

        this.bedrockClient.sendPacket(transactionPacket);
    }
}
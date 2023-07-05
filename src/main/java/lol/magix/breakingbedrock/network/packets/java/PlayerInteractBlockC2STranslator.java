package lol.magix.breakingbedrock.network.packets.java;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

@Translate(PacketType.JAVA)
public final class PlayerInteractBlockC2STranslator extends Translator<PlayerInteractBlockC2SPacket> {
    @Override
    public Class<PlayerInteractBlockC2SPacket> getPacketClass() {
        return PlayerInteractBlockC2SPacket.class;
    }

    @Override
    public void translate(PlayerInteractBlockC2SPacket packet) {
        var world = this.client().world;
        if (world == null) return;

        var inventory = this.player().getInventory();
        var selectedSlot = inventory.selectedSlot;

        var sourcePos = packet.getBlockHitResult().getBlockPos();
        var blockPos = WorldUtils.toBlockPos(sourcePos);
        var offset = packet.getBlockHitResult().getPos()
                .subtract(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());

        var handItem = ItemData.AIR; // TODO: Implement inventory.

        {
            // Send the first inventory transaction.
            var transactionPacket1 = new InventoryTransactionPacket();
            transactionPacket1.setTransactionType(InventoryTransactionType.ITEM_USE);
            transactionPacket1.setActionType(0);
            transactionPacket1.setBlockPosition(blockPos);
            transactionPacket1.setBlockFace(packet.getBlockHitResult().getSide().ordinal());
            transactionPacket1.setHotbarSlot(selectedSlot);
            transactionPacket1.setItemInHand(handItem);
            transactionPacket1.setPlayerPosition(this.javaClient().getPlayerPosition());
            transactionPacket1.setClickPosition(WorldUtils.convert(offset));
            transactionPacket1.setBlockDefinition(BlockStateTranslator.translate(world.getBlockState(sourcePos)));
            this.bedrockClient.sendPacket(transactionPacket1);
        }

        {
            // Send the second inventory transaction.
            var transactionPacket1 = new InventoryTransactionPacket();
            transactionPacket1.setTransactionType(InventoryTransactionType.ITEM_USE);
            transactionPacket1.setActionType(1);
            transactionPacket1.setBlockPosition(Vector3i.ZERO);
            transactionPacket1.setBlockFace(255);
            transactionPacket1.setHotbarSlot(selectedSlot);
            transactionPacket1.setItemInHand(handItem);
            transactionPacket1.setPlayerPosition(this.javaClient().getPlayerPosition());
            transactionPacket1.setClickPosition(Vector3f.ZERO);
            transactionPacket1.setBlockDefinition(BlockStateTranslator.translate(world.getBlockState(sourcePos)));
            this.bedrockClient.sendPacket(transactionPacket1);
        }
    }
}

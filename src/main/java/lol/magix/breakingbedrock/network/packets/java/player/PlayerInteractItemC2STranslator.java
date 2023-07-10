package lol.magix.breakingbedrock.network.packets.java.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

@Translate(PacketType.JAVA)
public final class PlayerInteractItemC2STranslator extends Translator<PlayerInteractItemC2SPacket> {
    @Override
    public Class<PlayerInteractItemC2SPacket> getPacketClass() {
        return PlayerInteractItemC2SPacket.class;
    }

    @Override
    public void translate(PlayerInteractItemC2SPacket packet) {
        var player = this.player();
        var world = this.client().world;
        if (player == null || world == null) return;

        var containers = this.containers();
        var selectedSlot = player.getInventory().selectedSlot;

        var itemStack = packet.getHand() == Hand.MAIN_HAND ?
                containers.getInventory().getItem(selectedSlot) :
                containers.getOffhand().getItem(0);

        // Get the block the player is looking at.
        var lookingAtPos = player.raycast(
                5, 0, false);
        if (lookingAtPos.getType() == HitResult.Type.BLOCK) {
            return; // This means the player isn't looking at air, the thing this packet handles.
        }

        var sourcePos = lookingAtPos.getPos();
        var blockPos = BlockPos.ofFloored(sourcePos);
        var block = world.getBlockState(blockPos);

        // Create an inventory transaction.
        var transactionPacket = new InventoryTransactionPacket();
        transactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
        transactionPacket.setActionType(1);
        transactionPacket.setBlockPosition(WorldUtils.toBlockPos(blockPos));
        transactionPacket.setHotbarSlot(selectedSlot);
        transactionPacket.setItemInHand(itemStack);
        transactionPacket.setPlayerPosition(this.javaClient().getPlayerPosition());
        transactionPacket.setClickPosition(WorldUtils.convert(lookingAtPos.getPos().subtract(
                blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        transactionPacket.setBlockDefinition(BlockStateTranslator.translate(block));
        this.bedrockClient.sendPacket(transactionPacket);
    }
}

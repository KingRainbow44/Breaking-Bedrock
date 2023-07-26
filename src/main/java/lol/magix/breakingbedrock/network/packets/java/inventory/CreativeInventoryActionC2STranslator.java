package lol.magix.breakingbedrock.network.packets.java.inventory;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.game.containers.action.ItemStackRequestBuilder;
import lol.magix.breakingbedrock.game.containers.generic.CreativeContainer;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.utils.RandomUtils;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestAction;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackRequestPacket;

import static lol.magix.breakingbedrock.utils.GameUtils.itemStackInfo;

@Translate(PacketType.JAVA)
public final class CreativeInventoryActionC2STranslator extends Translator<CreativeInventoryActionC2SPacket> {
    @Override
    public Class<CreativeInventoryActionC2SPacket> getPacketClass() {
        return CreativeInventoryActionC2SPacket.class;
    }

    @Override
    public void translate(CreativeInventoryActionC2SPacket packet) {
        var javaSlot = packet.getSlot(); // This is the slot that was interacted with.
        var javaStack = packet.getItemStack(); // This is the *updated* item stack value.

        var container = this.containers().getInventory();
        var bedrockSlot = container.getBedrockSlotId(javaSlot); // This is the Bedrock slot that was interacted with.
        var bedrockStack = container.getItem(bedrockSlot); // This is the *previous* item stack value.

        var requestId = RandomUtils.randomInt(1, 1000);
        var builder = ItemStackRequestBuilder.builder(requestId);

        System.out.println("inventory action of " + bedrockSlot + " " + itemStackInfo(javaStack));

        // Create the request packet.
        var requestPacket = new ItemStackRequestPacket();
        ItemStackRequestAction[] actions = null;

        // Check if the item was dropped.
        if (javaSlot == -1) {
            // Determine which slot contained the item.
            var slot = -1;
            var contents = container.getContents();
            for (var i = 0; i < contents.size(); i++) {
                var item = contents.get(i);
                if (item == null) continue;

                var translated = ItemTranslator.bedrock2Java(item);
                if (translated == null) continue;

                if (translated.getCount() != javaStack.getCount()) continue;
                if (!translated.getItem().equals(javaStack.getItem())) continue;

                slot = i; break;
            }

            actions = builder.drop(javaStack.getCount(),
                    container, ContainerSlotType.INVENTORY, slot)
                    .execute();
        } else if (javaStack.isEmpty()) {
            // Destroy the item.
            if (bedrockStack == null) return; // The item was not found.
            if (bedrockStack.getCount() <= 0) return; // The item was already destroyed.
            actions = builder.destroy(bedrockStack.getCount(),
                    container, ContainerSlotType.INVENTORY, bedrockSlot)
                    .execute();
        } else {
            // Attempt to look up the stack.
            var convertedStack = ItemTranslator.java2Bedrock(javaStack);
            var creative = (CreativeContainer) this.containers().getCreative();
            var createdStack = creative.getItem(convertedStack.getDefinition().getIdentifier());
            if (createdStack == -1) return; // The item was not found.

            // Build the item.
            actions = builder.create(createdStack)
                    .take(javaStack.getCount(), createdStack,
                            container, ContainerSlotType.INVENTORY, bedrockSlot)
                    .execute();
        }

        // Send the request packet.
        requestPacket.getRequests().add(
                new ItemStackRequest(requestId, actions, new String[0]));
        this.bedrockClient.sendPacket(requestPacket);
    }
}

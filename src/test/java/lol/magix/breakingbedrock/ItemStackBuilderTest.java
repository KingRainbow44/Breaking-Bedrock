package lol.magix.breakingbedrock;

import lol.magix.breakingbedrock.game.containers.action.ItemStackRequestBuilder;
import lol.magix.breakingbedrock.game.containers.player.CursorContainer;
import lol.magix.breakingbedrock.game.containers.player.InventoryContainer;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.Arrays;

public final class ItemStackBuilderTest {
    public static void main(String[] args) {
        var inventory = new InventoryContainer();
        var cursor = new CursorContainer();

        var diamond = ItemData.builder()
                .definition(new SimpleItemDefinition("minecraft:diamond", 1, false))
                .count(3).build();
        var stone = ItemData.builder()
                .definition(new SimpleItemDefinition("minecraft:stone", 1, false))
                .count(12).build();
        var air = ItemData.AIR;

        inventory.setBedrockItem(0, diamond);
        cursor.setBedrockItem(0, air);

        System.out.println("---------- Before ----------");

        System.out.println("Inventory: " + inventory);
        System.out.println("Cursor: " + cursor);

        System.out.println("---------- After ----------");

        var actions = ItemStackRequestBuilder.builder(1)
                .take(1, inventory, ContainerSlotType.INVENTORY, 0,
                        cursor, ContainerSlotType.CURSOR, 0).execute();

        System.out.println("Inventory actions: " + Arrays.toString(actions));
        System.out.println("Inventory: " + inventory);
        System.out.println("Cursor: " + cursor);
    }
}

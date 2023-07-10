package lol.magix.breakingbedrock.game.containers;

import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.client.MinecraftClient;

public abstract class DynamicContainer extends GenericContainer {
    /**
     * Creates an empty container.
     *
     * @param size The size of the container.
     */
    public DynamicContainer(int size) {
        super(size);
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void updateInventory() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Cannot update inventory with no player.");

        var screenHandler = player.currentScreenHandler;
        for (var i = 0; i < this.getSize(); i++)
            screenHandler.getSlot(this.getJavaSlotId(i))
                    .setStackNoCallbacks(ItemTranslator.bedrock2Java(this.getItem(i)));
    }
}

package lol.magix.breakingbedrock.game.containers.player;

import lol.magix.breakingbedrock.game.containers.GenericContainer;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.client.MinecraftClient;

public final class ArmorContainer extends GenericContainer {
    /**
     * Creates an empty container with 4 slots.
     */
    public ArmorContainer() {
        super(4);
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return bedrockSlotId + 5;
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return javaSlotId - 5;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void updateInventory() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Cannot update inventory with no player.");

        var screenHandler = player.playerScreenHandler;
        for (var i = 0; i < this.getSize(); i++)
            screenHandler.getSlot(this.getJavaSlotId(i))
                    .setStackNoCallbacks(ItemTranslator.bedrock2Java(this.getItem(i)));
    }
}

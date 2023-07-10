package lol.magix.breakingbedrock.game.containers.player;

import lol.magix.breakingbedrock.game.containers.GenericContainer;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.client.MinecraftClient;

public final class CursorContainer extends GenericContainer {
    /**
     * Creates an empty container with 1 slot.
     */
    public CursorContainer() {
        super(1);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void updateInventory() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Cannot update inventory with no player.");

        player.currentScreenHandler.setCursorStack(
                ItemTranslator.bedrock2Java(this.getItem(0)));
    }
}

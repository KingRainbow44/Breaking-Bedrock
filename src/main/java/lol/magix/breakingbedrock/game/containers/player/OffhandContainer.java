package lol.magix.breakingbedrock.game.containers.player;

import lol.magix.breakingbedrock.game.containers.GenericContainer;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.client.MinecraftClient;

public final class OffhandContainer extends GenericContainer {
    /**
     * Creates an empty container with 1 slot.
     */
    public OffhandContainer() {
        super(1);
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return 45;
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return 0;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void updateInventory() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) throw new RuntimeException("Cannot update inventory with no player.");

        player.playerScreenHandler.getSlot(this.getJavaSlotId(0))
                .setStack(ItemTranslator.bedrock2Java(this.getItem(0)));
    }
}

package lol.magix.breakingbedrock.translators.screen;

import net.minecraft.screen.PlayerScreenHandler;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;

public final class PlayerScreenTranslator extends ScreenHandlerTranslator<PlayerScreenHandler> {
    @Override
    public int getBedrockId(PlayerScreenHandler javaContainer, int javaSlotId) {
        if (javaSlotId >= 9 && javaSlotId <= 44) { // Slots for the inventory.
            return ContainerId.INVENTORY;
        } else if (javaSlotId >= 5 && javaSlotId <= 8) { // Slots for the player's armor.
            return ContainerId.ARMOR;
        } else if (javaSlotId == 45) { // Slot of the Java offhand.
            return ContainerId.OFFHAND;
        }

        return -1;
    }

    @Override
    public Class<PlayerScreenHandler> getScreenHandler() {
        return PlayerScreenHandler.class;
    }
}

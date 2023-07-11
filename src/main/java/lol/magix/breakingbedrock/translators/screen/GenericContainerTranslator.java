package lol.magix.breakingbedrock.translators.screen;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;

public final class GenericContainerTranslator extends ScreenHandlerTranslator<GenericContainerScreenHandler> {
    @Override
    public int getBedrockId(GenericContainerScreenHandler javaContainer, int javaSlotId) {
        var slots = javaContainer.getRows() * 9;

        // Fallback to the current container if the slot ID is out of bounds.
        if (javaSlotId < slots) return BedrockNetworkClient.getInstance()
                .getContainerHolder().getCurrentContainerId();

        return ContainerId.INVENTORY;
    }

    @Override
    public Class<GenericContainerScreenHandler> getScreenHandler() {
        return GenericContainerScreenHandler.class;
    }
}

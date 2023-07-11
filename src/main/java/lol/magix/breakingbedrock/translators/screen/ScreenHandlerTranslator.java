package lol.magix.breakingbedrock.translators.screen;

import net.minecraft.screen.ScreenHandler;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class ScreenHandlerTranslator<T extends ScreenHandler> {
    private static final Map<Class<? extends ScreenHandler>,
            ScreenHandlerTranslator<?>> TRANSLATORS = new HashMap<>();

    /**
     * Registers all screen handler translators.
     */
    public static void initialize() {
        ScreenHandlerTranslator.register(new GenericContainerTranslator());
        ScreenHandlerTranslator.register(new PlayerScreenTranslator());
    }

    /**
     * Registers a screen handler translator.
     *
     * @param translator The translator to register.
     */
    private static void register(ScreenHandlerTranslator<?> translator) {
        TRANSLATORS.put(translator.getScreenHandler(), translator);
    }

    /**
     * Fetches a screen handler translator.
     *
     * @param screenHandler The screen handler.
     * @return The translator. Null if not found.
     */
    private static ScreenHandlerTranslator<ScreenHandler> getTranslator(ScreenHandler screenHandler) {
        var type = screenHandler.getClass();
        return (ScreenHandlerTranslator<ScreenHandler>) TRANSLATORS.get(type);
    }

    /**
     * Converts a Bedrock slot ID to a Java slot ID.
     *
     * @param container The Java container.
     * @param slot The Bedrock slot ID.
     * @return The Java slot ID.
     */
    public static int bedrock2Java(ScreenHandler container, int slot) {
        var translator = ScreenHandlerTranslator.getTranslator(container);
        if (translator == null) return -1;

        var containerId = translator.getBedrockId(container, slot);
        if (containerId == -1) throw new IllegalStateException("Unable to get container ID");

        return containerId;
    }

    /**
     * Converts a Java slot ID to a Bedrock slot location.
     *
     * @param container The Java container.
     * @param slot The Java slot ID.
     * @return The Bedrock slot location.
     */
    public static ContainerSlotType bedrockSlotType(ScreenHandler container, int slot) {
        var translator = ScreenHandlerTranslator.getTranslator(container);
        if (translator == null) return ContainerSlotType.HOTBAR;

        var containerId = translator.getBedrockSlot(container, slot);
        if (containerId == null) throw new IllegalStateException("Unable to get container ID");

        return containerId;
    }

    /**
     * Converts a Java slot ID to the location of the slot in Bedrock.
     *
     * @param javaContainer The Java container.
     * @param javaSlotId The Java slot ID.
     * @return The Bedrock slot location.
     */
    public ContainerSlotType getBedrockSlot(T javaContainer, int javaSlotId) {
        return switch (this.getBedrockId(javaContainer, javaSlotId)) {
            default -> throw new RuntimeException("Invalid slot ID");
            case ContainerId.INVENTORY -> ContainerSlotType.INVENTORY;
            case ContainerId.OFFHAND -> ContainerSlotType.OFFHAND;
            case ContainerId.ARMOR -> ContainerSlotType.ARMOR;
            case ContainerId.UI -> ContainerSlotType.CURSOR;
        };
    }

    /**
     * Converts a Java slot ID to a Bedrock slot ID.
     * This uses the Java container to get the Bedrock ID.
     *
     * @param javaContainer The Java container.
     * @param javaSlotId The Java slot ID.
     * @return The Bedrock slot ID.
     */
    public abstract int getBedrockId(T javaContainer, int javaSlotId);

    /**
     * @return The screen handler class.
     */
    public abstract Class<T> getScreenHandler();
}

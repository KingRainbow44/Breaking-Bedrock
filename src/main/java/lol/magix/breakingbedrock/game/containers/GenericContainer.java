package lol.magix.breakingbedrock.game.containers;

/**
 * Generic container implementation.
 * This container cannot be used with the Java UI.
 */
public abstract class GenericContainer extends Container {
    /**
     * Creates an empty container.
     *
     * @param size The size of the container.
     */
    public GenericContainer(int size) {
        super(size);
    }

    @Override
    public int getJavaSlotId(int bedrockSlotId) {
        return bedrockSlotId;
    }

    @Override
    public int getBedrockSlotId(int javaSlotId) {
        return javaSlotId;
    }
}

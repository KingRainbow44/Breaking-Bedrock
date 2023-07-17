package lol.magix.breakingbedrock.utils;

public interface RandomUtils {
    /**
     * Returns a random integer between min and max, inclusive.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random integer between min and max, inclusive.
     */
    static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}

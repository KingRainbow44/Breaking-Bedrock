package lol.magix.breakingbedrock.utils;

/**
 * Utility methods for working with intervals.
 */
public interface IntervalUtils {
    /**
     * Run a task after a specified delay.
     *
     * @param runnable The task to run.
     * @param delay The delay in milliseconds.
     */
    static void runAfter(Runnable runnable, long delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }

            runnable.run();
        }).start();
    }
}

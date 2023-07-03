package lol.magix.breakingbedrock.objects;

import lombok.Builder;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
public final class ThreadFactoryBuilder implements ThreadFactory {
    private static final ThreadFactory backingFactory = Executors.defaultThreadFactory();

    /**
     * Creates a default thread factory.
     *
     * @return A thread factory.
     */
    public static ThreadFactory base() {
        return ThreadFactoryBuilder.builder()
                .format("Bedrock Listener - #%d")
                .priority(5).daemon(true)
                .build();
    }

    private final AtomicInteger count = new AtomicInteger(0);
    private final boolean daemon;
    private final String format;

    @Builder.Default
    private final int priority =
            Thread.currentThread().getPriority();
    private final UncaughtExceptionHandler exceptionHandler;

    /**
     * Formats the thread name.
     *
     * @param format The base format.
     * @param count The current thread count.
     * @return The formatted thread name.
     */
    private static String format(String format, int count) {
        return String.format(Locale.ROOT, format, count);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var thread = backingFactory.newThread(runnable);

        if (this.format != null) {
            thread.setName(format(this.format, this.count.getAndIncrement()));
        }

        thread.setDaemon(daemon);
        thread.setPriority(priority);

        if (this.exceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.exceptionHandler);
        }

        return thread;
    }
}

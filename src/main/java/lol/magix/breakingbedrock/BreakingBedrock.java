package lol.magix.breakingbedrock;

import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BreakingBedrock {
    @Getter private static final Logger logger = LoggerFactory.getLogger("BreakingBedrock");
    @Getter private static final Reflections reflector = new Reflections("lol.magix.breakingbedrock");

    /**
     * Initializes high-level mod systems.
     */
    public static void initialize() {
        logger.info("Initialized!");
    }
}

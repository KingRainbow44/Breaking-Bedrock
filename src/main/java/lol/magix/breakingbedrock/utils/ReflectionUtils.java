package lol.magix.breakingbedrock.utils;

import lol.magix.breakingbedrock.BreakingBedrock;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for Java reflection.
 */
public interface ReflectionUtils {
    /**
     * Gets all classes annotated with the specified annotation.
     * @param annotation The annotation.
     * @return A list of classes.
     */
    static List<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation) {
        return new ArrayList<>(BreakingBedrock.getReflector().getTypesAnnotatedWith(annotation));
    }
}

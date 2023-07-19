package lol.magix.breakingbedrock.utils;

import lol.magix.breakingbedrock.BreakingBedrock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    /**
     * Gets a field from a class.
     *
     * @param type The class.
     * @param fieldName The field name.
     * @return The field.
     */
    static Field getField(Class<?> type, String fieldName) {
        try {
            var field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception ignored) {
            throw new RuntimeException("Failed to get field " + fieldName + " from " + type.getName() + "!");
        }
    }
}

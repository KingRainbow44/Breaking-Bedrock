package lol.magix.breakingbedrock.utils;

import lol.magix.breakingbedrock.BreakingBedrock;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for loading Java resources.
 */
public interface ResourceUtils {
    /**
     * Returns a stream to the specified resource.
     * @param path The path to the resource.
     * @return The stream to the resource.
     */
    static InputStream getResourceAsStream(String path) {
        // Validate the path.
//        if (!path.startsWith("/"))
//            path = "/" + path;

        var stream = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            BreakingBedrock.getLogger().error("Resource {} could not be loaded.", path);
            return InputStream.nullInputStream();
        }

        return stream;
    }

    /**
     * Returns the contents of the specified resource.
     * @param path The path to the resource.
     * @return The contents of the resource.
     */
    static byte[] getResource(String path) {
        try (var resource = ResourceUtils.getResourceAsStream(path)) {
            return resource.readAllBytes();
        } catch (IOException ignored) {
            BreakingBedrock.getLogger().warn("Failed to load resource: " + path);
        }

        return new byte[0];
    }

    /**
     * Returns the contents of the specified resource as a string.
     * @param path The path to the resource.
     * @return The contents of the resource.
     */
    static String getResourceAsString(String path) {
        return new String(ResourceUtils.getResource(path));
    }

    /**
     * Returns the contents of the specified resource as an object.
     * @param path The path to the resource.
     * @param type The type of the object.
     * @return The contents of the resource.
     */
    static <T> T getResourceAsObject(String path, Class<T> type) {
        return BreakingBedrock.getGson().fromJson(ResourceUtils.getResourceAsString(path), type);
    }
}

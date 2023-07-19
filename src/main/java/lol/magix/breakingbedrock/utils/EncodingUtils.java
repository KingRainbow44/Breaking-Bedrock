package lol.magix.breakingbedrock.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SignatureException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for encoding and decoding data.
 */
public interface EncodingUtils {
    /**
     * Encodes a byte array to Base64.
     * @param data The data.
     * @return The Base64-encoded data.
     */
    static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Encodes a byte array to Base64.
     *
     * @param data The data.
     * @param url Whether to use URL-safe encoding.
     * @return The Base64-encoded data.
     */
    static String base64Encode(byte[] data, boolean url) {
        return url ? Base64.getUrlEncoder().encodeToString(data) : base64Encode(data);
    }

    /**
     * Decodes a Base64-encoded string.
     * @param data The Base64-encoded data.
     * @return The decoded data.
     */
    static String base64Decode(byte[] data) {
        return new String(Base64.getDecoder().decode(data));
    }

    /**
     * Decodes a Base64-encoded string.
     * @param data The Base64-encoded data.
     * @return The decoded data.
     */
    static String base64Decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }

    /**
     * Decodes a Base64-encoded string.
     * @param data The Base64-encoded data.
     * @return The decoded data.
     */
    static byte[] base64DecodeToBytes(String data) {
        return Base64.getDecoder().decode(data);
    }

    /**
     * Converts an object into a JSON element.
     *
     * @param object The object.
     * @return The JSON element.
     */
    static JsonElement toJson(Object object) {
        return BreakingBedrock.getGson().toJsonTree(object);
    }

    /**
     * Encodes an object into a JSON object.
     * @param object The object.
     * @return The JSON object.
     */
    static String jsonEncode(Object object) {
        return BreakingBedrock.getGson().toJson(object);
    }

    /**
     * Decodes a JSON object into an object.
     * @param json The JSON object.
     * @return The object.
     */
    static <T> T jsonDecode(String json, Class<T> type) {
        return BreakingBedrock.getGson().fromJson(json, type);
    }

    /**
     * Decodes a JSON file into an object.
     *
     * @param file The JSON file.
     * @param type The object type.
     * @return The object.
     */
    static <T> T jsonDecode(File file, Class<T> type) throws IOException {
        return BreakingBedrock.getGson().fromJson(new FileReader(file), type);
    }

    /**
     * Converts a big integer (long) to a byte array.
     * @param integer The integer.
     * @return The byte array.
     */
    static byte[] longToBytes(BigInteger integer) {
        var array = integer.toByteArray();
        if (array[0] == 0) {
            var newArray = new byte[array.length - 1];
            System.arraycopy(array, 1, newArray, 0, newArray.length);
            return newArray;
        }

        return array;
    }

    /**
     * Create a JOSE signature from a DER signature.
     * @param signature The DER signature.
     * @param algorithmType The algorithm type.
     * @return The JOSE signature.
     */
    static byte[] derToJose(byte[] signature, AlgorithmType algorithmType) throws SignatureException{
        var derEncoded = signature[0] == 0x30 && signature.length != algorithmType.ecNumberSize * 2;
        if (!derEncoded) {
            throw new SignatureException("Invalid DER signature format.");
        }

        var joseSignature = new byte[algorithmType.ecNumberSize * 2];

        // Skip 0x30.
        var offset = 1;
        if (signature[1] == (byte) 0x81) {
            // Skip sign.
            offset++;
        }

        // Convert to unsigned. Should match DER length - offset.
        var encodedLength = signature[offset++] & 0xff;
        if (encodedLength != signature.length - offset) {
            throw new SignatureException("Invalid DER signature format.");
        }

        // Skip 0x02.
        offset++;

        // Obtain R number length (Includes padding) and skip it.
        var rLength = signature[offset++];
        if (rLength > algorithmType.ecNumberSize + 1) {
            throw new SignatureException("Invalid DER signature format.");
        }
        var rPadding = algorithmType.ecNumberSize - rLength;
        // Retrieve R number.
        System.arraycopy(signature, offset + Math.max(-rPadding, 0), joseSignature, Math.max(rPadding, 0), rLength + Math.min(rPadding, 0));

        // Skip R number and 0x02.
        offset += rLength + 1;

        // Obtain S number length. (Includes padding)
        var sLength = signature[offset++];
        if (sLength > algorithmType.ecNumberSize + 1) {
            throw new SignatureException("Invalid DER signature format.");
        }
        var sPadding = algorithmType.ecNumberSize - sLength;

        // Retrieve R number.
        System.arraycopy(signature, offset + Math.max(-sPadding, 0), joseSignature, algorithmType.ecNumberSize + Math.max(sPadding, 0), sLength + Math.min(sPadding, 0));

        return joseSignature;
    }

    /**
     * Prepends the additional data to the message.
     * @param source The source.
     * @param additional The additional data.
     * @return The prepended data.
     */
    static JsonArray prepend(JsonArray source, JsonElement additional) {
        var result = new JsonArray();
        result.add(additional);
        result.addAll(source);
        return result;
    }

    /**
     * Un-zips a file.
     *
     * @param source The source file.
     * @param output The output directory.
     * @throws IOException If an I/O error occurs.
     */
    static void unzip(File source, File output) throws IOException {
        try (var zipStream = new ZipInputStream(new FileInputStream(source))) {
            var entry = zipStream.getNextEntry();
            while (entry != null) {
                var filePath = output.toPath().resolve(entry.getName());
                if (!entry.isDirectory()) {
                    var parent = filePath.getParent();
                    if (parent != null) {
                        if (!parent.toFile().exists() && !parent.toFile().mkdirs())
                            throw new IOException("Failed to create directory: " + parent);
                    }

                    try (var outputStream = new java.io.FileOutputStream(filePath.toFile())) {
                        var buffer = new byte[1024];
                        int length;
                        while ((length = zipStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                } else {
                    if (!filePath.toFile().exists() && !filePath.toFile().mkdirs())
                        throw new IOException("Failed to create directory: " + filePath);
                }

                zipStream.closeEntry();
                entry = zipStream.getNextEntry();
            }
        } catch (Exception ignored) {
            BreakingBedrock.getLogger().warn("Unable to unzip file: " + source.getAbsolutePath());
        }
    }

    /**
     * Zips a file.
     *
     * @param source The source file.
     * @param output The output file.
     * @throws IOException If an I/O error occurs.
     */
    static void zip(File source, File output) throws IOException {
        var path = Files.createFile(output.toPath());
        try (var zs = new ZipOutputStream(Files.newOutputStream(path))) {
            var sourcePath = source.toPath();
            Files.walk(sourcePath)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(p -> {
                        var zipEntry = new ZipEntry(sourcePath.relativize(p).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(p, zs);
                            zs.closeEntry();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
        }
    }
}

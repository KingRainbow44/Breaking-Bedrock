package lol.magix.breakingbedrock.utils;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Base64;

/**
 * Utility class for encoding and decoding data.
 */
public interface EncodingUtils {
    /**
     * Reads an input stream into text.
     * @param stream The input stream.
     * @return The text.
     */
    static String readStream(InputStream stream) throws IOException {
        var builder = new StringBuilder();
        var reader = new BufferedReader(new InputStreamReader(stream));
        String line; while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    /**
     * Encodes a byte array to Base64.
     * @param data The data.
     * @return The Base64-encoded data.
     */
    static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
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
     * Attempts to encode a packet.
     * @param packet The packet.
     * @return An encoded packet.
     */
    static String encodePacket(BedrockPacket packet) {
        var gson = BreakingBedrock.getGson();
        var encoded = gson.toJson(packet);
        if (encoded.length() < 1000)
            return encoded;
        if (encoded.length() > 3000)
            return packet.getClass().getCanonicalName();
        return encoded.substring(0, 1000) + "...";
    }
}
package lol.magix.breakingbedrock.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lol.magix.breakingbedrock.objects.Triplet;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;
import lombok.SneakyThrows;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Methods to help when handling JWTs.
 */
public interface CryptoUtils {
    /* The Base64 encoder for handling JWTs. */
    Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    /* The Gson encoder for handling JWTs. */
    Gson GSON = new Gson();

    /**
     * Parses a public key from the given string.
     * @param encodedPublicKey A Base64-encoded
     * @return A {@link ECPublicKey} object.
     */
    @SneakyThrows
    static ECPublicKey parsePublicKey(String encodedPublicKey) {
        // Decode the key into bytes.
        var keyBytes = EncodingUtils.base64DecodeToBytes(encodedPublicKey);
        // Create a public key from the bytes.
        var keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    /**
     * Parses a JWT token.
     * @param token The token to parse.
     * @return A triplet containing the header, payload, and signature.
     */
    static Triplet<JsonObject, JsonObject, String> parseJwt(String token) {
        // Split the token into its parts.
        var parts = token.split("\\.");
        // Decode the header and payload.
        var header = GSON.fromJson(new String(Base64.getUrlDecoder().decode(parts[0])), JsonObject.class);
        var payload = GSON.fromJson(new String(Base64.getUrlDecoder().decode(parts[1])), JsonObject.class);
        // Return the header, payload, and signature.
        return new Triplet<>(header, payload, parts[2]);
    }

    /**
     * Signs a JWT token.
     * @param payloadData The payload to sign.
     * @param publicKey The public key to sign with.
     * @param privateKey The private key to sign with.
     * @return A signed JWT token.
     */
    static String signJwt(JsonObject payloadData, ECPublicKey publicKey, ECPrivateKey privateKey) {
        // Create a JWT header.
        var headerData = new JsonObject();
        headerData.addProperty("alg", "ES384");
        headerData.addProperty("x5u", EncodingUtils.base64Encode(publicKey.getEncoded()));
        var header = GSON.toJson(headerData);
        header = BASE64_ENCODER.encodeToString(header.getBytes());
        // Create a JWT payload.
        var payload = GSON.toJson(payloadData);
        payload = BASE64_ENCODER.encodeToString(payload.getBytes());

        // Create a header + payload token.
        var token = header + "." + payload;
        // Create a signature for the token.
        var signature = CryptoUtils.createSignature(token, privateKey);

        // Return the signed token.
        return token + "." + signature;
    }

    /**
     * Creates a JWT signature.
     * @param token The token to sign.
     * @param privateKey The private key to sign with.
     * @return A JWT signature.
     */
    @SneakyThrows
    static String createSignature(String token, ECPrivateKey privateKey) {
        var signature = Signature.getInstance("SHA384withECDSA");
        signature.initSign(privateKey); signature.update(token.getBytes());

        var signatureBytes = signature.sign();
        var asJose = EncodingUtils.derToJose(signatureBytes, AlgorithmType.ECDSA384);
        return BASE64_ENCODER.encodeToString(asJose);
    }
}

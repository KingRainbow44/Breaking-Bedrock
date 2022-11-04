package lol.magix.breakingbedrock.network.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lombok.Getter;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles authentication with a Bedrock server.
 */
public final class Authentication {
    private static final KeyPairGenerator KEY_PAIR_GENERATOR;

    static {
        try {
            // Initialize a new key pair generator.
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("EC");
            KEY_PAIR_GENERATOR.initialize(new ECGenParameterSpec("secp256r1"));
        } catch (Exception exception) {
            throw new AssertionError("Unable to initialize key pair generator", exception);
        }
    }

    @Getter private ECPrivateKey privateKey;
    @Getter private ECPublicKey publicKey;

    @Getter private String xuid;
    @Getter private String displayName;
    @Getter private UUID identity;

    /**
     * Generates data for online authentication.
     * Makes requests to the Xbox Live API.
     * @return Authentication data. (JSON-encoded)
     * @throws Exception If a cryptographic error occurs.
     */
    public String getOnlineChainData() throws Exception {
        var gson = BreakingBedrock.getGson();

        // Generate a key pair for Xbox Live.
        var ecdsa256KeyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        this.publicKey = (ECPublicKey) ecdsa256KeyPair.getPublic();
        this.privateKey = (ECPrivateKey) ecdsa256KeyPair.getPrivate();
        // Get Xbox information.
        var xbox = new Xbox(System.getProperty("XboxAccessToken"));
        var userToken = xbox.getUserToken(this.publicKey, this.privateKey);
        var deviceToken = xbox.getDeviceToken(this.publicKey, this.privateKey);
        var titleToken = xbox.getTitleToken(deviceToken, this.publicKey, this.privateKey);
        var xsts = xbox.getXSTSToken(userToken, deviceToken, titleToken, this.publicKey, this.privateKey);

        // Generate a key pair for the Bedrock server.
        var ecdsa384KeyPair = EncryptionUtils.createKeyPair();
        this.publicKey = (ECPublicKey) ecdsa384KeyPair.getPublic();
        this.privateKey = (ECPrivateKey) ecdsa384KeyPair.getPrivate();
        // Get Minecraft information.
        var chainData = xbox.requestMinecraftChain(xsts, this.publicKey);
        var chainDataJson = gson.fromJson(chainData, JsonObject.class);
        // Extract chain data.
        var networkChain = chainDataJson.getAsJsonArray("chain");
        var chainHeader = networkChain.get(0).getAsString();
        chainHeader = chainHeader.split("\\.")[0]; // Get the JWT header. (Base64-encoded)
        chainHeader = EncodingUtils.base64Decode(chainHeader.getBytes());
        var x5uKey = gson.fromJson(chainHeader, JsonObject.class).get("x5u").getAsString();

        // Create a replacement chain.
        var newChain = new JsonObject();
        newChain.addProperty("certificateAuthority", true);
        newChain.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
        newChain.addProperty("identityPublicKey", x5uKey);
        newChain.addProperty("nbf", Instant.now().getEpochSecond() - TimeUnit.HOURS.toSeconds(6));

        {
            // Encode the public key.
            var encodedPublicKey = EncodingUtils.base64Encode(this.publicKey.getEncoded());
            // Create a JWT header.
            var jwtHeader = new JsonObject();
            jwtHeader.addProperty("alg", "ES384");
            jwtHeader.addProperty("x5u", encodedPublicKey);
            // Create a JWT payload.
            var encoder = Base64.getUrlEncoder().withoutPadding();
            var header = encoder.encodeToString(gson.toJson(jwtHeader).getBytes());
            var payload = encoder.encodeToString(gson.toJson(newChain).getBytes());

            // Sign the payload & header.
            var dataToSign = (header + "." + payload).getBytes();
            var signature = this.signBytes(dataToSign);
            var jwt = header + "." + payload + "." + signature;

            // Add the JWT to the chain.
            chainDataJson.add("chain", this.prependChain(networkChain, jwt));
        }

        {
            // Extract chain data.
            var lastChain = networkChain.get(networkChain.size() - 1).getAsString();
            var lastPayload = lastChain.split("\\.")[1]; // Get the JWT payload. (Base64-encoded)
            lastPayload = EncodingUtils.base64Decode(lastPayload.getBytes()); // Decode the payload.
            // Extract extra data.
            var payloadObject = gson.fromJson(lastPayload, JsonObject.class);
            var extraData = payloadObject.getAsJsonObject("extraData");
            // Extract remaining data.
            this.xuid = extraData.get("XUID").getAsString();
            this.identity = UUID.fromString(extraData.get("identity").getAsString());
            this.displayName = extraData.get("displayName").getAsString();
        }

        return gson.toJson(chainDataJson);
    }

    /**
     * Generates data for offline authentication.
     * Uses a username to create a UUID.
     * @param username The username to use.
     * @return Authentication data. (JSON-encoded)
     */
    public String getOfflineChainData(String username) throws Exception {
        var gson = BreakingBedrock.getGson();

        // Generate a UUID & XUID.
        var uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
        var xuid = Long.toString(uuid.getLeastSignificantBits());

        // Generate a key pair for the Bedrock server.
        var ecdsa384KeyPair = EncryptionUtils.createKeyPair();
        this.publicKey = (ECPublicKey) ecdsa384KeyPair.getPublic();
        this.privateKey = (ECPrivateKey) ecdsa384KeyPair.getPrivate();
        // Encode the public key.
        var encodedPublicKey = EncodingUtils.base64Encode(this.publicKey.getEncoded());

        // Create a JWT payload.
        var jwtPayload = new JsonObject();
        jwtPayload.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
        jwtPayload.addProperty("identityPublicKey", encodedPublicKey);
        jwtPayload.addProperty("nbf", Instant.now().getEpochSecond() - TimeUnit.HOURS.toSeconds(6));
        // Create extra data for the JWT payload.
        var extraData = new JsonObject();
        extraData.addProperty("titleId", "896928775"); // Minecraft: Windows 10 Edition title ID.
        extraData.addProperty("identity", uuid.toString());
        extraData.addProperty("displayName", username);
        extraData.addProperty("XUID", xuid);
        jwtPayload.add("extraData", extraData);

        // Create a JWT header.
        var jwtHeader = new JsonObject();
        jwtHeader.addProperty("alg", "ES384");
        jwtHeader.addProperty("x5u", encodedPublicKey);
        // Create a JWT payload.
        var encoder = Base64.getUrlEncoder().withoutPadding();
        var header = encoder.encodeToString(gson.toJson(jwtHeader).getBytes());
        var payload = encoder.encodeToString(gson.toJson(jwtPayload).getBytes());

        // Sign the payload & header.
        var dataToSign = (header + "." + payload).getBytes();
        var signature = this.signBytes(dataToSign);
        var jwt = header + "." + payload + "." + signature;

        // Create a chain.
        var chainArray = new JsonArray();
        chainArray.add(jwt);
        var chain = new JsonObject();
        chain.add("chain", chainArray);

        // Set the identity & XUID.
        this.xuid = xuid;
        this.identity = uuid;
        this.displayName = username;

        return gson.toJson(chain);
    }

    /*
     * Utility methods.
     */

    /**
     * Signs bytes using the stored private key.
     * @param bytes Bytes to sign.
     * @return Signed bytes.
     */
    public String signBytes(byte[] bytes) throws Exception {
        // Create a signature.
        var signature = Signature.getInstance("SHA384withECDSA");
        signature.initSign(this.privateKey);
        signature.update(bytes);

        // Sign & encode the data.
        var signatureBytes = EncodingUtils.derToJose(signature.sign(), AlgorithmType.ECDSA384);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    /**
     * Prepends a chain to a chain array.
     * @param target Target chain array.
     * @param chain Chain to prepend.
     * @return New chain array.
     */
    private JsonArray prependChain(JsonArray target, String chain) {
        // Create new and prepend.
        var array = new JsonArray();
        array.add(chain);
        // Add existing elements.
        for (var element : target) {
            array.add(element);
        }

        return array;
    }
}

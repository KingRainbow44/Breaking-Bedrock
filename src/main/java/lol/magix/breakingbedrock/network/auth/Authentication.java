package lol.magix.breakingbedrock.network.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;
import lol.magix.breakingbedrock.utils.CryptoUtils;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
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
    @Getter private ECPrivateKey xblPrivateKey;
    @Getter private ECPublicKey xblPublicKey;

    @Getter private String xuid;
    @Getter private String displayName;
    @Getter private UUID identity;

    /**
     * Generates data for online authentication.
     * @return Authentication data. (JSON-encoded)
     */
    public List<String> getOnlineChainData() {
        var gson = BreakingBedrock.getGson();

        // Generate an Xbox Live keypair.
        var ecdsa256KeyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        this.xblPublicKey = (ECPublicKey) ecdsa256KeyPair.getPublic();
        this.xblPrivateKey = (ECPrivateKey) ecdsa256KeyPair.getPrivate();

        // Generate a Minecraft: Bedrock keypair.
        var ecdsa384KeyPair = EncryptionUtils.createKeyPair();
        this.publicKey = (ECPublicKey) ecdsa384KeyPair.getPublic();
        this.privateKey = (ECPrivateKey) ecdsa384KeyPair.getPrivate();

        // Get login chain data from Minecraft's authentication API.
        var xboxAuth = new XboxV2(System.getProperty("XboxAccessToken"),
                this.xblPublicKey, this.xblPrivateKey, this.publicKey);
        xboxAuth.obtainDeviceToken(); // Obtain a device token.
        xboxAuth.obtainAuthToken("https://multiplayer.minecraft.net/"); // Obtain an auth token.
        var chainData = xboxAuth.getChainData(); // Obtain chain data.

        var keyPairs = new JsonObject();
        keyPairs.addProperty("private-xbl", EncodingUtils.base64Encode(this.xblPrivateKey.getEncoded()));
        keyPairs.addProperty("public-xbl", EncodingUtils.base64Encode(this.xblPublicKey.getEncoded()));
        keyPairs.addProperty("private-bedrock", EncodingUtils.base64Encode(this.privateKey.getEncoded()));
        keyPairs.addProperty("public-bedrock", EncodingUtils.base64Encode(this.publicKey.getEncoded()));

        // Decode the chain data.
        var chainDataJson = gson.fromJson(chainData, JsonObject.class);
        var chains = chainDataJson.getAsJsonArray("chain");

        // Parse the first chain.
        var firstChain = chains.get(0).getAsString();
        var firstChainData = firstChain.split("\\.");
        var firstChainHeader = EncodingUtils.base64Decode(firstChainData[0]);

        // Extract the public key (x5u) from the first chain.
        var firstChainHeaderJson = gson.fromJson(firstChainHeader, JsonObject.class);
        var x5uKey = firstChainHeaderJson.get("x5u").getAsString();

        {
            // Create a chain.
            var newChain = new JsonObject();
            var currentTime = Instant.now().getEpochSecond();
            var newTime = TimeUnit.HOURS.toSeconds(6);

            newChain.addProperty("iss", "BreakingBedrock");
            newChain.addProperty("exp", currentTime + newTime);
            newChain.addProperty("nbf", currentTime - newTime);
            newChain.addProperty("identityPublicKey", x5uKey);
            newChain.addProperty("certificateAuthority", true);
            // Sign the newly created chain.
            var signedChain = CryptoUtils.signJwt(newChain, this.publicKey, this.privateKey);

            // Create a new chain link.
            chains = EncodingUtils.prepend(chains, new JsonPrimitive(signedChain));
            chainDataJson.add("chain", chains);
        }

        {
            // Extract player information.
            var playerInfoChain = chains.get(chains.size() - 1).getAsString();
            var playerInfoChainData = CryptoUtils.parseJwt(playerInfoChain);
            var playerInfoPayload = playerInfoChainData.b();

            var extraData = playerInfoPayload.getAsJsonObject("extraData");
            this.xuid = extraData.get("XUID").getAsString();
            this.identity = UUID.fromString(extraData.get("identity").getAsString());
            this.displayName = extraData.get("displayName").getAsString();
        }

        // Return the updated chain data.
        return chains.asList().stream()
                .map(element -> element.toString()
                        .replaceAll("\"", ""))
                .toList();
    }

    /**
     * Generates data for offline authentication.
     * Uses a username to create a UUID.
     * @param username The username to use.
     * @return Authentication data. (JSON-encoded)
     */
    public List<String> getOfflineChainData(String username) throws Exception {
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

        // Set the identity & XUID.
        this.xuid = xuid;
        this.identity = uuid;
        this.displayName = username;

        return chainArray.asList().stream()
                .map(JsonElement::toString)
                .map(s -> s.replace("\"", ""))
                .toList();
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
        return this.signBytes(bytes, this.privateKey);
    }

    /**
     * Signs bytes using the specified private key.
     * @param bytes Bytes to sign.
     * @return Signed bytes.
     */
    public String signBytes(byte[] bytes, ECPrivateKey privateKey) throws Exception {
        // Create a signature.
        var signature = Signature.getInstance("SHA384withECDSA");
        signature.initSign(privateKey);
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

package lol.magix.breakingbedrock.network.auth;

import com.google.common.primitives.Longs;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.NetworkUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * An Xbox account wrapper.
 */
public final class Xbox {
    /**
     * TODO: Generate automatically when user wants to sign in.
     * Currently, use <a href="https://bit.ly/breakingbedrock">...</a> to generate a token.
     * When launching the game, add <code>-DXboxAccessToken=</code> to the JVM arguments.
     */
    private final String accessToken;

    /**
     * Creates a new Xbox account wrapper.
     * @param accessToken The access token.
     */
    public Xbox(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Creates a user token.
     * @param publicKey The public key.
     * @param privateKey The private key.
     * @return The user token.
     */
    public String getUserToken(ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
        // Create token data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        // Create authentication request.
        var properties = new JsonObject();
        jsonObject.add("Properties", properties);
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "t=" + this.accessToken);
        // Create signature.
        var proofKey = this.createProofKey(publicKey);
        properties.add("ProofKey", proofKey);

        // Perform request.
        var url = new URL(NetworkConstants.XBOX_USER_AUTH);
        var request = (HttpsURLConnection) url.openConnection();
        this.setupRequest(request, jsonObject, privateKey);

        // Parse the response.
        var response = EncodingUtils.readStream(request.getInputStream());
        var jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse.get("Token").getAsString();
    }

    /**
     * Creates a device token.
     * @param publicKey The public key.
     * @param privateKey The private key.
     * @return The device token.
     */
    public String getDeviceToken(ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
        // Create token data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        // Create device properties.
        var properties = new JsonObject();
        jsonObject.add("Properties", properties);
        properties.addProperty("AuthMethod", "ProofOfPossession");
        properties.addProperty("DeviceType", "Nintendo");
        properties.addProperty("Id", UUID.randomUUID().toString());
        properties.addProperty("SerialNumber", UUID.randomUUID().toString());
        properties.addProperty("Version", "0.0.0.0");
        // Create signature.
        var proofKey = this.createProofKey(publicKey);
        properties.add("ProofKey", proofKey);

        // Perform request.
        var url = new URL(NetworkConstants.XBOX_DEVICE_AUTH);
        var request = (HttpsURLConnection) url.openConnection();
        this.setupRequest(request, jsonObject, privateKey);

        // Parse the response.
        var response = EncodingUtils.readStream(request.getInputStream());
        var jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse.get("Token").getAsString();
    }

    /**
     * Creates a title token.
     * @param deviceToken The device token.
     * @param publicKey The public key.
     * @param privateKey The private key.
     * @return The title token.
     */
    public String getTitleToken(String deviceToken, ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
        // Create token data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        // Create device properties.
        var properties = new JsonObject();
        jsonObject.add("Properties", properties);
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("DeviceToken", deviceToken);
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "t=" + this.accessToken);
        // Create signature.
        var proofKey = this.createProofKey(publicKey);
        properties.add("ProofKey", proofKey);

        // Perform request.
        var url = new URL(NetworkConstants.XBOX_TITLE_AUTH);
        var request = (HttpsURLConnection) url.openConnection();
        this.setupRequest(request, jsonObject, privateKey);

        // Check the response.
        NetworkUtils.checkResponse(request);

        // Parse the response.
        var response = EncodingUtils.readStream(request.getInputStream());
        var jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse.get("Token").getAsString();
    }

    /**
     * Generates an XSTS token.
     * @param userToken The user token.
     * @param deviceToken The device token.
     * @param titleToken The title token.
     * @param publicKey The public key.
     * @param privateKey The private key.
     * @return The XSTS token.
     */
    public String getXSTSToken(String userToken, String deviceToken, String titleToken, ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
        // Create token data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", "https://multiplayer.minecraft.net/");
        jsonObject.addProperty("TokenType", "JWT");
        // Add user tokens.
        var userTokens = new JsonArray();
        userTokens.add(userToken);
        // Create device properties.
        var properties = new JsonObject();
        jsonObject.add("Properties", properties);
        properties.addProperty("DeviceToken", deviceToken);
        properties.addProperty("TitleToken", titleToken);
        properties.add("UserTokens", userTokens);
        properties.addProperty("SandboxId", "RETAIL");
        // Create signature.
        var proofKey = this.createProofKey(publicKey);
        properties.add("ProofKey", proofKey);

        // Perform request.
        var url = new URL(NetworkConstants.XBOX_AUTHORIZE);
        var request = (HttpsURLConnection) url.openConnection();
        this.setupRequest(request, jsonObject, privateKey);

        // Parse the response.
        return EncodingUtils.readStream(request.getInputStream());
    }

    /**
     * Creates a Minecraft token.
     * @param xsts The XSTS token.
     * @param publicKey The public key.
     * @return The Minecraft token.
     */
    public String requestMinecraftChain(String xsts, ECPublicKey publicKey) throws Exception {
        // Parse input arguments.
        var xstsObject = JsonParser.parseString(xsts).getAsJsonObject();
        var pubKeyData = EncodingUtils.base64Encode(publicKey.getEncoded());

        // Create body data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("identityPublicKey", pubKeyData);

        // Perform request.
        var url = new URL(NetworkConstants.MINECRAFT_AUTH);
        var connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "XBL3.0 x=" + xstsObject.get("DisplayClaims").getAsJsonObject()
                .getAsJsonArray("xui").getAsJsonArray()
                .get(0).getAsJsonObject().get("uhs").getAsString()
                + ";" + xstsObject.get("Token").getAsString());
        connection.setRequestProperty("User-Agent", "MCPE/UWP");
        connection.setRequestProperty("Client-Version", NetworkConstants.PACKET_CODEC.getMinecraftVersion());
        this.postJsonObject(connection, jsonObject);

        // Parse the response.
        return EncodingUtils.readStream(connection.getInputStream());
    }

    /*
     * Utility methods.
     */

    /**
     * Adds the signature header to the request.
     * @param request The connection.
     * @param data The post data.
     * @param privateKey The private key.
     */
    private void addSignatureHeader(HttpsURLConnection request, JsonObject data, ECPrivateKey privateKey) throws Exception {
        var currentTime = NetworkUtils.getOffsetTimestamp();
        var bytesToSign = new ByteArrayOutputStream();

        bytesToSign.write(new byte[] { 0, 0, 0, 1, 0 });
        bytesToSign.write(Longs.toByteArray(currentTime));
        bytesToSign.write(new byte[] { 0 });

        bytesToSign.write("POST".getBytes());
        bytesToSign.write(new byte[] { 0 });
        var query = request.getURL().getQuery();
        if (query == null) {
            query = "";
        }
        bytesToSign.write((request.getURL().getPath() + query).getBytes());
        bytesToSign.write(new byte[] { 0 });
        var authorization = request.getRequestProperty("Authorization");
        if (authorization == null) {
            authorization = "";
        }
        bytesToSign.write(authorization.getBytes());
        bytesToSign.write(new byte[] { 0 });
        bytesToSign.write(BreakingBedrock.getGson().toJson(data).getBytes());
        bytesToSign.write(new byte[] { 0 });

        var signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(bytesToSign.toByteArray());
        var signatureBytes = EncodingUtils.derToJose(signature.sign(), AlgorithmType.ECDSA256);

        var byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(new byte[] { 0, 0, 0, 1 });
        byteArrayOutputStream.write(Longs.toByteArray(currentTime));
        byteArrayOutputStream.write(signatureBytes);
        request.addRequestProperty("Signature", EncodingUtils.base64Encode(byteArrayOutputStream.toByteArray()));
    }

    /**
     * Attempts to write the JSON object to the POST request.
     * @param request The request.
     * @param body The body.
     * @throws IOException If an I/O error occurs.
     */
    private void postJsonObject(HttpsURLConnection request, JsonObject body) throws IOException {
        // Validate the body data.
        var bodyData = BreakingBedrock.getGson().toJson(body);
        // Substitute any encoded-unicode strings with their originals.
        var matcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(bodyData);
        while (matcher.find()) {
            var codePoint = Integer.parseInt(matcher.group(1), 16);
            bodyData = bodyData.replace(matcher.group(), new String(Character.toChars(codePoint)));
        }

        // Enable body writing.
        request.setDoOutput(true);
        // Write the data to the request.
        var writer = request.getOutputStream();
        writer.write(bodyData.getBytes());
        writer.flush();
        writer.close();
    }

    /**
     * Generates a proof key from the public key.
     * @param publicKey The public key.
     * @return The proof key.
     */
    private JsonObject createProofKey(ECPublicKey publicKey) {
        var proofKey = new JsonObject();
        proofKey.addProperty("crv", "P-256");
        proofKey.addProperty("alg", "ES256");
        proofKey.addProperty("use", "sig");
        proofKey.addProperty("kty", "EC");

        var key = NetworkUtils.getProofKey(publicKey);
        proofKey.addProperty("x", key.a());
        proofKey.addProperty("y", key.b());

        return proofKey;
    }

    /**
     * Sets up a request object.
     * @param request The request.
     * @param object The object.
     * @param privateKey The private key.
     */
    private void setupRequest(HttpsURLConnection request, JsonObject object, ECPrivateKey privateKey) throws Exception {
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type", "application/json");
        request.setRequestProperty("x-xbl-contract-version", "1");
        this.addSignatureHeader(request, object, privateKey);
        this.postJsonObject(request, object);
    }
}
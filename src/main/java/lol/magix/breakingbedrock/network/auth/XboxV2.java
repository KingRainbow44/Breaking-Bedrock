package lol.magix.breakingbedrock.network.auth;

import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.absolute.AlgorithmType;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.definitions.XboxConnectToken;
import lol.magix.breakingbedrock.objects.definitions.XboxDeviceConnect;
import lol.magix.breakingbedrock.utils.DeviceUtils;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.NetworkUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;

/**
 * An Xbox authorization wrapper.
 * Updated for Minecraft: Bedrock Edition (latest).
 * Based on Sandertv/gophertunnel
 */
public final class XboxV2 {
    /**
     * Attempts to fetch an access token.
     * @return The access token.
     */
    @SneakyThrows
    public static String getAccessToken() {
        // Check if an access token exists.
        var tokenFile = new File(BreakingBedrock.getDataDirectory(), "token.txt");
        if (tokenFile.exists()) {
            var token = Files.readString(tokenFile.toPath());
            if (token != null && !token.isEmpty())
                return token;
        }

        var gson = BreakingBedrock.getGson();

        // Create a form body for initializing a client connection.
        var body = new FormBody.Builder()
                .add("client_id", NetworkConstants.XBOX_ANDROID_CID)
                .add("scope", NetworkConstants.XBOX_AUTH_SCOPES)
                .add("response_type", NetworkConstants.XBOX_AUTH_RES_TYPE)
                .build();

        // Send request for initializing a client connection.
        var request = new Request.Builder()
                .url(NetworkConstants.XBOX_CONNECT_START)
                .method("POST", body)
                .build();

        // Get the response.
        try (var response = BreakingBedrock.getHttpClient().newCall(request).execute()) {
            if (response.code() != 200)
                throw new IllegalStateException("Unable to initialize Xbox Live connection: " + response.code());

            // Decode the response.
            var responseBody = response.body(); assert responseBody != null;
            var responseJson = gson.fromJson(responseBody.string(), XboxDeviceConnect.class);

            // Prompt the user to open a URL.
            var userCode = responseJson.getCode();
            var userUrl = responseJson.getRedirectUri();
            DeviceUtils.copyToClipboard(userCode);
            DeviceUtils.openUrl(userUrl);

            // Wait for the user to authorize the application.
            var interval = responseJson.getInterval();
            var tokens = new Pair<>("", "");
            var authorized = false;

            // Wait for authorization.
            while (!authorized) {
                // Check if the user has authorized the application.
                var tokenBody = new FormBody.Builder()
                        .add("client_id", NetworkConstants.XBOX_ANDROID_CID)
                        .add("grant_type", NetworkConstants.XBOX_AUTH_GRANTS)
                        .add("device_code", responseJson.getFetchToken());
                var tokenRequest = new Request.Builder()
                        .url(NetworkConstants.XBOX_CONNECT_TOKEN)
                        .method("POST", tokenBody.build())
                        .build();

                try (var tokenResponse = BreakingBedrock.getHttpClient().newCall(tokenRequest).execute()) {
                    if (tokenResponse.code() != 200) {
                        // Wait the interval.
                        Thread.sleep(interval * 1000L);
                    } else {
                        // Decode the response.
                        var tokenResponseBody = tokenResponse.body(); assert tokenResponseBody != null;
                        var tokenResponseJson = gson.fromJson(tokenResponseBody.string(), XboxConnectToken.class);
                        tokens = new Pair<>(tokenResponseJson.getToken(), tokenResponseJson.getRefresh());
                        authorized = true;
                    }
                }
            }

            // Get the access token.
            var token = tokens.a();

            // Save the access token.
            Files.writeString(tokenFile.toPath(), token);
            // Return the access token.
            return token;
        }
    }

    private final String accessToken;
    private final ECPublicKey xblPublicKey;
    private final ECPrivateKey xblPrivateKey;

    private final ECPublicKey clientPublicKey;

    @Getter private String deviceToken = "";
    @Getter private JsonObject authToken = null;

    /**
     * Creates a new XboxV2 instance.
     * @param accessToken The access token to use.
     * @param xblPublicKey The public key to use. (XBox Live)
     * @param xblPrivateKey The private key to use. (XBox Live)
     * @param clientPublicKey The client public key to use.
     */
    public XboxV2(
            String accessToken, ECPublicKey xblPublicKey,
            ECPrivateKey xblPrivateKey, ECPublicKey clientPublicKey
    ) {
        this.accessToken = accessToken;
        this.xblPublicKey = xblPublicKey;
        this.xblPrivateKey = xblPrivateKey;
        this.clientPublicKey = clientPublicKey;
    }

    /**
     * Performs a POST request to the URL.
     * @param url The URL to send the request to.
     * @param body The body to send.
     * @return The response.
     */
    @SneakyThrows
    private JsonObject doRequest(String url, JsonObject body) {
        var urlObject = new URL(url);
        var path = urlObject.getPath();
        var query = urlObject.getQuery();
        var urlData = new Pair<>(path, query);
        var encodedBody = EncodingUtils.jsonEncode(body);

        // Create a request from the URL.
        var request = new Request.Builder()
                .url(urlObject)
                .method("POST", RequestBody.create(encodedBody, MediaType.get("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Signature", this.generateSignature(urlData, null, body))
                .addHeader("x-xbl-contract-version", "1")
                .build();

        // Perform the request.
        try (var response = BreakingBedrock.getHttpClient().newCall(request).execute()) {
            // Validate the response code.
            if (response.code() != 200)
                return null;

            // Validate the response body.
            var responseBody = response.body();
            if (responseBody == null)
                return null;

            // Parse into a JSON object then return.
            return JsonParser.parseString(responseBody.string()).getAsJsonObject();
        }
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
     * Adds the signature header to the request.
     * @param url The request path + query.
     * @param auth The authorization header.
     * @param body The body data.
     */
    private String generateSignature(Pair<String, String> url, String auth, JsonObject body) throws Exception {
        var currentTime = NetworkUtils.getOffsetTimestamp();
        var bytesToSign = new ByteArrayOutputStream();

        var path = url.a();
        var query = url.b();

        if (query == null) query = "";
        if (auth == null) auth = "";

        bytesToSign.write(new byte[] { 0, 0, 0, 1, 0 });
        bytesToSign.write(Longs.toByteArray(currentTime));
        bytesToSign.write(new byte[] { 0 });

        bytesToSign.write("POST".getBytes());
        bytesToSign.write(new byte[] { 0 });
        bytesToSign.write((path + query).getBytes());
        bytesToSign.write(new byte[] { 0 });
        bytesToSign.write(auth.getBytes());
        bytesToSign.write(new byte[] { 0 });
        bytesToSign.write(BreakingBedrock.getGson().toJson(body).getBytes());
        bytesToSign.write(new byte[] { 0 });

        var signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(this.xblPrivateKey);
        signature.update(bytesToSign.toByteArray());
        var signatureBytes = EncodingUtils.derToJose(signature.sign(), AlgorithmType.ECDSA256);

        var byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(new byte[] { 0, 0, 0, 1 });
        byteArrayOutputStream.write(Longs.toByteArray(currentTime));
        byteArrayOutputStream.write(signatureBytes);

        return EncodingUtils.base64Encode(byteArrayOutputStream.toByteArray());
    }

    /*
     * Obtain credentials.
     */

    /**
     * Obtains an Xbox Live token.
     * @param relyingParty The endpoint which requires this token.
     */
    @SneakyThrows
    public void obtainAuthToken(String relyingParty) {
        // Create JSON body.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", relyingParty);
        jsonObject.addProperty("SiteName", "user.auth.xboxlive.com");
        jsonObject.addProperty("Sandbox", "RETAIL");
        jsonObject.addProperty("UseModernGamertag", true);
        jsonObject.addProperty("AccessToken", "t=" + this.accessToken);
        jsonObject.addProperty("AppId", NetworkConstants.XBOX_ANDROID_CID);
        jsonObject.addProperty("deviceToken", this.deviceToken);
        // Add the proof key.
        jsonObject.add("ProofKey", this.createProofKey(this.xblPublicKey));

        // Perform request.
        var response = this.doRequest(NetworkConstants.XBOX_TOKEN_AUTH, jsonObject);
        if (response == null) {
            BreakingBedrock.getLogger().warn("Failed to obtain Xbox Live token.");
            return;
        }

        this.authToken = response;
    }

    /**
     * Obtains an Xbox Live device token.
     */
    @SneakyThrows
    public void obtainDeviceToken() {
        // Create token data.
        var jsonObject = new JsonObject();
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        // Create device properties.
        var properties = new JsonObject();
        jsonObject.add("Properties", properties);
        properties.addProperty("AuthMethod", "ProofOfPossession");
        properties.addProperty("DeviceType", "Android");
        properties.addProperty("Id", "{" + UUID.randomUUID() + "}");
        properties.addProperty("Version", "10");
        // Create signature.
        var proofKey = this.createProofKey(this.xblPublicKey);
        properties.add("ProofKey", proofKey);

        // Perform request.
        var response = this.doRequest(NetworkConstants.XBOX_DEVICE_AUTH, jsonObject);
        if (response == null) {
            BreakingBedrock.getLogger().warn("Failed to obtain Xbox Live device token.");
            return;
        }

        // Set the device token.
        this.deviceToken = response.get("Token").getAsString();
    }

    /*
     * Authenticate with Minecraft.
     */

    /**
     * Gets a JWT chain for Minecraft authentication.
     * @return A JWT token.
     */
    @SneakyThrows
    public String getChainData() {
        // Create the request data.
        var jsonObject = new JsonObject();
         jsonObject.addProperty("identityPublicKey",
                 EncodingUtils.base64Encode(this.clientPublicKey.getEncoded()));
        var jsonEncoded = EncodingUtils.jsonEncode(jsonObject);

        // Build the authorization header.
        var userHash = this.authToken.getAsJsonObject("AuthorizationToken")
                .getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)
                .getAsJsonObject().get("uhs").getAsString();
        var token = this.authToken.getAsJsonObject("AuthorizationToken")
                .get("Token").getAsString();

        // Create the request.
        var request = new Request.Builder()
                .url(NetworkConstants.MINECRAFT_AUTH)
                .addHeader("User-Agent", "MCPE/Android")
                .addHeader("Content-Type", "application/json")
                .addHeader("Client-Version", NetworkConstants.PACKET_CODEC.getMinecraftVersion())
                .addHeader("Authorization", "XBL3.0 x=" + userHash + ";" + token)
                .addHeader("x-xbl-contract-version", "1")
                .method("POST", RequestBody.create(jsonEncoded, MediaType.get("application/json")))
                .build();

        // Perform the request.
        try (var response = BreakingBedrock.getHttpClient().newCall(request).execute()) {
            var responseBody = response.body();
            if (responseBody == null) {
                BreakingBedrock.getLogger().warn("Failed to obtain Minecraft JWT chain.");
                return "";
            }

            return responseBody.string();
        }
    }
}

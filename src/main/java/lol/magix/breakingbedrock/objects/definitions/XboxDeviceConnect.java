package lol.magix.breakingbedrock.objects.definitions;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public final class XboxDeviceConnect {
    @SerializedName("user_code")
    private String code;

    @SerializedName("device_code")
    private String fetchToken;

    @SerializedName("verification_uri")
    private String redirectUri;

    private int interval;

    @SerializedName("expires_in")
    private int expiry;
}

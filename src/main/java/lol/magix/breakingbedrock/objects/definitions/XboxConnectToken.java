package lol.magix.breakingbedrock.objects.definitions;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public final class XboxConnectToken {
    @SerializedName("type_type")
    private String type;

    @SerializedName("expires_in")
    private int expiry;

    private String scope;

    @SerializedName("access_token")
    private String token;

    @SerializedName("refresh_token")
    private String refresh;

    @SerializedName("user_id")
    private String user;
}

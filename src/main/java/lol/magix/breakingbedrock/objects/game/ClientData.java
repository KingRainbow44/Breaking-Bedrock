package lol.magix.breakingbedrock.objects.game;

import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.ProfileUtils;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Minecraft Skin data.
 */
@Builder
public final class ClientData {
    @SerializedName("AnimatedImageData")
    @Builder.Default public List<Object> animatedImageData = new ArrayList<>();

    @SerializedName("ArmSize")
    @Builder.Default public String armSize = "";

    @SerializedName("CapeData")
    @Builder.Default public String capeData = "";

    @SerializedName("CapeId")
    @Builder.Default public String capeId = "";

    @SerializedName("CapeImageHeight")
    @Builder.Default public int capeImageHeight = 0;

    @SerializedName("CapeImageWidth")
    @Builder.Default public int capeImageWidth = 0;

    @SerializedName("CapeOnClassicSkin")
    @Builder.Default public boolean capeOnClassicSkin = false;

    @SerializedName("ClientRandomId")
    @Builder.Default public long clientRandomId = new Random().nextLong();

    @SerializedName("CompatibleWithClientSideChunkGen")
    @Builder.Default public boolean compatibleWithClientSideChunkGen = false;

    @SerializedName("CurrentInputMode")
    @Builder.Default public int currentInputMode = 1;

    @SerializedName("DefaultInputMode")
    @Builder.Default public int defaultInputMode = 1;

    @SerializedName("DeviceId")
    @Builder.Default public String deviceId = UUID.randomUUID().toString();

    @SerializedName("DeviceModel")
    @Builder.Default public String deviceModel = "";

    @SerializedName("DeviceOS")
    @Builder.Default public int deviceOS = 7;

    @SerializedName("GameVersion")
    @Builder.Default public String gameVersion = NetworkConstants.PACKET_CODEC.getMinecraftVersion();

    @SerializedName("GuiScale")
    @Builder.Default public int guiScale = 0;

    @SerializedName("IsEditorMode")
    @Builder.Default public boolean isEditorMode = false;

    @SerializedName("LanguageCode")
    @Builder.Default public String languageCode = "en_US";

    @SerializedName("PersonaPieces")
    @Builder.Default public List<Object> personaPieces = new ArrayList<>();

    @SerializedName("PersonaSkin")
    @Builder.Default public boolean personaSkin = false;

    @SerializedName("PieceTintColors")
    @Builder.Default public List<Object> pieceTintColors = new ArrayList<>();

    @SerializedName("PlatformOfflineId")
    @Builder.Default public String platformOfflineId = "";

    @SerializedName("PlatformOnlineId")
    @Builder.Default public String platformOnlineId = "";

    @SerializedName("PlayFabId")
    @Builder.Default public String playFabId = "";

    @SerializedName("PremiumSkin")
    @Builder.Default public boolean premiumSkin = false;

    @SerializedName("SelfSignedId")
    @Builder.Default public String selfSignedId = UUID.randomUUID().toString();

    @SerializedName("ServerAddress")
    @Builder.Default public String serverAddress = "";

    @SerializedName("SkinAnimationData")
    @Builder.Default public String skinAnimationData = "";

    @SerializedName("SkinColor")
    @Builder.Default public String skinColor = "#0";

    @SerializedName("SkinData")
    @Builder.Default public String skinData = ProfileUtils.SKIN_DATA_BASE_64;

    @SerializedName("SkinGeometryData")
    @Builder.Default public String skinGeometryData = EncodingUtils.base64Encode(ProfileUtils.SKIN_GEOMETRY_DATA.getBytes());

    @SerializedName("SkinGeometryDataEngineVersion")
    @Builder.Default public String skinGeometryDataEngineVersion = "MQ";

    @SerializedName("SkinId")
    @Builder.Default public String skinId = UUID.randomUUID() + ".Custom" + UUID.randomUUID();

    @SerializedName("SkinImageHeight")
    @Builder.Default public int skinImageHeight = 64;

    @SerializedName("SkinImageWidth")
    @Builder.Default public int skinImageWidth = 64;

    @SerializedName("SkinResourcePatch")
    @Builder.Default public String skinResourcePatch = "ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tIgogICB9Cn0K";

    @SerializedName("ThirdPartyName")
    @Builder.Default public String thirdPartyName = "";

    @SerializedName("ThirdPartyNameOnly")
    @Builder.Default public boolean thirdPartyNameOnly = false;

    @SerializedName("TrustedSkin")
    @Builder.Default public boolean trustedSkin = false;

    @SerializedName("UIProfile")
    @Builder.Default public int uiProfile = 0;
}

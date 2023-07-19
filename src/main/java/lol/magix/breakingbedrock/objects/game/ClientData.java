package lol.magix.breakingbedrock.objects.game;

import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.ProfileUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimatedTextureType;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationExpressionType;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Minecraft Skin data.
 */
@Builder
public final class ClientData {
    @SerializedName("AnimatedImageData")
    @Builder.Default public List<SkinAnimation> animatedImageData = new ArrayList<>();

    @SerializedName("ArmSize")
    @Builder.Default public ArmSizeType armSize = ArmSizeType.WIDE;

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
    @Builder.Default public String deviceModel = "Generic Android 10 Device";

    @SerializedName("DeviceOS")
    @Builder.Default public int deviceOS = 1; // 7 = Windows 10, 1 = Android

    @SerializedName("GameVersion")
    @Builder.Default public String gameVersion = NetworkConstants.PACKET_CODEC.getMinecraftVersion();

    @SerializedName("GuiScale")
    @Builder.Default public int guiScale = 0;

    @SerializedName("IsEditorMode")
    @Builder.Default public boolean isEditorMode = false;

    @SerializedName("LanguageCode")
    @Builder.Default public String languageCode = "en_US";

    @SerializedName("OverrideSkin")
    @Builder.Default public boolean overrideSkin = false;

    @SerializedName("PersonaPieces")
    @Builder.Default public List<PersonaPiece> personaPieces = new ArrayList<>();

    @SerializedName("PersonaSkin")
    @Builder.Default public boolean personaSkin = false;

    @SerializedName("PieceTintColors")
    @Builder.Default public List<PersonaPieceTintColor> pieceTintColors = new ArrayList<>();

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
    @Builder.Default public String skinGeometryDataEngineVersion = "MC4wLjA=";

    @SerializedName("SkinId")
    @Builder.Default public String skinId = UUID.randomUUID() + ".Custom";

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

    /**
     * Applies the data from the image to the client data.
     *
     * @param image The image to apply.
     */
    public void setSkin(BufferedImage image) {
        var imageData = ImageData.from(image);
        this.skinData = Base64.getEncoder().encodeToString(imageData.getImage());
        this.skinImageHeight = imageData.getHeight();
        this.skinImageWidth = imageData.getWidth();
        this.skinId = UUID.randomUUID() + ".Custom";
    }

    /**
     * Applies the data from the image to the client data.
     *
     * @param image The image to apply.
     */
    public void setCape(BufferedImage image) {
        var imageData = ImageData.from(image);
        this.capeData = Base64.getEncoder().encodeToString(imageData.getImage());
        this.capeImageHeight = imageData.getHeight();
        this.capeImageWidth = imageData.getWidth();
        this.capeId = UUID.randomUUID().toString();
        this.capeOnClassicSkin = true;
    }

    public record SkinAnimation(@SerializedName("Frames") float frames,
                                @SerializedName("Image") String image,
                                @SerializedName("ImageHeight") int height,
                                @SerializedName("ImageWidth") int width,
                                @SerializedName("Type") AnimatedTextureType animatedTexture,
                                @SerializedName("AnimationExpression") AnimationExpressionType animationExpression) {
    }

    public record PersonaPiece(@SerializedName("IsDefault") boolean isDefault,
                               @SerializedName("PackId") String packId,
                               @SerializedName("PieceId") String pieceId,
                               @SerializedName("PieceType") String pieceType,
                               @SerializedName("ProductId") String productId) {
    }

    public record PersonaPieceTintColor(@SerializedName("PieceType") boolean pieceType,
                                        @SerializedName("Colors") List<String> colors) {
    }

    @Getter
    @RequiredArgsConstructor
    public enum ArmSizeType {
        @SerializedName("wide") WIDE("geometry.humanoid.custom", "default", "https://raw.githubusercontent.com/Flonja/TunnelMC/master/resources/steve.png"),
        @SerializedName("slim") SLIM("geometry.humanoid.customSlim", "slim", "https://raw.githubusercontent.com/Flonja/TunnelMC/master/resources/alex.png");

        private final String geometryName;
        private final String model;
        private final String defaultSkinUrl;

        public String getEncodedGeometryData() {
            return EncodingUtils.base64Encode(("{\"geometry\":{\"default\":\"" + geometryName + "\"}}").getBytes(StandardCharsets.UTF_8));
        }

        public static ArmSizeType fromUUID(UUID uuid) {
            return (uuid.hashCode() & 1) == 1 ? SLIM : WIDE;
        }
    }
}

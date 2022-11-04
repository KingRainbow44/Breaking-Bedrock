package lol.magix.breakingbedrock.objects.absolute;

import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.v557.Bedrock_v557;

/**
 * Constants related to networking.
 */
public interface NetworkConstants {
    /* This will be the latest supported version. */
    BedrockPacketCodec PACKET_CODEC = Bedrock_v557.V557_CODEC;

    // Collection of URLs to authenticate to Minecraft: Bedrock through Xbox Live.
    String XBOX_USER_AUTH = "https://user.auth.xboxlive.com/user/authenticate";
    String XBOX_AUTHORIZE = "https://xsts.auth.xboxlive.com/xsts/authorize";
    String XBOX_DEVICE_AUTH = "https://device.auth.xboxlive.com/device/authenticate";
    String XBOX_TITLE_AUTH = "https://title.auth.xboxlive.com/title/authenticate";
    String MINECRAFT_AUTH = "https://multiplayer.minecraft.net/authentication";
}

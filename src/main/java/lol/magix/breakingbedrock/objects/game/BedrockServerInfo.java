package lol.magix.breakingbedrock.objects.game;

import lol.magix.breakingbedrock.utils.EncodingUtils;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class BedrockServerInfo extends ServerInfo {
    public BedrockServerInfo(String name, String address, boolean local) {
        super(name, address, local);
    }

    @Override
    public NbtCompound toNbt() {
        var nbt = super.toNbt();
        nbt.putBoolean("bedrock", true);

        return nbt;
    }

    /**
     * Parses a BedrockServerInfo from a NbtCompound.
     *
     * @param root The NbtCompound to parse from.
     * @return The parsed BedrockServerInfo.
     */
    public static BedrockServerInfo fromNbt(NbtCompound root) {
        var info = new BedrockServerInfo(
                root.getString("name"),
                root.getString("ip"),
                false);
        if (root.contains("icon", NbtElement.STRING_TYPE))
            info.setFavicon(EncodingUtils.base64DecodeToBytes(root.getString("icon")));
        if (root.contains("acceptTextures", NbtElement.BYTE_TYPE)) {
            if (root.getBoolean("acceptTextures")) {
                info.setResourcePackPolicy(ResourcePackPolicy.ENABLED);
            } else {
                info.setResourcePackPolicy(ResourcePackPolicy.DISABLED);
            }
        }

        return info;
    }
}

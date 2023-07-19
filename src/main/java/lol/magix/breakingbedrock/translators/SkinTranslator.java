package lol.magix.breakingbedrock.translators;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.Pair;
import lol.magix.breakingbedrock.objects.game.ClientData;
import lol.magix.breakingbedrock.objects.game.ImageDataPlayerSkinTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkinTranslator {
    private static final Map<UUID, Pair<SerializedSkin, Integer>> skins = new HashMap<>();

    /**
     * Creates an identifier from a profile texture type.
     *
     * @param type The profile texture type.
     * @param uuid The UUID of the player.
     * @param version The version of the skin.
     * @return The identifier.
     */
    private static Identifier getIdentifier(Type type, UUID uuid, int version) {
        var string = switch (type) {
            case SKIN -> "skins";
            case CAPE -> "capes";
            case ELYTRA -> "elytra";
        };
        return new Identifier(string + "/" + uuid.toString() + "/v" + version);
    }

    /**
     * @param uuid The UUID of the player.
     * @return The skin model type.
     */
    public static String getModel(UUID uuid) {
        // Check if the player is connected to the server.
        if (!BedrockNetworkClient.connected())
            return DefaultSkinHelper.getModel(uuid);

        // Get the serialized skin.
        var skin = skins.get(uuid);
        if (skin == null)
            return DefaultSkinHelper.getModel(uuid);

        // Get the arm size.
        var armSize = skin.a().getArmSize().toUpperCase();
        return armSize.isEmpty() ? DefaultSkinHelper.getModel(uuid) :
                ClientData.ArmSizeType.valueOf(armSize).getModel();
    }

    /**
     * Identifies the texture part of a player.
     *
     * @param type The type of the texture.
     * @param uuid The UUID of the player.
     * @return The identifier of the texture.
     */
    public static Identifier getTexturePart(Type type, UUID uuid) {
        // Check if the player is connected to the server.
        if (!BedrockNetworkClient.connected()) return null;

        // Get the serialized skin.
        var skin = skins.getOrDefault(uuid, null);
        if (skin == null) return null;

        var identifier = getIdentifier(type, uuid, skin.b());
        var texture = MinecraftClient.getInstance()
                .getTextureManager()
                .getOrDefault(identifier, null);
        if (texture == null) {
            var imageData = switch (type) {
                case SKIN -> skin.a().getSkinData();
                case CAPE -> skin.a().getCapeData();
                case ELYTRA -> ImageData.EMPTY;
            };
            if (imageData.equals(ImageData.EMPTY)) {
                return null;
            }

            texture = new ImageDataPlayerSkinTexture(imageData, DefaultSkinHelper.getTexture(), Type.SKIN == type, null);

            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
        }
        return identifier;
    }

    /**
     * Caches a serialized skin.
     *
     * @param uuid The UUID of the player.
     * @param skin The serialized skin.
     */
    public static void addSerializedSkin(UUID uuid, SerializedSkin skin) {
        // Check if the player is connected to the server.
        if (!BedrockNetworkClient.connected()) return;

        // Check the skin geometry type.
        if (!skin.getGeometryName().equals("geometry.humanoid.custom") &&
                !skin.getGeometryName().equals("geometry.humanoid.customSlim")) {
            BreakingBedrock.getLogger().warn("Discarding unknown geometry skin: {}", skin.getGeometryName());
            return;
        }

        skins.compute(uuid, (uuid1, pair) -> {
            var version = 0;
            if (pair != null) {
                version = pair.b() + 1;
            }
            return new Pair<>(skin, version);
        });
    }
}

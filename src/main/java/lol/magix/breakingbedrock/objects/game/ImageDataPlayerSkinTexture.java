package lol.magix.breakingbedrock.objects.game;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;

import java.io.IOException;

public final class ImageDataPlayerSkinTexture extends ResourceTexture {
    private final ImageData image;
    private final boolean convertLegacy;
    private final Runnable loadedCallback;

    private boolean loaded;

    public ImageDataPlayerSkinTexture(ImageData image, Identifier fallbackSkin, boolean convertLegacy, Runnable callback) {
        super(fallbackSkin);

        this.image = image;
        this.convertLegacy = convertLegacy;
        this.loadedCallback = callback;
    }

    private void onTextureLoaded(NativeImage image) {
        if (this.loadedCallback != null) {
            this.loadedCallback.run();
        }

        MinecraftClient.getInstance().executeSync(() -> {
            this.loaded = true;
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> {
                    this.uploadTexture(image);
                });
            } else {
                this.uploadTexture(image);
            }
        });
    }

    private void uploadTexture(NativeImage image) {
        TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        MinecraftClient.getInstance().executeSync(() -> {
            if (!this.loaded) {
                try {
                    super.load(manager);
                } catch (IOException var3) {
                    BreakingBedrock.getLogger().warn("Failed to load texture: {}", this.location, var3);
                }

                this.loaded = true;
            }
        });

        BreakingBedrock.getLogger().debug("Loading skin texture from imageData");
        var nativeImage = this.loadTexture(this.image);
        if (nativeImage == null) {
            nativeImage = this.loadTextureData(manager).getImage();
        }
        this.onTextureLoaded(nativeImage);
    }

    private NativeImage loadTexture(ImageData image) {
        NativeImage nativeImage = null;
        try {
            nativeImage = ConversionUtils.imageToNative(image);
            if (this.convertLegacy) {
                nativeImage = remapTexture(nativeImage);
            }
        } catch (Exception e) {
            BreakingBedrock.getLogger().error("Error while loading the skin texture", e);
        }

        return nativeImage;
    }

    private static NativeImage remapTexture(NativeImage image) {
        int i = image.getHeight();
        int j = image.getWidth();
        if ((i == 64 || j == 128) && (i == 32 || i == 64 || i == 128)) {
            if (i == 32) {
                NativeImage nativeImage = new NativeImage(64, 64, true);
                nativeImage.copyFrom(image);
                image.close();
                image = nativeImage;
                nativeImage.fillRect(0, 32, 64, 32, 0);
                nativeImage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeImage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeImage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeImage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeImage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            stripAlpha(image, 0, 0, 32, 16);
            if (i == 32) {
                stripColor(image, 32, 0, 64, 32);
            }

            stripAlpha(image, 0, 16, 64, 32);
            stripAlpha(image, 16, 48, 48, 64);
            return image;
        } else {
            image.close();
            BreakingBedrock.getLogger().warn("Discarding incorrectly sized ({}x{}) skin texture", j, i);
            return null;
        }
    }

    private static void stripColor(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int x = x1; x < x2; ++x) {
            for(int y = y1; y < y2; ++y) {
                int k = image.getColor(x, y);
                if ((k >> 24 & 255) < 128) {
                    return;
                }
            }
        }

        for(int x = x1; x < x2; ++x) {
            for(int y = y1; y < y2; ++y) {
                image.setColor(x, y, image.getColor(x, y) & 0xFF_FF_FF);
            }
        }
    }

    private static void stripAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int x = x1; x < x2; ++x) {
            for(int y = y1; y < y2; ++y) {
                image.setColor(x, y, image.getColor(x, y) | 0xFF_00_00_00);
            }
        }
    }
}

package lol.magix.breakingbedrock.translators.pack;

import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import io.netty.buffer.ByteBuf;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.definitions.resourcepack.JavaFontDefinition;
import lol.magix.breakingbedrock.objects.definitions.resourcepack.Metadata;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.ImageUtils;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket.Status;

import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public final class ResourcePackTranslator {
    public static final File BASE
            = new File(BreakingBedrock.getDataDirectory(), "resource_packs");
    public static final File CONVERT_CACHE
            = new File(BASE, "convert_cache");
    public static final File SERVER_CACHE
            = new File(BASE, "server_cache");
    public static final File CLIENT_CACHE
            = new File(BASE, "client_cache");

    /** Bindings from Pack ID -> Resource Pack Info. */
    @Getter private static final Map<String, ResourcePackInfo> cache = new HashMap<>();
    private static final Map<UUID, ResourcePackDownloadHandle> downloads = new HashMap<>();

    @Setter private static ResourcePackDownloadHandle currentDownload;
    @Getter private static final Queue<ResourcePackDownloadHandle> downloadQueue
            = new LinkedBlockingDeque<>();

    /**
     * Initializes the resource pack translator.
     */
    public static void initialize() {
        var logger = BreakingBedrock.getLogger();

        // Create directories.
        if (!BASE.exists() && !BASE.mkdirs()) {
            logger.warn("Failed to create resource pack directory.");
        }
        if (!SERVER_CACHE.exists() && !SERVER_CACHE.mkdirs()) {
            logger.warn("Failed to create server cache directory.");
        }
        if (!CLIENT_CACHE.exists() && !CLIENT_CACHE.mkdirs()) {
            logger.warn("Failed to create client cache directory.");
        }
        if (!CONVERT_CACHE.exists() && !CONVERT_CACHE.mkdirs()) {
            logger.warn("Failed to create convert cache directory.");
        }
    }

    /**
     * Checks if a resource pack is downloaded.
     *
     * @param packId The pack ID.
     * @return Whether the resource pack is downloaded.
     */
    public static boolean packDownloaded(UUID packId) {
        // Check if the pack has been converted.
        var converted = new File(CLIENT_CACHE, packId + ".zip");
        if (converted.exists()) {
            return true;
        }

        // Check if the pack has been downloaded.
        var server = new File(SERVER_CACHE, packId + ".zip");
        return server.exists();
    }

    /**
     * Prepares a resource pack for downloading.
     *
     * @param info The resource pack info.
     */
    public static void addPack(ResourcePackDownloadHandle info) {
        downloads.put(info.getPackInfo().getPackId(), info);

        // Try to request the first chunk.
        if (currentDownload == null) {
            currentDownload = info;
            info.requestChunk();
        } else {
            downloadQueue.add(info);
        }
    }

    /**
     * Appends data to a resource pack download.
     *
     * @param packId The pack ID.
     * @param data The data to append.
     */
    public static void downloadPack(UUID packId, ByteBuf data) {
        var info = downloads.get(packId);
        if (info == null) {
            BreakingBedrock.getLogger().warn("Received unknown resource pack download.");
            return;
        }

        // Append data.
        info.getPackData().writeBytes(data);

        // Check if all data has been received.
        if (info.getChunks() == info.getCurrentChunk()) {
            info.completeDownload(); // Invoke completion handler.

            // Check if there are any queued downloads.
            if (downloadQueue.isEmpty()) {
                currentDownload = null;

                var client = info.getClient();
                // Send server resource pack packet.
                var responsePacket = new ResourcePackClientResponsePacket();
                responsePacket.setStatus(Status.COMPLETED);
                client.sendPacket(responsePacket, true);

                // Call the client's callback.
                client.onPacksDownloaded();
            } else {
                currentDownload = downloadQueue.poll();
                currentDownload.requestChunk();
            }
        } else {
            info.requestChunk(); // Request next chunk.
        }
    }

    /**
     * Attempts to decrypt, convert, then re-package a resource pack.
     *
     * @param resourcePack The resource pack to translate.
     */
    public static void translate(ResourcePackInfo resourcePack)
            throws IOException {
        // Get the source resource pack.
        // This is fetched from the server.
        var file = new File(SERVER_CACHE, resourcePack.getPackId() + ".zip");
        if (!file.exists()) {
            BreakingBedrock.getLogger().warn("Failed to find resource pack in server cache.");
            return;
        }

        // Un-archive the resource pack.
        var packDirectory = new File(CONVERT_CACHE, resourcePack.getPackId().toString());
        if (!packDirectory.exists() && !packDirectory.mkdirs()) {
            BreakingBedrock.getLogger().warn("Failed to create resource pack directory.");
            return;
        }
        EncodingUtils.unzip(file, packDirectory);

        // Check if another folder is present.
        // This is the case for some resource packs.
        var folders = packDirectory.listFiles(File::isDirectory);
        if (folders != null && folders.length == 1) {
            packDirectory = folders[0];
        }

        // Check if the resource pack needs to be decrypted.
        var contents = new File(packDirectory, "contents.json");
        if (contents.exists()) {
            // Decrypt the resource pack.
            var decrypted = new File(packDirectory, "decrypted");
            if (!CDecrypt.decrypt(resourcePack.getContentKey(), packDirectory, decrypted))
                throw new IOException("Failed to decrypt resource pack.");
            // Set the pack directory to the decrypted directory.
            packDirectory = decrypted;
        }

        // Get the pack output directory.
        var outputDirectory = new File(packDirectory, "convert_output");
        if (!outputDirectory.mkdirs()) throw new IOException("Failed to create output directory.");

        // Convert the resource pack from Bedrock -> Java.
        // String resourcePackName;

        {
            // Read the Bedrock metadata.
            // var bedrockMetadata = EncodingUtils.jsonDecode(
            //         new File(packDirectory, "manifest.json"),
            //         JsonObject.class);
            // var header = bedrockMetadata.get("header").getAsJsonObject();

            // Create the Java metadata.
            // resourcePackName = header.get("name").getAsString();
            var description = new JsonObject();
            description.addProperty("text", "Converted from Minecraft: Bedrock.");
            description.addProperty("color", "#6fc2f2");
            var metadata = Metadata.builder()
                    .pack(Metadata.Pack.builder()
                            .packFormat(GameConstants.RP_FORMAT)
                            .description(List.of(description))
                            .build())
                    .build();

            Files.writeString(file(outputDirectory, "pack.mcmeta"),
                    EncodingUtils.jsonEncode(metadata));
        }

        {
            // Copy the pack icon.
            var icon = new File(packDirectory, "pack_icon.png");
            if (icon.exists()) {
                Files.copy(icon.toPath(), file(outputDirectory, "pack.png"));
            }
        }

        {
            // Initialize the assets directory.
            var assets = new File(outputDirectory, "assets/minecraft");
            if (!assets.mkdirs()) throw new IOException("Failed to create assets directory.");

            var font = new File(assets, "font");
            if (!font.mkdirs()) throw new IOException("Failed to create font directory.");

            var textures = new File(assets, "textures");
            if (!textures.mkdirs()) throw new IOException("Failed to create textures directory.");
        }

        {
            /*
             * Information about Bedrock -> Java fonts:
             * - Bedrock fonts are stored as PNG images.
             * - These PNGs are called 'glyphs' which contain a Unicode prefix in the file name.
             * - Each image is 256 x 256 pixels.
             * - Each 'glyph' is 16 x 16 pixels.
             * - Java font definitions are contained in 'assets/minecraft/font/default.json'.
             * - We are converting glyphs to 'assets/minecraft/textures/custom-font/'.
             * - The height and ascent add together to get the pixel dimensions of an image in the bitmap.
             */

            // Translate glyphs.
            var fontTarget = new File(outputDirectory, "assets/minecraft/font");
            var fontFileTarget = new File(outputDirectory, "assets/minecraft/textures/custom-font");
            var fontSource = new File(packDirectory, "font");
            if (!fontSource.exists()) return;
            if (!fontFileTarget.mkdirs()) throw new IOException("Failed to create font directory.");

            var files = fontSource.listFiles();
            if (files == null) return;

            // Create an object for Java font definitions.
            var definitions = new JsonObject();
            var providers = new ArrayList<JavaFontDefinition>();

            for (var fontFile : files) {
                var fileName = fontFile.getName();
                if (!fileName.startsWith("glyph_") ||
                        !fileName.endsWith(".png")) continue;
                // Get the Unicode prefix.
                var prefix = fileName.substring(6, 8);

                // Create a folder for the font.
                var fontFolder = new File(fontFileTarget, "glyph_" + prefix.toLowerCase());
                if (!fontFolder.mkdirs()) throw new IOException("Failed to create font directory.");

                // Get the font size.
                var size = ImageUtils.getSize(fontFile) / 16;

                // Perform image subdivision.
                var images = ImageUtils.divide(fontFile, size);
                for (var i = 0; i < images.size(); i++) {
                    // Determine the image name.
                    var image = images.get(i);
                    var imageName = prefix.toLowerCase() + "_" + i + ".png";

                    try {
                        // Crop the image.
                        var cropped = ImageUtils.crop(image);
                        // Get the individual size of the image.
                        var height = cropped.getHeight();

                        // Write the image to the target directory.
                        var imageFile = new File(fontFolder, imageName);
                        ImageUtils.saveImage(imageFile, cropped);

                        // Create a new font definition.
                        providers.add(JavaFontDefinition.b()
                                .name(fontFolder.getName() + "/" + imageName)
                                .offset(prefix, i).height(height).ascent(height));
                    } catch (RasterFormatException ignored) { }
                }
            }

            // Write the Java font definitions.
            definitions.add("providers", EncodingUtils.toJson(providers));
            Files.writeString(file(fontTarget, "default.json"),
                    EncodingUtils.jsonEncode(definitions));
        }

        // Copy the resource pack contents.
        var outputPath = new File(CLIENT_CACHE, resourcePack.getPackId().toString());
        Files.move(outputDirectory.toPath(), outputPath.toPath());
    }

    /**
     * Returns the path to a file.
     *
     * @param base The base directory.
     * @param path The path to the file.
     * @return The path to the file.
     */
    private static Path file(File base, String path) {
        return Paths.get(base.getAbsolutePath(), path);
    }

    interface CDecrypt extends Library {
        CDecrypt INSTANCE = Native.load("rp_decrypt", CDecrypt.class);

        /**
         * Attempts to decrypt a resource pack.
         *
         * @param key The resource pack key.
         * @param resourcePack The extracted, encrypted resource pack.
         * @param output The output file.
         * @return Whether the decryption was successful.
         */
        static boolean decrypt(String key, File resourcePack, File output) {
            var packDirectory = resourcePack.getAbsolutePath();
            var outputDirectory = output.getAbsolutePath();

            return INSTANCE.decrypt(key.getBytes(StandardCharsets.US_ASCII), key.length(),
                    packDirectory.getBytes(StandardCharsets.US_ASCII), packDirectory.length(),
                    outputDirectory.getBytes(StandardCharsets.US_ASCII), outputDirectory.length());
        }

        /**
         * Attempts to decrypt a resource pack.
         *
         * @param key The resource pack key.
         * @param packDirectory The directory to the extracted, encrypted resource pack.
         * @param outputDirectory The directory to output the decrypted resource pack.
         * @return Whether the decryption was successful.
         */
        boolean decrypt(byte[] key, int keyLen,
                        byte[] packDirectory, int packDirLen,
                        byte[] outputDirectory, int outputDirLen);
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.BlockPaletteTranslator;
import lol.magix.breakingbedrock.translators.BlockStateTranslator;
import lol.magix.breakingbedrock.translators.DecodedPaletteStorage;
import lol.magix.breakingbedrock.translators.LegacyBlockPaletteTranslator;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.*;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;

import java.util.Objects;

@Translate(PacketType.BEDROCK)
// Taken from THEREALWWEFAN231/tunnelmc.
public final class LevelChunkTranslator extends Translator<LevelChunkPacket> {
    @Override
    public Class<LevelChunkPacket> getPacketClass() {
        return LevelChunkPacket.class;
    }

    @Override
    public void translate(LevelChunkPacket packet) {
        // Extract chunk data.
        var chunkX = packet.getChunkX();
        var chunkZ = packet.getChunkZ();

        // Fetch the world.
        var world = MinecraftClient.getInstance().world;
        Objects.requireNonNull(world, "World is null");

        // Perform chunk rendering.
        var biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        var sections = new ChunkSection[24];
        var buffer = Unpooled.buffer();
        buffer.writeBytes(packet.getData());

        // Decode sub-chunks.
        for (var i = 0; i < packet.getSubChunksLength(); i++) {
            var section = new ChunkSection(biomeRegistry);
            var version = buffer.readByte();
            switch (version) {
                default -> LevelChunkTranslator.decodeZero(buffer, section);
                case 1 -> LevelChunkTranslator.decodeOne(buffer, section);
                case 8 -> LevelChunkTranslator.decodeEight(buffer, section, buffer.readByte());
                case 9 -> LevelChunkTranslator.decodeNine(buffer, section, buffer.readByte());
            }

            sections[i] = section;
        }

        // Determine empty sub-chunks.
        var emptySections = 0;
        if (
                sections[0] != null &&
                sections[1] != null &&
                sections[2] != null &&
                sections[3] != null &&
                sections[0].isEmpty() &&
                sections[1].isEmpty() &&
                sections[2].isEmpty() &&
                sections[3].isEmpty()
        ) emptySections = 4;

        // Decode the biomes.
        try {
            ReadableContainer<RegistryEntry<Biome>> last = null;
            for (var i = 0; i < packet.getSubChunksLength(); i++) {
                var biomes = LevelChunkTranslator.decodeBiomes(buffer, biomeRegistry);
                if (biomes == null) {
                    if (i == 0) throw new IllegalStateException("Cannot fallback to last palette at 0.");
                    biomes = last;
                } else last = biomes;

                var section = sections[i];
                if (section == null) continue;

                // Set the biomes.
                var palette = PacketByteBufs.create();
                biomes.writePacket(palette);
                var container = section.getBiomeContainer();
                container.writePacket(palette);
            }
        } catch (Exception exception) {
            this.logger.debug("Failed to decode block palette", exception);
        }

        // Process the packet.
        final var emptySectionsF = emptySections;
        MinecraftClient.getInstance().executeSync(() -> {
            var processedSections = new ChunkSection[24];
            System.arraycopy(sections, emptySectionsF,
                    processedSections, 4, sections.length - 4);

            var chunk = new WorldChunk(world, new ChunkPos(chunkX, chunkZ),
                    UpgradeData.NO_UPGRADE_DATA, new ChunkTickScheduler<>(), new ChunkTickScheduler<>(),
                    0, processedSections, null, null);
            this.javaClient().processPacket(new ChunkDataS2CPacket(
                    chunk, world.getLightingProvider(), null, null));
        });
    }

    /**
     * @param buffer The buffer to decode from.
     * @param section The section to decode into.
     */
    private static void decodeZero(ByteBuf buffer, ChunkSection section) {
        var blockIds = new byte[4096];
        var metaIds = new byte[2048];
        buffer.readBytes(blockIds);
        buffer.readBytes(metaIds);

        for (var x = 0; x < 16; x++) {
            for (var y = 0; y < 16; y++) {
                for (var z = 0; z < 16; z++) {
                    var index = (x << 8) + (z << 4) + y;

                    var id = blockIds[index];
                    var meta = metaIds[index >> 1] >> (index & 1) * 4 & 15;

                    var blockState = LegacyBlockPaletteTranslator
                            .getLegacyToId().get(id << 6 | meta);
                    if (blockState != null)
                        section.setBlockState(x, y, z, blockState);
                }
            }
        }
    }

    /**
     * @param buffer The buffer to decode from.
     * @param section The section to decode into.
     */
    private static void decodeOne(ByteBuf buffer, ChunkSection section) {
        LevelChunkTranslator.decodeEight(buffer, section, (byte) 1);
    }

    /**
     * @param buffer The buffer to decode from.
     * @param section The section to decode into.
     * @param size The size of the palette.
     */
    private static void decodeEight(ByteBuf buffer, ChunkSection section, byte size) {
        for (var storageReadIndex = 0; storageReadIndex < size; storageReadIndex++) {
            var storage = DecodedPaletteStorage.fromPacket(buffer, DecodedPaletteStorage.BLOCK_PALETTE);
            if (storage == null) continue;

            if (storageReadIndex == 0) {
                for (var x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (var y = 0; y < 16; y++) {
                            var id = storage.get(x, y, z);
                            if (id != null && id != BlockPaletteTranslator.AIR_BLOCK_ID) {
                                var blockState = BlockStateTranslator.getRuntime2Java().get(id);
                                if (blockState == null) {
                                    System.out.printf("Missing block state for %d%n", id);
                                    blockState = GameConstants.FALLBACK_BLOCK.getDefaultState();
                                }

                                section.setBlockState(x, y, z, blockState);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param buffer The buffer to decode from.
     * @param section The section to decode into.
     * @param size The size of the palette.
     */
    private static void decodeNine(ByteBuf buffer, ChunkSection section, byte size) {
        buffer.readByte(); // Read height byte, we don't need it.
        LevelChunkTranslator.decodeEight(buffer, section, size);
    }

    /**
     * @param buffer The buffer to decode from.
     * @return The decoded biomes.
     */
    private static ReadableContainer<RegistryEntry<Biome>> decodeBiomes(ByteBuf buffer, Registry<Biome> registry) {
        var javaBiomes = new PalettedContainer<>(registry.getIndexedEntries(),
                registry.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BLOCK_STATE);

        var storage = DecodedPaletteStorage.fromPacket(buffer, DecodedPaletteStorage.BIOME_PALETTE);
        if (storage == null) {
            // storage == null means this storage had the flag pointing to the previous one. It basically means we should
            // inherit whatever palette we decoded last.
            return null;
        }

        for (var x = 0; x < 16; x++) {
            for (var z = 0; z < 16; z++) {
                for (var y = 0; y < 16; y++) {
                    var biomeId = storage.get(x, y, z);
                    javaBiomes.set(x, y, z, registry.getEntry(biomeId)
                            .orElse(registry.entryOf(BiomeKeys.PLAINS)));
                }
            }
        }

        return javaBiomes;
    }
}

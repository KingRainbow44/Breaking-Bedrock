package lol.magix.breakingbedrock.network.packets.world;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.util.stream.NetworkDataInputStream;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.objects.binary.BitArrayVersion;
import lol.magix.breakingbedrock.translators.BlockPaletteTranslator;
import lol.magix.breakingbedrock.translators.LegacyBlockPaletteTranslator;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.io.IOException;

@Translate(PacketType.BEDROCK)
/// Taken from THEREALWWEFAN231/tunnelmc.
public final class LevelChunkTranslator extends Translator<LevelChunkPacket> {
    private static final IndexedIterable<Biome> BIOME_REGISTRY =
            DynamicRegistryManager.createAndLoad().get(Registry.BIOME_KEY);

    @Override
    public Class<LevelChunkPacket> getPacketClass() {
        return LevelChunkPacket.class;
    }

    @Override
    public void translate(LevelChunkPacket packet) {
        // Extract chunk data.
        var chunkX = packet.getChunkX();
        var chunkZ = packet.getChunkZ();

        // Do render check.
        if (!this.shouldRender(chunkX, chunkZ)) {
            return;
        }
        
        // Perform chunk rendering.
        var biomeRegistry = DynamicRegistryManager
                .createAndLoad().get(Registry.BIOME_KEY);
        var sections = new ChunkSection[24];
        var buffer = Unpooled.buffer();
        buffer.writeBytes(packet.getData());

        for (var i = 0; i < packet.getSubChunksLength(); i++) {
            sections[i] = new ChunkSection(i, biomeRegistry);
            var chunkVersion = buffer.readByte();
            if (chunkVersion != 1 && chunkVersion != 8) {
                System.out.println("handling legacy chunk");
                manage0VersionChunk(buffer, sections[i]);
                continue;
            }

            var storageSize = chunkVersion == 1 ? 1 : buffer.readByte();

            for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
                var paletteHeader = buffer.readByte();
                var isRuntime = (paletteHeader & 1) == 1;
                var paletteVersion = (paletteHeader | 1) >> 1;

                var bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

                var maxBlocksInSection = 4096; // 16*16*16
                var bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
                var wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

                for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
                    var word = buffer.readIntLE();
                    bitArray.getWords()[wordIterationIndex] = word;
                }

                var paletteSize = VarInts.readInt(buffer);
                var sectionPalette = new int[paletteSize];
                var nbtStream = isRuntime ? null : new NBTInputStream(
                        new NetworkDataInputStream(new ByteBufInputStream(buffer)));

                for (int k = 0; k < paletteSize; k++) {
                    if (isRuntime) {
                        sectionPalette[k] = VarInts.readInt(buffer);
                    } else {
                        try {
                            var map = ((NbtMap) nbtStream.readTag()).toBuilder();
                            map.replace("name", "minecraft:" + map.get("name").toString());
                            sectionPalette[k] = BlockPaletteTranslator.getBlockId(BlockPaletteTranslator.getBlockState(map.build()));
                        } catch (IOException ignored) {
                            BreakingBedrock.getLogger().warn("Failed to read block state from palette.");
                        }
                    }
                }

                if (storageReadIndex == 0) {
                    var index = 0;

                    for (var x = 0; x < 16; x++) {
                        for (var z = 0; z < 16; z++) {
                            for (var y = 0; y < 16; y++) {
                                // Check if the section exists.
                                var section = sections[i];
                                if (section == null) continue;

                                var paletteIndex = bitArray.get(index);
                                var blockId = sectionPalette[paletteIndex];
                                if (blockId != BlockPaletteTranslator.AIR_BLOCK_ID) {
                                    // Get the block state from the ID.
                                    var blockState = BlockPaletteTranslator
                                            .getRuntimeToState().get(blockId);
                                    if (blockState == null) continue;

                                    // Update the block state.
                                    section.setBlockState(x, y, z, blockState);
                                }

                                index++;
                            }
                        }
                    }
                }
            }
        }

        // TODO: Fix biomes in chunks.
        // var javaBiomes = new int[1024];
        // var bedrockBiomes = new byte[256];
        // buffer.readBytes(bedrockBiomes);
        //
        // var biomeCount = 0;
        // for (var bedrockBiome : bedrockBiomes) {
        //     var desired = bedrockBiome;
        //     if (BIOME_REGISTRY.get(desired) == null) {
        //         // This is an invalid biome.
        //         desired = 1;
        //     }
        //
        //     // Conversion from 256 -> 1024.
        //     javaBiomes[biomeCount++] = desired;
        //     javaBiomes[biomeCount++] = desired;
        //     javaBiomes[biomeCount++] = desired;
        //     javaBiomes[biomeCount++] = desired;
        // }

        // Create a world chunk.
        var world = MinecraftClient.getInstance().world;
        var worldChunk = new WorldChunk(world, new ChunkPos(chunkX, chunkZ));

        // Apply chunk data.
        for (var i = 0; i < worldChunk.getSectionArray().length; i++) {
            if (sections[i] == null) sections[i] = new ChunkSection(i, biomeRegistry);
            worldChunk.getSectionArray()[i] = sections[i];
        }

//        for (var section : worldChunk.getSectionArray())
//            System.out.println(section.getPacketSize());

        // Send chunk packet.
        var lightProvider = world.getChunkManager().getLightingProvider();
        var chunkPacket = new ChunkDataS2CPacket(worldChunk, lightProvider,
                null, null, true);
        this.javaClient().processPacket(chunkPacket);
    }

    /**
     * Checks if the chunk should be rendered.
     * @param chunkX The chunk's X coordinate.
     * @param chunkZ The chunk's Z coordinate.
     * @return True if the chunk should be rendered, false otherwise.
     */
    private boolean shouldRender(int chunkX, int chunkZ) {
        var client = MinecraftClient.getInstance();
        var options = client.options;
        var player = client.player;
        var world = client.world;

        // Perform null checks.
        if (player == null) return false;
        if (world == null) return false;

        // Calculate the player's chunk coordinates.
        var playerChunkX = MathHelper.floor(player.getX()) >> 4;
        var playerChunkZ = MathHelper.floor(player.getZ()) >> 4;
        // Get the player's render distance.
        var renderDistance = options.getViewDistance().getValue();
        
        return Math.abs(chunkX - playerChunkX) <= renderDistance && 
                Math.abs(chunkZ - playerChunkZ) <= renderDistance;
    }

    /**
     * Handles a version 0 chunk.
     * @param buffer The chunk data buffer.
     * @param section The chunk section.
     */
    private void manage0VersionChunk(ByteBuf buffer, ChunkSection section) {
        var blockIds = new byte[4096];
        buffer.readBytes(blockIds);
        var metaIds = new byte[2048];
        buffer.readBytes(metaIds);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int idx = (x << 8) + (z << 4) + y;
                    int id = blockIds[idx];
                    int meta = metaIds[idx >> 1] >> (idx & 1) * 4 & 15;

                    // We use "LegacyBlockPalette" as this is what PocketMine-MP uses.
                    var blockState = LegacyBlockPaletteTranslator.getLegacyToId().get(id << 6 | meta);
                    if (blockState != null) {
                        section.setBlockState(x, y, z, blockState);
                    }
                }
            }
        }
    }
}

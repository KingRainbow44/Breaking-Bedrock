package lol.magix.breakingbedrock.translators;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.binary.BitArray;
import lol.magix.breakingbedrock.objects.binary.BitArrayVersion;
import lol.magix.breakingbedrock.objects.binary.EmptyBitArray;
import lol.magix.breakingbedrock.objects.game.GeneralBlockState;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.util.stream.NetworkDataInputStream;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
// Copied from Flonja/TunnelMC
public final class DecodedPaletteStorage {
    private final BitArray bitArray;
    private final Map<Integer, Integer> palette;

    public Integer get(int x, int y, int z) {
        return palette.getOrDefault(bitArray.get(index(x, y, z)), 0);
    }

    public void set(int x, int y, int z, int value) {
        if (!palette.containsValue(value)) {
            throw new IllegalArgumentException("Value does not exist in palette");
        }

        var id = 0;
        for (var entry : palette.entrySet()) {
            if (entry.getValue().equals(value)) {
                id = entry.getKey();
            }
        }

        bitArray.set(index(x, y, z), id);
    }

    private static int index(int x, int y, int z) {
        return (x << 8) + (z << 4) + y;
    }

    public static DecodedPaletteStorage fromPacket(ByteBuf byteBuf, Function<ByteBuf, Integer> encoding) {
        var paletteHeader = byteBuf.readUnsignedByte();
        var isRuntime = (paletteHeader & 1) == 1;
        var paletteVersion = (paletteHeader | 1) >> 1;

        if (paletteHeader >> 1 == 0x7f) {
            return null;
        }

        var bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

        var maxBlocksInSection = 4096;
        var bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
        var wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);
        if (!byteBuf.isReadable(wordsSize * 4)) {
            return new DecodedPaletteStorage(BitArrayVersion.V0.createPalette(maxBlocksInSection), new HashMap<>());
        }

        for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
            var word = byteBuf.readIntLE();
            bitArray.getWords()[wordIterationIndex] = word;
        }

        var paletteSize = 1;
        if (!(bitArray instanceof EmptyBitArray)) {
            paletteSize = VarInts.readInt(byteBuf);
        }

        var palette = new HashMap<Integer, Integer>();
        for (int i = 0; i < paletteSize; i++) {
            if (isRuntime) {
                palette.put(i, VarInts.readInt(byteBuf));
            } else {
                palette.put(i, encoding.apply(byteBuf));
            }
        }

        return new DecodedPaletteStorage(bitArray, palette);
    }

    public static final Function<ByteBuf, Integer> BIOME_PALETTE = ByteBuf::readIntLE;
    public static final Function<ByteBuf, Integer> BLOCK_PALETTE = byteBuf -> {
        try (var nbtStream = new NBTInputStream(
                new NetworkDataInputStream(
                        new ByteBufInputStream(byteBuf)))) {
            var nbt = (NbtMap) nbtStream.readTag();
            var map = nbt.toBuilder();

            map.replace("name", "minecraft:" + map.get("name").toString());
            return BlockPaletteTranslator.getBlockId(new GeneralBlockState(map.build()));
        } catch (IOException e) {
            BreakingBedrock.getLogger().warn("Failed to read block palette.", e);
        }

        return null;
    };
}

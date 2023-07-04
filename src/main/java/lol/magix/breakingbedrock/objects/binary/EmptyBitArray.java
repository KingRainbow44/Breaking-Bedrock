package lol.magix.breakingbedrock.objects.binary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmptyBitArray implements BitArray {
    private static final BitArray INSTANCE = new EmptyBitArray();

    @Override
    public void set(int index, int value) {
        // NOOP
    }

    @Override
    public int get(int index) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int[] getWords() {
        return new int[0];
    }

    @Override
    public BitArrayVersion getVersion() {
        return BitArrayVersion.V0;
    }

    @Override
    public BitArray copy() {
        return INSTANCE;
    }
}

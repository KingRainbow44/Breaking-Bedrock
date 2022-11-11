package lol.magix.breakingbedrock.objects.binary;

public interface BitArray {
    void set(int index, int value);

    int get(int index);

    int size();

    int[] getWords();

    BitArrayVersion getVersion();

    BitArray copy();
}
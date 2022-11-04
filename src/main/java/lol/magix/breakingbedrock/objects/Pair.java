package lol.magix.breakingbedrock.objects;

/**
 * A pair of objects.
 * @param a The first object.
 * @param b The second object.
 */
public record Pair<A, B>(
        A a,
        B b
) {
}
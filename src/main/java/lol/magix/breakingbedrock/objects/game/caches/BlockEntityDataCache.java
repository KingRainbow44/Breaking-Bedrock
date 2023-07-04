package lol.magix.breakingbedrock.objects.game.caches;

import lombok.Getter;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockEntityDataCache {
    @Getter private final Map<Vector3i, NbtMap> data
            = new ConcurrentHashMap<>();

    /**
     * @param position The position of the block entity.
     * @return The data of the block entity at the given position.
     */
    public NbtMap getDataFromPosition(Vector3i position) {
        return this.data.get(position);
    }
}

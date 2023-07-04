package lol.magix.breakingbedrock.utils;

import net.minecraft.util.math.BlockPos;
import org.cloudburstmc.math.vector.Vector3i;

public interface WorldUtils {
    /**
     * @param vector The vector to convert.
     * @return The converted vector.
     */
    static BlockPos toBlockPos(Vector3i vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * @param pos The position to convert.
     * @return The converted position.
     */
    static long asLong(Vector3i pos) {
        return BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
    }
}

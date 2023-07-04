package lol.magix.breakingbedrock.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.cloudburstmc.math.vector.Vector3f;
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
     * @param position The position to convert.
     * @return The converted position.
     */
    static Vector3i toBlockPos(BlockPos position) {
        return Vector3i.from(position.getX(), position.getY(), position.getZ());
    }

    /**
     * @param pos The position to convert.
     * @return The converted position.
     */
    static long asLong(Vector3i pos) {
        return BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @param source The source vector.
     * @return The converted vector.
     */
    static Vector3f convert(Vec3d source) {
        return Vector3f.from(source.getX(), source.getY(), source.getZ());
    }
}

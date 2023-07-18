package lol.magix.breakingbedrock.utils;

import lol.magix.breakingbedrock.objects.Triplet;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.lang.reflect.Method;

public interface GameUtils {
    Method SET_FLAG = GameUtils.setFlagMethod();

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

    /**
     * @param source The source vector.
     * @return The converted vector.
     */
    static Vec3d convert(Vector3f source) {
        return new Vec3d(source.getX(), source.getY(), source.getZ());
    }

    /**
     * @param source The source vector.
     * @param compare The vector to compare to.
     * @return Whether the vectors are equal.
     */
    static boolean equals(Vector3i source, Vector3i compare) {
        return source.getX() == compare.getX()
                && source.getY() == compare.getY()
                && source.getZ() == compare.getZ();
    }

    /**
     * Get the block the player is looking at.
     *
     * @param player The player to get the block from.
     * @param world The world to get the block from.
     * @return The block the player is looking at.
     */
    static Triplet<BlockPos, BlockState, Vec3d> getLookingBlock(ClientPlayerEntity player, World world) {
        // Get the block the player is looking at.
        var lookingAtPos = player.raycast(
                5, 0, false);
        if (lookingAtPos.getType() == HitResult.Type.BLOCK) {
            return null; // This means the player isn't looking at air, the thing this packet handles.
        }

        var sourcePos = lookingAtPos.getPos();
        var blockPos = BlockPos.ofFloored(sourcePos);
        var block = world.getBlockState(blockPos);

        return new Triplet<>(blockPos, block, sourcePos);
    }

    /**
     * @param item The item to check.
     * @return Whether the item is air.
     */
    static boolean isAir(ItemData item) {
        return item.getDefinition().getIdentifier()
                .equals(ItemData.AIR.getDefinition().getIdentifier());
    }

    /**
     * @return The setFlag method.
     */
    static Method setFlagMethod() {
        try {
            return Entity.class.getDeclaredMethod(
                    "setFlag", int.class, boolean.class);
        } catch (Exception ignored) { }

        throw new RuntimeException("Could not find setFlag method!");
    }

    /**
     * Sets the flag of an entity.
     *
     * @param entity The entity to set the flag of.
     * @param index The index of the flag.
     * @param value The value to set the flag to.
     */
    static void setFlag(Entity entity, int index, boolean value) {
        try {
            SET_FLAG.invoke(entity, index, value);
        } catch (Exception ignored) { }
    }
}

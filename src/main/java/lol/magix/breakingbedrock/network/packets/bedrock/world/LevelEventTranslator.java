package lol.magix.breakingbedrock.network.packets.bedrock.world;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinClientPlayerInteractionManager;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinWorldRenderer;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

@Translate(PacketType.BEDROCK)
public final class LevelEventTranslator extends Translator<LevelEventPacket> {
    public static final Map<Vector3i, BlockBreakingWrapper> BLOCK_BREAKING_INFOS
            = new ConcurrentHashMap<>();
    public static final LongSet TO_REMOVE = new LongOpenHashSet();
    private static final Random random = Random.create();

    @Override
    public Class<LevelEventPacket> getPacketClass() {
        return LevelEventPacket.class;
    }

    @Override
    public void translate(LevelEventPacket packet) {
        var client = this.client();

        var world = client.world;
        var interactionManager = client.interactionManager;

        if (world == null || interactionManager == null) {
            return;
        }

        if (packet.getType() instanceof LevelEvent levelEvent) {
            switch (levelEvent) {
                case BLOCK_START_BREAK -> {
                    var position = packet.getPosition().toInt();
                    var blockBreakingInfo = new BlockBreakingInfo(0, GameUtils.toBlockPos(position));
                    var blockBreakingWrapper = new BlockBreakingWrapper(packet.getData(), blockBreakingInfo);
                    BLOCK_BREAKING_INFOS.put(position, blockBreakingWrapper);

                    SortedSet<BlockBreakingInfo> sortedSet = Sets.newTreeSet();
                    sortedSet.add(blockBreakingInfo);
                    ((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer)
                            .getBlockBreakingProgressions()
                            .put(GameUtils.asLong(position), sortedSet);
                }
                case BLOCK_UPDATE_BREAK -> {
                    BlockBreakingWrapper blockBreakingWrapper = BLOCK_BREAKING_INFOS.get(packet.getPosition().toInt());
                    if (blockBreakingWrapper == null) {
                        break;
                    }
                    blockBreakingWrapper.length = packet.getData();
                }
                case BLOCK_STOP_BREAK -> {
                    if (packet.getPosition().equals(Vector3f.ZERO)) {
                        if (BLOCK_BREAKING_INFOS.containsKey(packet.getPosition().toInt())) {
                            long key = ((IMixinClientPlayerInteractionManager) interactionManager).getCurrentBreakingPos().asLong();
                            TO_REMOVE.add(key);
                        }
                    } else {
                        Vector3i position = packet.getPosition().toInt();
                        if (BLOCK_BREAKING_INFOS.containsKey(position)) {
                            long key = BlockPos.asLong(position.getX(), position.getY(), position.getZ());
                            TO_REMOVE.add(key);
                        }
                    }
                }
                case PARTICLE_CRACK_BLOCK -> {
                    var direction = Direction.byId(packet.getData() >> 24);
                    var bedrockRuntimeId = packet.getData() & 0xffffff; // Strip out the above encoding
                    var blockState = BlockStateTranslator.getRuntime2Java().get(bedrockRuntimeId);
                    var vector = packet.getPosition().toInt();
                    var pos = GameUtils.toBlockPos(vector);

                    if (blockState != null && blockState.getRenderType() != BlockRenderType.INVISIBLE) {
                        int x = pos.getX();
                        int y = pos.getY();
                        int z = pos.getZ();

                        try {
                            Box box = blockState.getOutlineShape(world, pos).getBoundingBox();
                            double x1 = (double)x + random.nextDouble() * (box.maxX - box.minX - 0.20000000298023224) + 0.10000000149011612 + box.minX;
                            double y1 = (double)y + random.nextDouble() * (box.maxY - box.minY - 0.20000000298023224) + 0.10000000149011612 + box.minY;
                            double z1 = (double)z + random.nextDouble() * (box.maxZ - box.minZ - 0.20000000298023224) + 0.10000000149011612 + box.minZ;
                            switch (direction) {
                                case UP -> y1 = (double)y + box.maxY + 0.10000000149011612;
                                case DOWN -> y1 = (double)y + box.minY - 0.10000000149011612;
                                case NORTH -> z1 = (double)z + box.minZ - 0.10000000149011612;
                                case SOUTH -> z1 = (double)z + box.maxZ + 0.10000000149011612;
                                case WEST -> x1 = (double)x + box.minX - 0.10000000149011612;
                                case EAST -> x1 = (double)x + box.maxX + 0.10000000149011612;
                            }

                            client.particleManager.addParticle(
                                    (new BlockDustParticle(
                                            world, x1, y1, z1,
                                            0.0, 0.0, 0.0,
                                            blockState, pos)
                                    ).move(0.2F)
                                            .scale(0.6F)
                            );
                        } catch (UnsupportedOperationException ignored) { }
                    }
                }
                case PARTICLE_DESTROY_BLOCK -> MinecraftClient.getInstance()
                        .execute(() -> world.syncWorldEvent(client.player, 2001,
                                GameUtils.toBlockPos(packet.getPosition().toInt()),
                                Block.getRawIdFromState(BlockStateTranslator.getRuntime2Java().get(packet.getData()))));
            }
        }
    }

    public static class BlockBreakingWrapper {
        public long lastUpdate;
        public int length;
        public float currentDuration;
        public BlockBreakingInfo blockBreakingInfo;

        public BlockBreakingWrapper(int length, BlockBreakingInfo blockBreakingInfo) {
            this.length = length;
            this.blockBreakingInfo = blockBreakingInfo;
        }
    }
}

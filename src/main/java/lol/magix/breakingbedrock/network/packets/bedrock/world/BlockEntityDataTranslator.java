package lol.magix.breakingbedrock.network.packets.bedrock.world;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockentity.BlockEntityRegistry;
import lol.magix.breakingbedrock.utils.WorldUtils;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Translate(PacketType.BEDROCK)
public final class BlockEntityDataTranslator extends Translator<BlockEntityDataPacket> {
    private final static Constructor<BlockEntityUpdateS2CPacket> constructor;

    static {
        try {
            constructor = BlockEntityUpdateS2CPacket.class.getDeclaredConstructor(
                            BlockPos.class, BlockEntityType.class, NbtCompound.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<BlockEntityDataPacket> getPacketClass() {
        return BlockEntityDataPacket.class;
    }

    @Override
    public void translate(BlockEntityDataPacket packet) {
        this.bedrockClient.getBlockEntityDataCache()
                .getData().put(packet.getBlockPosition(), packet.getData());

        var translator = BlockEntityRegistry.getTranslator(packet.getData());
        if (translator != null) {
            var tag = translator.translateTag(packet.getData());
            try {
                this.javaClient().processPacket(constructor.newInstance(
                        WorldUtils.toBlockPos(packet.getBlockPosition()),
                        translator.getJavaId(), tag));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        this.logger.error("Couldn't find block entity translator for {}.",
                packet.getData().getString("id"));
    }
}

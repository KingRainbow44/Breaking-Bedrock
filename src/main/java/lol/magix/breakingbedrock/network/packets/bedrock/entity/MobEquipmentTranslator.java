package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.datafixers.util.Pair;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;

import java.util.Collections;

@Translate(PacketType.BEDROCK)
public final class MobEquipmentTranslator extends Translator<MobEquipmentPacket> {
    @Override
    public Class<MobEquipmentPacket> getPacketClass() {
        return MobEquipmentPacket.class;
    }

    @Override
    public void translate(MobEquipmentPacket packet) {
        var itemStack = new Pair<>(switch (packet.getContainerId()) {
            case ContainerId.INVENTORY -> EquipmentSlot.MAINHAND;
            case ContainerId.OFFHAND -> EquipmentSlot.OFFHAND;
            default -> throw new RuntimeException(
                    "Unknown container id: " + packet.getContainerId());
        }, ItemTranslator.bedrock2Java(packet.getItem()));

        this.javaClient().processPacket(new EntityEquipmentUpdateS2CPacket(
                (int) packet.getRuntimeEntityId(),
                Collections.singletonList(itemStack)
        ));
    }
}

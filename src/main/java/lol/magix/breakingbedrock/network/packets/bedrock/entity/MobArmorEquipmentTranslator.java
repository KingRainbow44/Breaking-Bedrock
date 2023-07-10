package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.MobArmorEquipmentPacket;

@Translate(PacketType.BEDROCK)
public final class MobArmorEquipmentTranslator extends Translator<MobArmorEquipmentPacket> {
    @Override
    public Class<MobArmorEquipmentPacket> getPacketClass() {
        return MobArmorEquipmentPacket.class;
    }

    @Override
    public void translate(MobArmorEquipmentPacket packet) {
        var javaEquipment = new ObjectArrayList<Pair<EquipmentSlot, ItemStack>>();
        javaEquipment.add(translate(EquipmentSlot.HEAD, packet.getHelmet()));
        javaEquipment.add(translate(EquipmentSlot.CHEST, packet.getChestplate()));
        javaEquipment.add(translate(EquipmentSlot.LEGS, packet.getLeggings()));
        javaEquipment.add(translate(EquipmentSlot.FEET, packet.getBoots()));

        this.javaClient().processPacket(new EntityEquipmentUpdateS2CPacket(
                (int) packet.getRuntimeEntityId(), javaEquipment
        ));
    }

    private static Pair<EquipmentSlot, ItemStack> translate(EquipmentSlot slot, ItemData item) {
        return new Pair<>(slot, ItemTranslator.bedrock2Java(item));
    }
}

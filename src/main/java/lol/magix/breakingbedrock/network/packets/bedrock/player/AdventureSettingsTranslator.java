package lol.magix.breakingbedrock.network.packets.bedrock.player;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import org.cloudburstmc.protocol.bedrock.data.AdventureSetting;
import org.cloudburstmc.protocol.bedrock.packet.AdventureSettingsPacket;

@Translate(PacketType.BEDROCK)
public final class AdventureSettingsTranslator extends Translator<AdventureSettingsPacket> {
    @Override
    public Class<AdventureSettingsPacket> getPacketClass() {
        return AdventureSettingsPacket.class;
    }

    @Override
    public void translate(AdventureSettingsPacket packet) {
        var settings = packet.getSettings();

        // Create the player abilities.
        var abilities = new PlayerAbilities();
        abilities.allowFlying = settings.contains(AdventureSetting.MAY_FLY);
        abilities.allowModifyWorld = settings.contains(AdventureSetting.BUILD);
        abilities.flying = settings.contains(AdventureSetting.FLYING);
        abilities.invulnerable = false;

        this.javaClient().processPacket(new PlayerAbilitiesS2CPacket(abilities));
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.authlib.GameProfile;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.SkinTranslator;
import lol.magix.breakingbedrock.utils.ProfileUtils;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;

import java.util.ArrayList;

@Translate(PacketType.BEDROCK)
public final class PlayerListTranslator extends Translator<PlayerListPacket> {
    @Override
    public Class<PlayerListPacket> getPacketClass() {
        return PlayerListPacket.class;
    }

    @Override
    public void translate(PlayerListPacket packet) {
        var action = packet.getAction() == PlayerListPacket.Action.ADD ?
                Action.ADD_PLAYER :
                Action.UPDATE_LISTED;

        var entries = new ArrayList<Entry>();
        for (var entry : packet.getEntries()) {
            var profile = new GameProfile(entry.getUuid(), entry.getName());
            var shouldList = action == Action.ADD_PLAYER;

            entries.add(new PlayerListS2CPacket.Entry(
                    entry.getUuid(), profile, shouldList, 0, GameMode.SURVIVAL,
                    Text.of(profile.getName()), null));

            if (shouldList) {
                SkinTranslator.addSerializedSkin(profile.getId(), entry.getSkin());
            }
        }

        this.javaClient().processPacket(new PlayerListS2CPacket(
                ProfileUtils.asPacket(entries, action)
        ));
    }
}

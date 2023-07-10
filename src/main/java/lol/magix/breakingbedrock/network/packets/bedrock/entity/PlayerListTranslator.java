package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.authlib.GameProfile;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
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

        var toAdd = new ArrayList<Entry>();
        var toRemove = new ArrayList<Entry>();

        for (var entry : packet.getEntries()) {
            var profile = new GameProfile(entry.getUuid(), entry.getName());
            var listEntry = new PlayerListS2CPacket.Entry(
                    entry.getUuid(), profile, true, 0, GameMode.SURVIVAL,
                    Text.of(profile.getName()), null);

            if (action == Action.ADD_PLAYER) {
                var existingEntry = this.javaClient()
                        .getLocalNetwork().getPlayerListEntry(profile.getId());
                if (existingEntry != null) toRemove.add(listEntry);
            }

            toAdd.add(listEntry);
        }

        if (!toRemove.isEmpty()) {
            this.javaClient().processPacket(new PlayerListS2CPacket(
                    ProfileUtils.asPacket(toRemove, Action.UPDATE_LISTED)
            ));
        }

        this.javaClient().processPacket(new PlayerListS2CPacket(
                ProfileUtils.asPacket(toAdd, action)
        ));
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.game;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.TextUtils;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;

@Translate(PacketType.BEDROCK)
public final class SetTitleTranslator extends Translator<SetTitlePacket> {
    @Override
    public Class<SetTitlePacket> getPacketClass() {
        return SetTitlePacket.class;
    }

    @Override
    public void translate(SetTitlePacket packet) {
        switch (packet.getType()) {
            case CLEAR -> this.javaClient().processPacket(
                    new ClearTitleS2CPacket(false));
            case RESET -> this.javaClient().processPacket(
                    new ClearTitleS2CPacket(true));
            case TITLE -> this.javaClient().processPacket(
                    new TitleS2CPacket(TextUtils.translate(packet.getText())));
            case SUBTITLE -> this.javaClient().processPacket(
                    new SubtitleS2CPacket(TextUtils.translate(packet.getText())));
            case ACTIONBAR -> this.player().sendMessage(
                    TextUtils.translate(packet.getText()), true);
            case TIMES -> this.javaClient().processPacket(
                    new TitleFadeS2CPacket(packet.getFadeInTime(),
                            packet.getStayTime(), packet.getFadeOutTime()));
        }
    }
}

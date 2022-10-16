package lol.magix.breakingbedrock.network.packets.misc;

import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ScreenUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@Translate(PacketType.BEDROCK)
public final class DisconnectTranslator extends Translator<DisconnectPacket> {
    @Override
    public Class<DisconnectPacket> getPacketClass() {
        return DisconnectPacket.class;
    }

    @Override
    public void translate(DisconnectPacket packet) {
        // Disconnect from all sources.
        var world = MinecraftClient.getInstance().world;
        if (world != null) world.disconnect();

        // Show disconnect screen.
        MinecraftClient.getInstance().execute(() -> ScreenUtils.disconnect(
                Text.of(packet.getKickMessage())));
    }
}

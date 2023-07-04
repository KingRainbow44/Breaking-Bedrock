package lol.magix.breakingbedrock.network.packets.java;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket.State;

@Translate(PacketType.JAVA)
public final class ClientStatusC2STranslator extends Translator<ClientStatusC2SPacket> {
    @Override
    public Class<ClientStatusC2SPacket> getPacketClass() {
        return ClientStatusC2SPacket.class;
    }

    @Override
    public void translate(ClientStatusC2SPacket packet) {
        var player = this.player();
        if (player == null) return;

        if (packet.getMode() == Mode.PERFORM_RESPAWN) {
            var respawnPacket = new RespawnPacket();
            respawnPacket.setPosition(Vector3f.ZERO);
            respawnPacket.setState(State.CLIENT_READY);
            respawnPacket.setRuntimeEntityId(this.data().getRuntimeId());

            this.bedrockClient.sendPacket(respawnPacket);
        }
    }
}

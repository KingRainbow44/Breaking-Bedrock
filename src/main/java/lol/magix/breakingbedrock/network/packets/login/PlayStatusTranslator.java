package lol.magix.breakingbedrock.network.packets.login;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import org.cloudburstmc.protocol.bedrock.packet.PlayStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.bedrock.packet.TickSyncPacket;

@Translate(PacketType.BEDROCK)
public final class PlayStatusTranslator extends Translator<PlayStatusPacket> {
    @Override
    public Class<PlayStatusPacket> getPacketClass() {
        return PlayStatusPacket.class;
    }

    @Override
    public void translate(PlayStatusPacket packet) {
        var status = packet.getStatus();

        switch (status) {
            default -> this.logger.warn("Unknown play status: " + status);

            case LOGIN_SUCCESS -> {
                if (this.shouldLog)
                    this.logger.info("Login accepted by server.");
            }

            case PLAYER_SPAWN -> {
                if (this.shouldLog)
                    this.logger.info("Attempting to spawn player...");

                // Wait for the player to initialize.
                while (!this.data().isInitialized()) {
                    try {
                        this.logger.info("Waiting 3s for player to initialize...");
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) { }
                }

                // Complete server-side initialization.
                var tickPacket = new TickSyncPacket();
                this.bedrockClient.sendPacket(tickPacket, true);

                var completePacket = new SetLocalPlayerAsInitializedPacket();
                completePacket.setRuntimeEntityId(this.data().getRuntimeId());
                this.bedrockClient.sendPacket(completePacket, true);
            }
        }
    }
}

package lol.magix.breakingbedrock.network.packets.java.chat;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket;

@Translate(PacketType.JAVA)
public final class CommandExecutionC2STranslator extends Translator<CommandExecutionC2SPacket> {
    @Override
    public Class<CommandExecutionC2SPacket> getPacketClass() {
        return CommandExecutionC2SPacket.class;
    }

    @Override
    public void translate(CommandExecutionC2SPacket packet) {
        var commandPacket = new CommandRequestPacket();
        commandPacket.setCommand("/" + packet.command());
        commandPacket.setCommandOriginData(new CommandOriginData(
                CommandOriginType.PLAYER,
                this.bedrockClient.getAuthentication().getIdentity(),
                "", 0
        ));

        this.bedrockClient.sendPacket(commandPacket);
    }
}

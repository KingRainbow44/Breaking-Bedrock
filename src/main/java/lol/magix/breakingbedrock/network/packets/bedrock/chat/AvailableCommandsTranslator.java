    package lol.magix.breakingbedrock.network.packets.bedrock.chat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

@Translate(PacketType.BEDROCK)
public final class AvailableCommandsTranslator extends Translator<AvailableCommandsPacket> {
    @Override
    public Class<AvailableCommandsPacket> getPacketClass() {
        return AvailableCommandsPacket.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void translate(AvailableCommandsPacket packet) {
        var root = new RootCommandNode<CommandSource>();

        try {
            // Translate all commands.
            for (var command : packet.getCommands()) {
                var builder = LiteralArgumentBuilder.literal(command.getName());
                for (var subCommand : command.getSubcommands()) {
                    builder.then(literal(subCommand.getName())
                            .executes(AvailableCommandsTranslator::run));
                }

                var aliases = command.getAliases();
                if (aliases != null) {
                    for (var alias : aliases.getValues().keySet()) {
                        builder.then(literal(alias));
                    }
                }

                root.addChild((CommandNode<CommandSource>) (Object) builder.build());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        this.javaClient().processPacket(new CommandTreeS2CPacket(root));
    }

    /**
     * Dummy method to make the compiler happy.
     *
     * @param context Command context.
     * @return Dummy value.
     */
    private static int run(CommandContext<?> context) {
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Dummy method to make the compiler happy.
     *
     * @param object Object to check.
     * @return Dummy value.
     */
    private static boolean requirement(Object object) {
        return true;
    }
}

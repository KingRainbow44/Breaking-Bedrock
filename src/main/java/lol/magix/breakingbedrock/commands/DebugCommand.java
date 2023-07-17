package lol.magix.breakingbedrock.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static lol.magix.breakingbedrock.objects.game.TextBuilder.translate;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public interface DebugCommand {
    Text USAGE = Text.literal("Usage: /debug <log> [packets]")
            .setStyle(Style.EMPTY.withColor(0xFF462E));

    /**
     * Registers the '/debug' command.
     */
    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher
                .register(literal("debug")
                        .then(literal("log")
                                .then(argument("log", word())
                                        .executes(DebugCommand::toggleLogging)))
                        .then(literal("resourcepack")
                                .executes(DebugCommand::translateResourcePack))
                .executes(DebugCommand::showUsage));
    }

    /**
     * Called when the command is used without any arguments.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int showUsage(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendError(USAGE);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called when the command is used with the 'log' argument.
     * Enables/disables debug logging for a specific type.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int toggleLogging(CommandContext<FabricClientCommandSource> context) {
        var log = getString(context, "log");

        var bedrockClient = BedrockNetworkClient.getInstance();
        var clientData = bedrockClient.getData();

        switch (log) {
            default -> context.getSource().sendError(
                    Text.of("Unknown log type: " + log));
            case "packets" -> {
                var newValue = !clientData.isLogPackets();
                clientData.setLogPackets(newValue);

                context.getSource().sendFeedback(translate("commands.debug.packets")
                        .args(newValue ? "enabled" : "disabled").color(newValue ? 0x00FF6D : 0xFF462E).get());
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called when the command is used with the 'resourcepack' argument.
     * Translates the resource pack.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int translateResourcePack(CommandContext<FabricClientCommandSource> context) {
        var resourcePackId = System.getProperty("DebugResourcePackId");
        var resourcePackKey = System.getProperty("DebugResourcePackKey");

        context.getSource().sendFeedback(translate("commands.debug.resourcepack")
                .args(resourcePackId, resourcePackKey).color(0xFFFE00).get());

        // Create a new resource pack.
        var pack = ResourcePackTranslator.getCache().get(resourcePackId);
        if (pack == null) {
            throw new NullPointerException("Invalid resource pack ID: " + resourcePackId);
        }

        try {
            // Translate the resource pack.
            ResourcePackTranslator.translate(pack);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }
}

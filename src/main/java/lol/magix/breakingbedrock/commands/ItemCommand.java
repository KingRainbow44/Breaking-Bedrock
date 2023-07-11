package lol.magix.breakingbedrock.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public interface ItemCommand {
    Text INVALID_IDENTIFIER = Text.literal("No runtime ID specified.")
            .setStyle(Style.EMPTY.withColor(0xC03714));
    Text UNCONNECTED_CLIENT = Text.literal("The client is not connected to a server.")
            .setStyle(Style.EMPTY.withColor(0xC03714));

    /**
     * Registers the '/item' command.
     */
    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("item")
                .then(literal("id")
                        .then(argument("identifier", string())
                                .executes(ItemCommand::fetchById))
                        .executes(ItemCommand::noIdSpecified))
                .executes(ItemCommand::showUsage));
    }

    /**
     * Called when the command is used without any arguments.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int showUsage(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendError(Text.literal("Usage: /item id <item identifier>")
                .setStyle(Style.EMPTY.withColor(0xC03714)));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called when no ID is specified.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int noIdSpecified(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendError(Text.literal("No identifier specified. (format in 'namespace:identifier')")
                .setStyle(Style.EMPTY.withColor(0xC03714)));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Fetches an item by its ID.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int fetchById(CommandContext<FabricClientCommandSource> context) {
        var id = getString(context, "identifier");
        if (!id.contains(":")) {
            context.getSource().sendError(INVALID_IDENTIFIER);
            return Command.SINGLE_SUCCESS;
        }

        // Get the connected client.
        var client = BedrockNetworkClient.getInstance();
        if (client == null || !client.isConnected()) {
            context.getSource().sendError(UNCONNECTED_CLIENT);
            return Command.SINGLE_SUCCESS;
        }

        var bedrockItem = client.getData().getId2Runtime().get(id);
        context.getSource().sendFeedback(Text.literal("The Bedrock runtime ID is " + bedrockItem)
                .setStyle(Style.EMPTY.withColor(0x1EC057)));

        return Command.SINGLE_SUCCESS;
    }
}

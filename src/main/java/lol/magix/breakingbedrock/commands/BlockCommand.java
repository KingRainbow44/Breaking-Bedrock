package lol.magix.breakingbedrock.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lol.magix.breakingbedrock.translators.blockstate.BlockPaletteTranslator;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public interface BlockCommand {
    /**
     * Registers the '/block' command.
     */
    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("block")
                .then(literal("runtime")
                        .then(argument("runtimeId", integer())
                                .executes(BlockCommand::fetchByRuntimeId))
                        .executes(BlockCommand::noRuntimeSpecified))
                .executes(BlockCommand::showUsage));
    }

    /**
     * Called when the command is used without any arguments.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int showUsage(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendError(Text.literal("Usage: /block runtime <runtime ID>")
                .setStyle(Style.EMPTY.withColor(0xC03714)));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called when no runtime ID is specified.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int noRuntimeSpecified(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendError(Text.literal("No runtime ID specified.")
                .setStyle(Style.EMPTY.withColor(0xC03714)));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Fetches a block by its runtime ID.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int fetchByRuntimeId(CommandContext<FabricClientCommandSource> context) {
        var runtimeId = getInteger(context, "runtimeId");
        var bedrockBlock = BlockPaletteTranslator.getRuntime2Bedrock().get(runtimeId);

        var source = context.getSource();
        if (bedrockBlock == null) {
            source.sendFeedback(Text.literal("The runtime ID is invalid.")
                    .setStyle(Style.EMPTY.withColor(0xC03714)));
        } else {
            source.sendFeedback(Text.literal("The Bedrock state is " + bedrockBlock)
                    .setStyle(Style.EMPTY.withColor(0x1EC057)));
            source.sendFeedback(Text.literal("The Java state is " + bedrockBlock.toJavaBlockState())
                    .setStyle(Style.EMPTY.withColor(0xFFF710)));
        }

        return Command.SINGLE_SUCCESS;
    }
}

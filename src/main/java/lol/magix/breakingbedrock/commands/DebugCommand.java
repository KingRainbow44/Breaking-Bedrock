package lol.magix.breakingbedrock.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.translators.pack.ResourcePackTranslator;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static lol.magix.breakingbedrock.objects.game.TextBuilder.translate;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public interface DebugCommand {
    AtomicBoolean ENTITY_DEBUG = new AtomicBoolean(false);
    Consumer<Entity> ENTITY_LOGGER = DebugCommand::logEntity;

    Text USAGE = Text.literal("Usage: /debug <log> [packets]")
            .setStyle(Style.EMPTY.withColor(0xFF462E));

    /**
     * Callback for entity logging.
     *
     * @param entity The entity to log.
     */
    static void logEntity(Entity entity) {
        if (entity instanceof ArmorStandEntity nameTag) {
            var customName = nameTag.getCustomName();
            System.out.println("custom name exists: " + (customName != null));
            if (customName != null) System.out.println("custom name: " + customName);
            System.out.println("custom name shown: " + (nameTag.isCustomNameVisible()));
        } else {
            System.out.println("entity is not name tag");
            System.out.println("entity type: " + (entity == null ?
                    "null" : entity.getClass().getSimpleName()));
        }
    }

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
                        .then(literal("entity")
                                .executes(DebugCommand::enableEntityLogging))
                        .then(literal("blockactions")
                                .executes(DebugCommand::listBlockActions))
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

    /**
     * Called when the command is used with the 'entity' argument.
     * Enables/disables entity logging.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int enableEntityLogging(CommandContext<FabricClientCommandSource> context) {
        var newValue = !ENTITY_DEBUG.get();
        ENTITY_DEBUG.set(newValue);

        context.getSource().sendFeedback(translate("commands.debug.entity")
                .args(newValue ? "enabled" : "disabled").color(newValue ? 0x00FF6D : 0xFF462E).get());

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called when the command is used with the 'blockactions' argument.
     * Lists all block actions.
     *
     * @param context The command context.
     * @return The command result.
     */
    static int listBlockActions(CommandContext<FabricClientCommandSource> context) {
        var client = BedrockNetworkClient.getInstance();
        client.getLogger().info(EncodingUtils.jsonEncode(client.getBlockActions()));
        return Command.SINGLE_SUCCESS;
    }
}

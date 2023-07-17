package lol.magix.breakingbedrock;

import com.mojang.brigadier.Command;
import lol.magix.breakingbedrock.commands.BlockCommand;
import lol.magix.breakingbedrock.commands.DebugCommand;
import lol.magix.breakingbedrock.commands.ItemCommand;
import lol.magix.breakingbedrock.translators.FormTranslator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public final class BreakingBedrockInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register commands.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            dispatcher
                    .register(literal("formoption")
                            .then(argument("formId", integer())
                            .then(argument("option", integer())
                            .executes(context -> {
                                var formId = getInteger(context, "formId");
                                var option = getInteger(context, "option");
                                FormTranslator.runOption(formId, option);

                                return Command.SINGLE_SUCCESS;
                            })))
                    );
            dispatcher
                    .register(literal("formsubmit")
                            .then(argument("formId", integer())
                            .executes(context -> {
                                var formId = getInteger(context, "formId");
                                FormTranslator.submitForm(formId);

                                return Command.SINGLE_SUCCESS;
                            }))
                    );

            // Register external commands.
            BlockCommand.register(dispatcher);
            ItemCommand.register(dispatcher);
            DebugCommand.register(dispatcher);
        });
    }
}

package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.screens.JoinBedrockServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public final class MixinMultiplayerScreen extends Screen {
    private MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "RETURN"))
    public void init(CallbackInfo callback) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.of("Connect To Bedrock Server"),
                this::bedrockConnect)
                .size(150, 20)
                .position(5, 5).build());
    }

    /**
     * Opens the {@link JoinBedrockServerScreen} screen.
     * @param button The button clicked.
     */
    private void bedrockConnect(ButtonWidget button) {
        if (this.client == null)
            return;

        this.client.setScreen(new JoinBedrockServerScreen(this));
    }
}

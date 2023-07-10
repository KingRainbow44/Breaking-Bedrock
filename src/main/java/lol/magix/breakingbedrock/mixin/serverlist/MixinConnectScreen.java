package lol.magix.breakingbedrock.mixin.serverlist;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.ConnectionDetails;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.game.BedrockServerInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen extends Screen {
    @Shadow volatile boolean connectingCancelled;
    @Shadow @Final Screen parent;

    @Shadow protected abstract void setStatus(Text status);

    protected MixinConnectScreen(Text title) {
        super(title);
    }

    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void connect(MinecraftClient client, ServerAddress address, ServerInfo info, CallbackInfo ci) {
        if (this.client == null) return;

        if (!(info instanceof BedrockServerInfo)) {
            return;
        }
        ci.cancel();

        // Attempt to connect to the server.
        var connectionAddress = new ConnectionDetails(address.getAddress(), address.getPort(),
                GameConstants.DEFAULT_AUTHENTICATION);

        this.setStatus(Text.translatable("connect.connecting"));
        BedrockNetworkClient.getInstance().connect(connectionAddress, this::setStatus, () -> {
            if (this.connectingCancelled) {
                BedrockNetworkClient.getInstance().disconnect();
                this.client.setScreen(this.parent);
            }

            return this.connectingCancelled;
        });
    }
}

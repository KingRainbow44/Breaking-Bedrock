package lol.magix.breakingbedrock.mixin;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.network.translation.PacketTranslator;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientConnection.class)
public final class MixinClientConnection {
    @Shadow
    private Text disconnectReason;

    @Inject(method = "isOpen", at = @At("HEAD"), cancellable = true)
    public void isOpen(CallbackInfoReturnable<Boolean> callback) {
        if (BedrockNetworkClient.getInstance().isConnected()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "isEncrypted", at = @At("HEAD"), cancellable = true)
    public void isEncrypted(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (BedrockNetworkClient.getInstance().isConnected()) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo callback) {
        if (BedrockNetworkClient.getInstance().isConnected()) {
            PacketTranslator.getJavaTranslator().translatePacket(packet);
            callback.cancel();
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    public void disconnect(Text disconnectReason, CallbackInfo callback) {
        if (BedrockNetworkClient.getInstance().isConnected()) {
            BedrockNetworkClient.getHandle().close("Client closed peer connection");
            this.disconnectReason = disconnectReason;
            callback.cancel();
        }
    }
}

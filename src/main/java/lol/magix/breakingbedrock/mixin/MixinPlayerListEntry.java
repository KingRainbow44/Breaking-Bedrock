package lol.magix.breakingbedrock.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.translators.SkinTranslator;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry {
    @Shadow public abstract GameProfile getProfile();

    /**
     * @param type The type of the texture.
     * @return The texture part.
     */
    @Unique
    private Identifier getTexturePart(Type type) {
        return !BedrockNetworkClient.connected() ? null :
                SkinTranslator.getTexturePart(type, this.getProfile().getId());
    }

    @Inject(method = "getModel", at = @At(value = "HEAD"), cancellable = true)
    public void getModel(CallbackInfoReturnable<String> cir) {
        if (!BedrockNetworkClient.connected()) return;
        cir.setReturnValue(SkinTranslator.getModel(this.getProfile().getId()));
    }

    @Inject(method = "getSkinTexture", at = @At(value = "TAIL"), cancellable = true)
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        var identifier = this.getTexturePart(Type.SKIN);
        if (identifier != null) cir.setReturnValue(identifier);
    }

    @Inject(method = "getCapeTexture", at = @At(value = "TAIL"), cancellable = true)
    public void getCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        var identifier = this.getTexturePart(Type.CAPE);
        if (identifier != null) cir.setReturnValue(identifier);
    }

    @Inject(method = "getElytraTexture", at = @At(value = "HEAD"), cancellable = true)
    public void getElytraTexture(CallbackInfoReturnable<Identifier> cir) {
        if (!BedrockNetworkClient.connected()) return;
        cir.setReturnValue(null);
    }
}

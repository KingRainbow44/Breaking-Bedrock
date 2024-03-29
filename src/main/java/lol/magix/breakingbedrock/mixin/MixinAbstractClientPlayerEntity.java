package lol.magix.breakingbedrock.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.game.ScoreNameEntity;
import lol.magix.breakingbedrock.translators.SkinTranslator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements ScoreNameEntity {
    @Unique @Getter @Setter @Nullable
    private Text scoreName = null;

    public MixinAbstractClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    /**
     * @param type The type of the texture.
     * @return The texture part.
     */
    @Unique
    private Identifier getTexturePart(Type type) {
        return !BedrockNetworkClient.connected() ? null :
                SkinTranslator.getTexturePart(type, this.getUuid());
    }

    @Inject(method = "getModel", at = @At(value = "HEAD"), cancellable = true)
    public void getModel(CallbackInfoReturnable<String> cir) {
        if (!BedrockNetworkClient.connected()) return;
        cir.setReturnValue(SkinTranslator.getModel(this.getUuid()));
    }

    @Inject(method = "hasSkinTexture", at = @At(value = "HEAD"), cancellable = true)
    public void hasSkinTexture(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.getTexturePart(Type.SKIN) != null);
    }

    @Inject(method = "getSkinTexture", at = @At(value = "TAIL"), cancellable = true)
    public void getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
        var identifier = this.getTexturePart(Type.SKIN);
        if (identifier != null) cir.setReturnValue(identifier);
    }

    @Inject(method = "canRenderCapeTexture", at = @At(value = "HEAD"), cancellable = true)
    public void canRenderCapeTexture(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.getTexturePart(Type.CAPE) != null);
    }

    @Inject(method = "getCapeTexture", at = @At(value = "TAIL"), cancellable = true)
    public void getCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        var identifier = this.getTexturePart(Type.CAPE);
        if (identifier != null) cir.setReturnValue(identifier);
    }

    @Inject(method = "canRenderElytraTexture", at = @At(value = "HEAD"), cancellable = true)
    public void canRenderElytraTexture(CallbackInfoReturnable<Boolean> cir) {
        if (!BedrockNetworkClient.connected()) return;
        cir.setReturnValue(false);
    }
}

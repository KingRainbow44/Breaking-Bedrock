package lol.magix.breakingbedrock.mixin;

import com.google.common.collect.ImmutableList;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ResourcePackManager.class)
public abstract class MixinResourcePackManager {
    @Shadow private List<ResourcePackProfile> enabled;

    @Inject(method = "createResourcePacks", at = @At("HEAD"), cancellable = true)
    public void createResourcePacks(CallbackInfoReturnable<List<ResourcePack>> cir) {
        // Add all resource packs from the server.
        var bedrockClient = BedrockNetworkClient.getInstance();
        if (bedrockClient == null) return;
        var javaClient = bedrockClient.getJavaNetworkClient();
        if (javaClient == null) return;

        // Get the active resource packs.
        var copy = new ArrayList<>(this.enabled);
        copy.addAll(javaClient.getResourcePacks());
        cir.setReturnValue(copy.stream()
                .map(ResourcePackProfile::createResourcePack)
                .collect(ImmutableList.toImmutableList()));
    }
}

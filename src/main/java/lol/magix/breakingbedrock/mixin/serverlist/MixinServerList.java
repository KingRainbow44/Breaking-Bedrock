package lol.magix.breakingbedrock.mixin.serverlist;

import lol.magix.breakingbedrock.objects.game.BedrockServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerList.class)
public class MixinServerList {
    @Redirect(method = "loadFile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ServerInfo;fromNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/client/network/ServerInfo;"))
    public ServerInfo fromNbt(NbtCompound root) {
        if (root.contains("bedrock", NbtElement.BYTE_TYPE)) {
            return BedrockServerInfo.fromNbt(root);
        }

        return ServerInfo.fromNbt(root);
    }
}

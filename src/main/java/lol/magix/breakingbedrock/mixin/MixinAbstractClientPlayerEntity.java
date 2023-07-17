package lol.magix.breakingbedrock.mixin;

import com.mojang.authlib.GameProfile;
import lol.magix.breakingbedrock.objects.game.ScoreNameEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements ScoreNameEntity {
    @Unique @Getter @Setter @Nullable
    private Text scoreName = null;

    public MixinAbstractClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
}

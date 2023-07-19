package lol.magix.breakingbedrock.network.packets.bedrock.world;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;

@Translate(PacketType.BEDROCK)
public final class LevelSoundEventTranslator extends Translator<LevelSoundEventPacket> {
    @Override
    public Class<LevelSoundEventPacket> getPacketClass() {
        return LevelSoundEventPacket.class;
    }

    @Override
    public void translate(LevelSoundEventPacket packet) {
        var soundManager = this.client().getSoundManager();
        var position = GameUtils.toBlockPos(packet.getPosition().toInt());

        switch (packet.getSound()) {
            case HIT -> {
                var blockState = BlockStateTranslator.getRuntime2Java()
                        .get(packet.getExtraData());
                if (blockState == null) return;
                var soundGroup = blockState.getSoundGroup();

                soundManager.play(new PositionedSoundInstance(soundGroup.getHitSound(), SoundCategory.BLOCKS,
                        (soundGroup.getVolume() + 1.0F) / 8.0F,
                        soundGroup.getPitch() * 0.5F,
                        SoundInstance.createRandom(), position));
            }
            case ATTACK_NODAMAGE -> soundManager.play(new PositionedSoundInstance(
                    SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                    SoundCategory.PLAYERS, 1.0F, 1.0F,
                    SoundInstance.createRandom(), position
            ));
            case ATTACK_STRONG -> soundManager.play(new PositionedSoundInstance(
                    SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                    SoundCategory.PLAYERS, 1.0F, 1.0F,
                    SoundInstance.createRandom(), position
            ));
        }
    }
}

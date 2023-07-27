package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinPlayerEntity;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.translators.entity.EntityMetadataTranslator;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import lol.magix.breakingbedrock.utils.ProfileUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;

import java.util.List;

@Translate(PacketType.BEDROCK)
public final class AddPlayerTranslator extends Translator<AddPlayerPacket> {
    @Override
    public Class<AddPlayerPacket> getPacketClass() {
        return AddPlayerPacket.class;
    }

    @Override
    public void translate(AddPlayerPacket packet) {
        var world = this.client().world;
        if (world == null) {
            this.data().getPendingPlayers().add(packet);
            return;
        }

        var runtimeId = (int) packet.getRuntimeEntityId();
        var identity = packet.getUuid();
        var name = packet.getUsername();

        var position = packet.getPosition();
        var rotation = packet.getRotation();
        var motion = packet.getMotion();

        this.run(() -> {
            // Create a player profile.
            var profile = new GameProfile(identity, name);
            var player = new OtherClientPlayerEntity(world, profile);

            // Set player data.
            player.setId(runtimeId);
            player.setPos(position.getX(), position.getY(), position.getZ());
            player.setPitch(rotation.getX());
            player.setYaw(rotation.getY());
            player.setHeadYaw(rotation.getZ());
            player.setVelocity(motion.getX(), motion.getY(), motion.getZ());

            // Add the player to the player list.
            var networkHandler = this.javaClient().getLocalNetwork();
            if (networkHandler.getPlayerListEntry(identity) == null) {
                // Create a player list entry.
                var shortened = profile.getName();
                if (shortened != null && shortened.length() > 16) {
                    shortened = shortened.substring(0, 16);
                }

                var entry = new PlayerListS2CPacket.Entry(
                        identity, profile, true, 0,
                        ConversionUtils.convertBedrockGameMode(packet.getGameType()),
                        Text.of(shortened), null);
                this.javaClient().processPacket(new PlayerListS2CPacket(
                        ProfileUtils.asPacket(List.of(entry), Action.ADD_PLAYER)
                ));
            }

            // Spawn the player to the client.
            this.javaClient().processPacket(new PlayerSpawnS2CPacket(player));

            // Set the item the player is holding.
            var itemStack = new Pair<>(EquipmentSlot.MAINHAND,
                    ItemTranslator.bedrock2Java(packet.getHand()));
            this.javaClient().processPacket(new EntityEquipmentUpdateS2CPacket(runtimeId, List.of(itemStack)));

            // Translate the player's metadata.
            EntityMetadataTranslator.translate(
                    new lol.magix.breakingbedrock.objects.Pair<>(player, packet.getMetadata()));

            // Set the player skin flags.
            player.getDataTracker().set(
                    IMixinPlayerEntity.PLAYER_MODEL_PARTS(), (byte) (
                            PlayerModelPart.JACKET.getBitFlag()
                                    | PlayerModelPart.HAT.getBitFlag()
                                    | PlayerModelPart.LEFT_SLEEVE.getBitFlag()
                                    | PlayerModelPart.LEFT_PANTS_LEG.getBitFlag()
                                    | PlayerModelPart.RIGHT_SLEEVE.getBitFlag()
                                    | PlayerModelPart.RIGHT_PANTS_LEG.getBitFlag()
                                    | PlayerModelPart.CAPE.getBitFlag()
                    )
            );
        });
    }
}

package lol.magix.breakingbedrock.network.packets.bedrock.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.mixin.interfaces.IMixinPlayerEntity;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.translators.ItemTranslator;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;

import java.util.EnumSet;
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
        if (world == null) return;

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
                var entry = new PlayerListS2CPacket.Entry(
                        identity, profile, true, 0,
                        ConversionUtils.convertBedrockGameMode(packet.getGameType()),
                        Text.of(profile.getName()), null);

                // Create a buffer representing the packet.
                var buffer = PacketByteBufs.create();
                buffer.writeEnumSet(EnumSet.of(Action.ADD_PLAYER), Action.class);
                buffer.writeCollection(List.of(entry), (buf, anEntry) -> {
                    buf.writeUuid(anEntry.profileId());
                    var writer = AddPlayerTranslator.getWriterFor(Action.ADD_PLAYER);
                    if (writer != null) writer.write(buf, anEntry);;
                });

                this.javaClient().processPacket(new PlayerListS2CPacket(buffer));
            }

            // Spawn the player to the client.
            this.javaClient().processPacket(new PlayerSpawnS2CPacket(player));

            // Set the item the player is holding.
            var itemStack = new Pair<>(EquipmentSlot.MAINHAND,
                    ItemTranslator.bedrock2Java(packet.getHand()));
            this.javaClient().processPacket(new EntityEquipmentUpdateS2CPacket(runtimeId, List.of(itemStack)));

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
            this.javaClient().processPacket(new EntityTrackerUpdateS2CPacket(
                    player.getId(), player.getDataTracker().getChangedEntries()));
        });
    }

    /**
     * Get the writer for the given action.
     *
     * @param action The action.
     * @return The writer.
     */
    private static Action.Writer getWriterFor(Action action) {
        try {
            var type = action.getClass();
            var field = type.getDeclaredField("writer");
            field.setAccessible(true);
            return (Action.Writer) field.get(action);
        } catch (Exception ignored) {
            return null;
        }
    }
}

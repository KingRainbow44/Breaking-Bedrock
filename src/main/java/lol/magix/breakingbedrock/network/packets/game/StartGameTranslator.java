package lol.magix.breakingbedrock.network.packets.game;

import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Translate(PacketType.BEDROCK)
public final class StartGameTranslator extends Translator<StartGamePacket> {
    @Override
    public Class<StartGamePacket> getPacketClass() {
        return StartGamePacket.class;
    }

    @Override
    public void translate(StartGamePacket packet) {
        // Initial boilerplate start game code.
        var entityId = (int) packet.getRuntimeEntityId();
        var hashSeed = packet.getSeed();

        // TODO: Convert item entries.

        // Set player's gamemode.
        var previousGameMode = GameMode.DEFAULT;
        var currentGameMode = ConversionUtils.convertBedrockGameMode(packet.getPlayerGameType());

        // Register available dimensions.
        var hardcore = false;
        var dimensionIds = new HashSet<>(List.of(
                World.NETHER, World.OVERWORLD, World.END));

        // Get the current dimension.
        var dimensionType = ConversionUtils.convertBedrockDimension(packet.getDimensionId());
        var dimensionId = ConversionUtils.convertBedrockWorld(packet.getDimensionId());

        // Do game properties.
        int maxPlayers = 999, chunkLoadDistance = 3;
        boolean reducedDebugInfo = false, showDeathScreen = true,
                debugWorld = false, flatWorld = false;

        for (var gamerule : packet.getGamerules()) {
            switch (gamerule.getName()) {
                case "doimmediaterespawn" ->
                        showDeathScreen = !((Boolean) gamerule.getValue());
            }
        }

        // Connect on the local network.
        var gameJoinPacket = new GameJoinS2CPacket(
                entityId, hardcore, previousGameMode, currentGameMode,
                dimensionIds, DynamicRegistryManager.EMPTY, dimensionType,
                dimensionId, hashSeed, maxPlayers, chunkLoadDistance, chunkLoadDistance,
                reducedDebugInfo, showDeathScreen, debugWorld, flatWorld,
                Optional.empty(), 0
        );
        this.javaClient().processPacket(gameJoinPacket);

        // Initialize the Bedrock client.
        this.bedrockClient.onPlayerInitialization();

        // --- POST INITIALIZATION --- \\

        // Update gamerules.
        this.run(() -> ConversionUtils.updateGameRules(packet.getGamerules()));

        // Set position.
        var playerPosition = packet.getPlayerPosition();
        var playerRotation = packet.getRotation();
        var xPos = playerPosition.getX();
        var yPos = playerPosition.getY();
        var zPos = playerPosition.getZ();
        var pitch = playerRotation.getX();
        var yaw = playerRotation.getY();

        var positionPacket = new PlayerPositionLookS2CPacket(xPos, yPos, zPos, yaw, pitch,
                Collections.emptySet(), 0);
        this.javaClient().processPacket(positionPacket);

        // Set the render center.
        int chunkX = MathHelper.floor(xPos) >> 4;
        int chunkZ = MathHelper.floor(zPos) >> 4;
        var centerPacket = new ChunkRenderDistanceCenterS2CPacket(chunkX, chunkZ);
        this.javaClient().processPacket(centerPacket);

        // Mark the player as initialized.
        this.data().setInitialized(true);
        this.data().setRuntimeId(entityId);

        // Request the render distance.
        var distancePacket = new RequestChunkRadiusPacket();
        distancePacket.setRadius(chunkLoadDistance);
        this.bedrockClient.sendPacket(distancePacket, true);
    }
}

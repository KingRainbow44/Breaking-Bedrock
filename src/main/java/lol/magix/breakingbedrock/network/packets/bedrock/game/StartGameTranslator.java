package lol.magix.breakingbedrock.network.packets.bedrock.game;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lol.magix.breakingbedrock.annotations.Translate;
import lol.magix.breakingbedrock.network.translation.Translator;
import lol.magix.breakingbedrock.objects.absolute.GameConstants;
import lol.magix.breakingbedrock.objects.absolute.PacketType;
import lol.magix.breakingbedrock.utils.ConversionUtils;
import lol.magix.breakingbedrock.utils.GameUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

import java.util.*;

@Translate(PacketType.BEDROCK)
public final class StartGameTranslator extends Translator<StartGamePacket> {
    @Override
    public Class<StartGamePacket> getPacketClass() {
        return StartGamePacket.class;
    }

    @Override
    public void translate(StartGamePacket packet) {
        // Initial boilerplate start game code.
        var client = MinecraftClient.getInstance();
        var registryManager = GameConstants.REGISTRY.get();
        var entityId = (int) packet.getRuntimeEntityId();
        var hashSeed = packet.getSeed();

        // Set player's gamemode.
        var gameMode = ConversionUtils.convertBedrockGameMode(packet.getPlayerGameType());

        // Register available dimensions.
        var hardcore = false;
        var dimensionIds = new HashSet<>(List.of(
                World.NETHER, World.OVERWORLD, World.END));

        // Get the current dimension.
        var dimensionType = ConversionUtils.convertBedrockDimension(packet.getDimensionId());
        var dimensionId = ConversionUtils.convertBedrockWorld(packet.getDimensionId());

        // Do game properties.
        var options = client.options;
        int
                maxPlayers = 999,
                chunkLoadDistance = options.getViewDistance().getValue();
        boolean reducedDebugInfo = false, showDeathScreen = true,
                debugWorld = false, flatWorld = false;

        for (var gamerule : packet.getGamerules()) {
            switch (gamerule.getName()) {
                case "doimmediaterespawn" ->
                        showDeathScreen = !((Boolean) gamerule.getValue());
            }
        }

        // Set initial view distance.
        this.data().setViewDistance(chunkLoadDistance);
        // Set movement mode.
        this.data().setMovementMode(
                packet.getAuthoritativeMovementMode());

        // Connect on the local network.
        var gameJoinPacket = new GameJoinS2CPacket(
                entityId, hardcore, gameMode, gameMode,
                dimensionIds, registryManager, dimensionType,
                dimensionId, hashSeed, maxPlayers, chunkLoadDistance, chunkLoadDistance,
                reducedDebugInfo, showDeathScreen, debugWorld, flatWorld,
                Optional.empty(), 0
        );
        this.javaClient().processPacket(gameJoinPacket);

        // Set the spawn position.
        this.javaClient().processPacket(new PlayerSpawnPositionS2CPacket(
                GameUtils.toBlockPos(packet.getDefaultSpawn()), 0f
        ));

        // Initialize the Bedrock client.
        this.bedrockClient.onPlayerInitialization();

        // --- POST INITIALIZATION --- \\

        // Set tags.
        // this.javaClient().processPacket(new SynchronizeTagsS2CPacket());

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

        // Perform client setup.
        var codecHelper = this.bedrockClient.getSession()
                .getPeer().getCodecHelper();

        // Set up the item registry.
        SimpleDefinitionRegistry.Builder<ItemDefinition> itemRegistry
                = SimpleDefinitionRegistry.builder();
        BiMap<Integer, String> itemRuntimeIds = HashBiMap.create();
        for (var definition : packet.getItemDefinitions()) {
            if (itemRuntimeIds.put(definition.getRuntimeId(),
                    definition.getIdentifier()) == null)
                itemRegistry.add(definition);
            else
                this.logger.warn("Duplicate item entry {}.",
                        definition.getIdentifier());
        }
        codecHelper.setItemDefinitions(itemRegistry.build());
        // Set the item runtime IDs.
        this.data().getId2Runtime().clear();
        this.data().getId2Runtime().putAll(itemRuntimeIds.inverse());

        // Set up the block registry.
        codecHelper.setBlockDefinitions(GameConstants.BLOCKS.get());
    }
}

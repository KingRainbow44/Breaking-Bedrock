package lol.magix.breakingbedrock.objects.absolute;

/**
 * Paths to resource files.
 */
public interface Resources {
    // GeyserMC mappings. (GeyserMC/mappings)
    String BLOCKS_MAPPINGS = "mappings/blocks.json";
    String ITEM_MAPPINGS = "mappings/items.json";
    // GeyserMC bindings. (GeyserMC/Geyser)
    String BLOCK_PALETTE = "bindings/block_palette.nbt";
    // TunnelMC mappings. (Flonja/TunnelMC)
    String ITEM_STATES = "mappings/runtime_item_states.json";
    // Prismarine mappings. (PrismarineJS/minecraft-data)
    String BEDROCK_BLOCKS = "mappings/blocks/blocksB2J.json";
    String JAVA_BLOCKS = "mappings/blocks/blocksJ2B.json";
    // PocketMine-MP mappings. (pmmp/BedrockData)
    String LEGACY_BLOCK_IDS = "mappings/block_id_map.json";
    String LEGACY_BLOCK_DATA = "mappings/block_state_meta_map.json";
    // Self-made mappings/bindings.
    String REGISTRY = "bindings/registry.nbt";
    String LEGACY_JAVA = "mappings/legacy2java.json";
}

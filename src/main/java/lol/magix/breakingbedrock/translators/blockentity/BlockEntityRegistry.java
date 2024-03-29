package lol.magix.breakingbedrock.translators.blockentity;

import lol.magix.breakingbedrock.translators.blockentity.defaults.ChestBlockEntityTranslator;
import lol.magix.breakingbedrock.translators.blockentity.defaults.GenericBlockEntityTranslator;
import lol.magix.breakingbedrock.translators.blockentity.defaults.SignBlockEntityTranslator;
import net.minecraft.block.entity.BlockEntityType;
import org.cloudburstmc.nbt.NbtMap;

import java.util.HashMap;
import java.util.Map;

public interface BlockEntityRegistry {
    Map<String, BlockEntityTranslator> TRANSLATORS = new HashMap<>();

    /**
     * Fetches a block entity translator based on the Bedrock identifier.
     *
     * @param identifier The Bedrock identifier. Serialized in NBT.
     * @return The block entity translator.
     */
    static BlockEntityTranslator getTranslator(NbtMap identifier) {
        return TRANSLATORS.get(identifier.getString("id"));
    }

    /**
     * Registers all block entity translators.
     */
    static void loadRegistry() {
        TRANSLATORS.put("Sign", new SignBlockEntityTranslator());
        TRANSLATORS.put("Chest", new ChestBlockEntityTranslator());
        TRANSLATORS.put("EnderChest", new GenericBlockEntityTranslator(
                BlockEntityType.ENDER_CHEST));

        // These are block entity translators with unhandled logic.
        TRANSLATORS.put("Lectern", new GenericBlockEntityTranslator(BlockEntityType.LECTERN));
        TRANSLATORS.put("Bell", new GenericBlockEntityTranslator(BlockEntityType.BELL));
        TRANSLATORS.put("Barrel", new GenericBlockEntityTranslator(BlockEntityType.BARREL));
    }
}

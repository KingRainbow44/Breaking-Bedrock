package lol.magix.breakingbedrock.translators;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.absolute.Resources;
import lol.magix.breakingbedrock.objects.binary.NbtMapOps;
import lol.magix.breakingbedrock.translators.blockstate.BlockStateTranslator;
import lol.magix.breakingbedrock.utils.ResourceUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ItemTranslator {
    private static final BiMap<String, Item> bedrock2Java = HashBiMap.create();
    private static final List<Item> blockedItems = new ArrayList<>() {{
        this.add(Items.SPECTRAL_ARROW);
        this.add(Items.DEBUG_STICK);
        this.add(Items.KNOWLEDGE_BOOK);
        this.add(Items.TIPPED_ARROW);
        this.add(Items.BUNDLE);
        this.add(Items.FURNACE_MINECART);
    }};

    /**
     * Loads all item mappings.
     */
    public static void loadMappings() {
        // Load mappings.
        var items = ResourceUtils.getResourceAsObject(
                Resources.ITEM_MAPPINGS, JsonObject.class);
        var states = ResourceUtils.getResourceAsObject(
                Resources.ITEM_STATES, JsonArray.class);
        if (items == null || states == null) return;

        // Add item states into item mappings.
        var id2Int = new HashMap<String, Integer>();
        for (var entry : states.asList()) {
            var object = entry.getAsJsonObject();
            id2Int.put(
                    object.get("name").getAsString(),
                    object.get("id").getAsInt());
        }

        // Load all item mappings.
        for (var entry : items.entrySet()) {
            var javaIdStr = entry.getKey();
            var javaId = new Identifier(javaIdStr);
            var item = Registries.ITEM.get(javaId);

            // Skip registering blocked items.
            if (blockedItems.contains(item)) continue;

            // Parse the Bedrock item data.
            var itemData = entry.getValue().getAsJsonObject();
            var bedrockId = itemData.get("bedrock_identifier").getAsString();
            var bedrockData = itemData.get("bedrock_data").getAsInt();

            if (item == Items.AIR && !javaIdStr.equals("minecraft:air")) {
                BreakingBedrock.getLogger().warn("Skipping unknown Bedrock item {}.", javaIdStr);
                continue;
            }

            bedrock2Java.put(bedrockId + ":" + bedrockData, item);
        }
    }

    /**
     * Converts a Bedrock item data to a Java item stack.
     *
     * @param data The Bedrock item data.
     * @return The Java item stack.
     */
    public static ItemStack bedrock2Java(ItemData data) {
        var definition = data.getDefinition();

        // noinspection ConstantValue
        if (definition == null) return ItemStack.EMPTY;

        var item = bedrock2Java.get(
                definition.getIdentifier() + ":" + data.getDamage());
        if (item == null) {
            item = bedrock2Java.get(definition.getIdentifier() + ":0");
            if (item == null) return ItemStack.EMPTY;
        }

        // Create the item stack.
        var stack = new ItemStack(item);
        stack.setCount(data.getCount());

        if (stack.isDamageable())
            stack.setDamage(data.getDamage());

        var nbtTags = data.getTag();
        if (nbtTags != null) {
            stack.setNbt(bedrock2Java(nbtTags));
            stack.setDamage(nbtTags.getInt("Damage", data.getDamage()));
            stack.setCustomName(Text.literal(nbtTags.getCompound("display")
                    .getString("Name", null)));

            // Convert enchantments.
            var enchantments = nbtTags.getList("ench", NbtType.COMPOUND, null);
            if (enchantments != null) for (var enchantment : enchantments) {
                var enchantmentId = enchantment.getShort("id");
                var enchantmentLevel = enchantment.getShort("lvl");

                // Convert the enchantment.
                var javaEnchantment = EnchantmentTranslator.bedrock2Java
                        .get((int) enchantmentId);
                if (javaEnchantment == null) {
                    BreakingBedrock.getLogger().warn("Enchantment {} (lvl {}) was not found.",
                            enchantmentId, enchantmentLevel);
                    continue;
                }

                stack.addEnchantment(javaEnchantment, enchantmentLevel);
            }
        }

        return stack;
    }

    /**
     * Converts a Java item stack to a Bedrock item data.
     *
     * @param stack The Java item stack.
     * @return The Bedrock item data.
     */
    public static ItemData java2Bedrock(ItemStack stack) {
        var item = stack.getItem();

        if (!bedrock2Java.containsValue(item)) {
            BreakingBedrock.getLogger().warn("Item {} not mapped.",
                    item.getName().getString());
            return ItemData.AIR;
        }

        // Get the Bedrock item data.
        var idPair = bedrock2Java.inverse().get(item).split(":");
        var blockRuntimeId = BlockStateTranslator.getJava2Runtime()
                .get(Block.getBlockFromItem(item).getDefaultState());

        var client = BedrockNetworkClient.getInstance();
        if (!client.isConnected()) {
            BreakingBedrock.getLogger().warn("Client is not connected.");
            return ItemData.AIR;
        }

        // Fetch the item's runtime ID.
        var runtimeId = client.getData().getId2Runtime()
                .get(idPair[0] + ":" + idPair[1]);

        // Get the client defined registries.
        var codec = client.getSession().getPeer().getCodecHelper();
        var itemRegistry = codec.getItemDefinitions();
        var blockRegistry = codec.getBlockDefinitions();

        return ItemData.builder()
                .definition(itemRegistry.getDefinition(runtimeId))
                .blockDefinition(blockRegistry.getDefinition(blockRuntimeId))
                .damage(Integer.parseInt(idPair[2]))
                .count(stack.getCount())
                .tag(java2Bedrock(stack.getNbt()))
                .build();
    }

    /**
     * Converts a Bedrock NBT tag to a Java NBT tag.
     *
     * @param root The Bedrock NBT tag.
     * @return The Java NBT tag.
     */
    private static NbtCompound bedrock2Java(NbtMap root) {
        if (root == null) return null;
        return (NbtCompound) NbtMapOps.INSTANCE.convertTo(NbtOps.INSTANCE, root);
    }

    /**
     * Converts a Java NBT tag to a Bedrock NBT tag.
     *
     * @param root The Java NBT tag.
     * @return The Bedrock NBT tag.
     */
    private static NbtMap java2Bedrock(NbtCompound root) {
        if (root == null) return null;
        return (NbtMap) NbtOps.INSTANCE.convertTo(NbtMapOps.INSTANCE, root);
    }
}

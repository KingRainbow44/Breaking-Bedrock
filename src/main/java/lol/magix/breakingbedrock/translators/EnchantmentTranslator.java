package lol.magix.breakingbedrock.translators;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

import java.util.HashMap;
import java.util.Map;

public interface EnchantmentTranslator {
    Map<Integer, Enchantment> bedrock2Java = new HashMap<>() {{
        this.put(0, Enchantments.PROTECTION);
        this.put(1, Enchantments.FIRE_PROTECTION);
        this.put(2, Enchantments.FEATHER_FALLING);
        this.put(3, Enchantments.BLAST_PROTECTION);
        this.put(4, Enchantments.PROJECTILE_PROTECTION);
        this.put(5, Enchantments.THORNS);
        this.put(6, Enchantments.RESPIRATION);
        this.put(7, Enchantments.DEPTH_STRIDER);
        this.put(8, Enchantments.AQUA_AFFINITY);
        this.put(9, Enchantments.SHARPNESS);
        this.put(10, Enchantments.SMITE);
        this.put(11, Enchantments.BANE_OF_ARTHROPODS);
        this.put(12, Enchantments.KNOCKBACK);
        this.put(13, Enchantments.FIRE_ASPECT);
        this.put(14, Enchantments.LOOTING);
        this.put(15, Enchantments.EFFICIENCY);
        this.put(16, Enchantments.SILK_TOUCH);
        this.put(17, Enchantments.UNBREAKING);
        this.put(18, Enchantments.FORTUNE);
        this.put(19, Enchantments.POWER);
        this.put(20, Enchantments.PUNCH);
        this.put(21, Enchantments.FLAME);
        this.put(22, Enchantments.INFINITY);
        this.put(23, Enchantments.LUCK_OF_THE_SEA);
        this.put(24, Enchantments.LURE);
        this.put(25, Enchantments.FROST_WALKER);
        this.put(26, Enchantments.MENDING);
        this.put(27, Enchantments.BINDING_CURSE);
        this.put(28, Enchantments.VANISHING_CURSE);
        this.put(29, Enchantments.IMPALING);
        this.put(30, Enchantments.RIPTIDE);
        this.put(31, Enchantments.LOYALTY);
        this.put(32, Enchantments.CHANNELING);
        this.put(33, Enchantments.MULTISHOT);
        this.put(34, Enchantments.PIERCING);
        this.put(35, Enchantments.QUICK_CHARGE);
        this.put(36, Enchantments.SOUL_SPEED);
    }};
}

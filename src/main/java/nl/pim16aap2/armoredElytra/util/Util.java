package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class Util {
    public static String errorToString(Error e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // Check if an item is broken or not.
    public static boolean isBroken(ItemStack item) {
        return item.getDurability() >= item.getType().getMaxDurability();
    }

    // Get the armor tier from a chest plate.
    public static ArmorTier armorToTier(Material mat) {
        ArmorTier ret = ArmorTier.NONE;
        XMaterial xmat = XMaterial.matchXMaterial(mat);

        switch (xmat) {
            case LEATHER_CHESTPLATE:
                ret = ArmorTier.LEATHER;
                break;
            case GOLDEN_CHESTPLATE:
                ret = ArmorTier.GOLD;
                break;
            case CHAINMAIL_CHESTPLATE:
                ret = ArmorTier.CHAIN;
                break;
            case IRON_CHESTPLATE:
                ret = ArmorTier.IRON;
                break;
            case DIAMOND_CHESTPLATE:
                ret = ArmorTier.DIAMOND;
                break;
            case NETHERITE_CHESTPLATE:
                ret = ArmorTier.NETHERITE;
                break;
            default:
                break;
        }
        return ret;
    }

    public static boolean isChestPlate(ItemStack itemStack) {
        return isChestPlate(itemStack.getType());
    }

    // Check if mat is a chest plate.
    public static boolean isChestPlate(Material mat) {
        try {
            XMaterial xmat = XMaterial.matchXMaterial(mat);

            return xmat == XMaterial.LEATHER_CHESTPLATE || xmat == XMaterial.GOLDEN_CHESTPLATE ||
                    xmat == XMaterial.CHAINMAIL_CHESTPLATE || xmat == XMaterial.IRON_CHESTPLATE ||
                    xmat == XMaterial.DIAMOND_CHESTPLATE || xmat == XMaterial.NETHERITE_CHESTPLATE;
        } catch (IllegalArgumentException e) {
            // No need to handle this, this is just XMaterial complaining the material doesn't exist.
            return false;
        }
    }

    // Function that returns which/how many protection enchantments there are.
    public static int getProtectionEnchantmentsVal(Map<Enchantment, Integer> enchantments) {
        int ret = 0;
        if (enchantments.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL))
            ret += 1;
        if (enchantments.containsKey(Enchantment.PROTECTION_EXPLOSIONS))
            ret += 2;
        if (enchantments.containsKey(Enchantment.PROTECTION_FALL))
            ret += 4;
        if (enchantments.containsKey(Enchantment.PROTECTION_FIRE))
            ret += 8;
        if (enchantments.containsKey(Enchantment.PROTECTION_PROJECTILE))
            ret += 16;
        return ret;
    }
}

package nl.pim16aap2.armoredElytra.nms;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_13_R2.NBTTagByte;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagInt;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NBTTagString;
import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class NBTEditor_V1_13_R2 implements NBTEditor
{
    private ArmoredElytra plugin;

    // Get the names and lores for every tier of armor.
    public NBTEditor_V1_13_R2(ArmoredElytra plugin)
    {
        this.plugin = plugin;
    }

    // Add armor to the supplied item, based on the armorTier.
    @Override
    public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        ItemMeta itemmeta   = item.getItemMeta();
        int armorProtection = ArmorTier.getArmor(armorTier);
        int armorToughness  = ArmorTier.getToughness(armorTier);

        itemmeta.setDisplayName(plugin.getArmoredElytrName(armorTier));
        if (plugin.getElytraLore() != null)
            itemmeta.setLore(Arrays.asList(plugin.fillInArmorTierInStringNoColor(plugin.getElytraLore(), armorTier)));
        item.setItemMeta(itemmeta);

        net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound   =    (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList modifiers      =     new NBTTagList();
        NBTTagCompound armor      =     new NBTTagCompound();
        armor.set("AttributeName",      new NBTTagString("generic.armor"));
        armor.set("Name",               new NBTTagString("generic.armor"));
        armor.set("Amount",             new NBTTagInt(armorProtection));
        armor.set("Operation",          new NBTTagInt(0));
        armor.set("UUIDLeast",          new NBTTagInt(894654));
        armor.set("UUIDMost",           new NBTTagInt(2872));
        armor.set("Slot",               new NBTTagString("chest"));
        modifiers.add(armor);

        NBTTagCompound armorTough =     new NBTTagCompound();
        armorTough.set("AttributeName", new NBTTagString("generic.armorToughness"));
        armorTough.set("Name",          new NBTTagString("generic.armorToughness"));
        armorTough.set("Amount",        new NBTTagInt(armorToughness));
        armorTough.set("Operation",     new NBTTagInt(0));
        armorTough.set("UUIDLeast",     new NBTTagInt(894654));
        armorTough.set("UUIDMost",      new NBTTagInt(2872));
        armorTough.set("Slot",          new NBTTagString("chest"));
        modifiers.add(armorTough);

        if (unbreakable)
            compound.set("Unbreakable", new NBTTagByte((byte) 1));

        compound.set("AttributeModifiers", modifiers);
        item = CraftItemStack.asBukkitCopy(nmsStack);
        return item;
    }

    // Get the armor tier of the supplied item.
    @Override
    public ArmorTier getArmorTier(ItemStack item)
    {
        if (item == null)
            return ArmorTier.NONE;
        if (item.getType() != Material.ELYTRA)
            return ArmorTier.NONE;

        // Get the NBT tags from the item.
        NBTTagCompound compound = CraftItemStack.asNMSCopy(item).getTag();
        if (compound == null)
            return ArmorTier.NONE;
        String nbtTags = compound.toString();

        // Check if the item has the generic.armor attribute.
        // Format = <level>,Slot:"chest",AttributeName:"generic.armor so get pos of char before 
        // The start of the string, as that's the value of the generic.armor attribute.
        // The level is now formatted as x.xd, e.g. 6.0d.
        int pos = nbtTags.indexOf(",Slot:\"chest\",AttributeName:\"generic.armor\"");
        int armorValue = 0;
        
        if (pos <= 0)
            return ArmorTier.NONE;
        
        try 
        {
            String stringAtPos = nbtTags.substring(pos - 4, pos - 1);
            armorValue = (int) Double.parseDouble(stringAtPos);
        }
        catch (Exception e) 
        {
            armorValue = 0;
        }
        
        switch (armorValue)
        {
        case 3:
            return ArmorTier.LEATHER;
        case 5:
            return ArmorTier.GOLD;
        case 6:
            return ArmorTier.IRON;
        case 8:
            return ArmorTier.DIAMOND;
        default:
            return ArmorTier.NONE;
        }
    }
}
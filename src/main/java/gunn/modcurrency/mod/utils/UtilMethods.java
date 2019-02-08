package gunn.modcurrency.mod.utils;

import gunn.modcurrency.mod.ModConfig;
import net.minecraft.item.ItemStack;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-06-12
 */
public final class UtilMethods {
    public static boolean equalStacks(ItemStack one, ItemStack two) {
        boolean basic = one.getItem().equals(two.getItem()) && (one.getItemDamage() == two.getItemDamage());
        boolean ench = false;
        if (one.hasTagCompound() && two.hasTagCompound()) {
            ench = (one.getTagCompound().equals(two.getTagCompound()));
        } else if (!one.hasTagCompound() && one.hasTagCompound() == false) {
            ench = true;
        }
        return basic && ench;
    }

    public static String translateMoney(long amount) {
        double dollar = (double)amount * ModConfig.coinDollarScale;
        return String.format("%.2f", dollar);
    }

    public static int getBillWorth(int itemDamage, int stackSize) {
        int cash = 0;
        if (0 <= itemDamage && itemDamage <= 5) {
            cash = ModConfig.billValueList.get(itemDamage);
        }
        return cash * stackSize;
    }

    public static int getCoinWorth(int itemDamage, int stackSize) {
        int cash = 0;
        if (0 <= itemDamage && itemDamage <= 5) {
            cash = ModConfig.coinValueList.get(itemDamage);
        }
        return cash * stackSize;
    }
}

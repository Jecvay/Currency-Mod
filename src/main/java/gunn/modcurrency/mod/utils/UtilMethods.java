package gunn.modcurrency.mod.utils;

import net.minecraft.item.ItemStack;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-06-12
 */
public final class UtilMethods {
   public static boolean equalStacks(ItemStack one, ItemStack two){
        boolean basic = one.getItem().equals(two.getItem()) && (one.getItemDamage() == two.getItemDamage());
        boolean ench = false;
        if(one.hasTagCompound() && two.hasTagCompound()) {
            ench = (one.getTagCompound().equals(two.getTagCompound()));
        }else if(one.hasTagCompound() == false && one.hasTagCompound() == false) ench = true;
        return basic && ench;
    }

    public static String translateMoney(long amount){
       String amnt = Long.toString(amount);
       String finalTranslation;


        if(amnt.length() == 1) {
            finalTranslation = "0.0" + amnt;
        }else if(amnt.length() >2){   //At least one dollar
            finalTranslation = amnt.substring(0, amnt.length()-2) + "." + amnt.substring(amnt.length()-2, amnt.length());
        }else if(amnt.equals(0)) {
            finalTranslation = amnt;
        }else{
            finalTranslation = "0." + amnt;
        }

        return finalTranslation;
    }

    public static int getBillWorth(int itemDamage, int stackSize) {
        int cash = 0;
        switch (itemDamage) {
            case 0:
                cash = 100;
                break;
            case 1:
                cash = 500;
                break;
            case 2:
                cash = 1000;
                break;
            case 3:
                cash = 2000;
                break;
            case 4:
                cash = 5000;
                break;
            case 5:
                cash = 10000;
                break;
        }

        return cash * stackSize;
    }

    public static int getCoinWorth(int itemDamage, int stackSize) {
        int cash = 0;
        switch (itemDamage) {
            case 0:
                cash = 1;
                break;
            case 1:
                cash = 5;
                break;
            case 2:
                cash = 10;
                break;
            case 3:
                cash = 25;
                break;
            case 4:
                cash = 100;
                break;
            case 5:
                cash = 200;
                break;
        }

        return cash * stackSize;
    }
}

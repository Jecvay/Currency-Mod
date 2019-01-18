package gunn.modcurrency.mod.item;

import gunn.modcurrency.mod.ModConfig;
import gunn.modcurrency.mod.ModCurrency;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import gunn.modcurrency.mod.utils.UtilMethods;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-01-16
 */
public class ItemWallet extends Item{
    public static final int walletLength = 6;
    public static final int WALLET_COLUMN_COUNT = 9;
    public static final int WALLET_ROW_COUNT = ModConfig.walletSize;
    public static final int WALLET_TOTAL_COUNT = WALLET_COLUMN_COUNT * WALLET_ROW_COUNT;

    public ItemWallet(){
        setRegistryName("wallet");
        setCreativeTab(ModCurrency.tabCurrency);
        setUnlocalizedName(getRegistryName().toString());
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0; i < walletLength; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getRegistryName() + "_" + i, "inventory"));
        }
    }

    public void openGui(EntityPlayer player, World world, BlockPos pos) {
       player.openGui(ModCurrency.instance, 32, world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World itemStackIn, EntityPlayer worldIn, EnumHand playerIn) {
        openGui(worldIn, itemStackIn, worldIn.getPosition());
        return super.onItemRightClick(itemStackIn, worldIn, playerIn);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!worldIn.isRemote && stack.hasTagCompound()){
            NBTTagCompound compound = stack.getTagCompound();
            ItemStackHandler itemStackHandler = new ItemStackHandler(ItemWallet.WALLET_TOTAL_COUNT);
            itemStackHandler.deserializeNBT(compound.getCompoundTag("inventory"));
            if(compound.hasKey("total")){
                if(compound.getInteger("total") != getTotalCash(itemStackHandler)){
                    compound.setInteger("total", getTotalCash(itemStackHandler));
                    stack.setTagCompound(compound);
                }
            }else{
                compound.setInteger("total", getTotalCash(itemStackHandler));
                stack.setTagCompound(compound);
            }
        }
    }

    private int getTotalCash(ItemStackHandler itemStackHandler) {
        int totalCash = 0;
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            ItemStack itemStack = itemStackHandler.getStackInSlot(i);
            Item item = itemStack.getItem();
            if (item.equals(ModItems.itemCoin)) {
                int itemDamage = itemStack.getItemDamage();
                totalCash += UtilMethods.getCoinWorth(
                        itemDamage, itemStack.getCount());
            } else if (item.equals(ModItems.itemBanknote)) {
                int itemDamage = itemStack.getItemDamage();
                totalCash += UtilMethods.getBillWorth(
                        itemDamage, itemStack.getCount());
            }
        }
        return totalCash;
    }
}

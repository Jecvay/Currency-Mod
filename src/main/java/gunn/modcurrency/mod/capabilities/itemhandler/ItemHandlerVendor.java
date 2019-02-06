package gunn.modcurrency.mod.capabilities.itemhandler;

import gunn.modcurrency.mod.network.PacketHandler;
import gunn.modcurrency.mod.network.PacketUpdateSizeToClient;
import gunn.modcurrency.mod.tileentity.TileVending;
import gunn.modcurrency.mod.utils.UtilMethods;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-02-25
 */
public class ItemHandlerVendor extends ItemStackHandler {
    TileVending tile;

    public ItemHandlerVendor(int size, TileVending tile) {
        super(size);
        this.tile = tile;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (UtilMethods.equalStacks(this.getStackInSlot(slot), stack)) {
            //if can fit
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            validateSlotIndex(slot);
            ItemStack existing = this.stacks.get(slot);

            int limit = 1000;

            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate) {
                if (existing.isEmpty()) {
                    this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                } else if (tile.getItemSize(slot) < tile.getStackLimit()){
                    tile.growItemSize(stack.getCount(), slot);
                    if (tile.getPlayerUsing() != null) {
                        PacketUpdateSizeToClient pack = new PacketUpdateSizeToClient();
                        pack.setData(tile.getPos(), slot, tile.getItemSize(slot));
                        PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) tile.getPlayerUsing());
                    }
                }
                onContentsChanged(slot);
            }

            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
        }
        return stack;
    }
}
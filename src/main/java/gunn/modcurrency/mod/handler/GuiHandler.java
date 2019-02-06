package gunn.modcurrency.mod.handler;

import gunn.modcurrency.mod.client.gui.*;
import gunn.modcurrency.mod.container.ContainerATM;
import gunn.modcurrency.mod.container.ContainerExchanger;
import gunn.modcurrency.mod.container.ContainerVending;
import gunn.modcurrency.mod.container.ContainerWallet;
import gunn.modcurrency.mod.item.ModItems;
import gunn.modcurrency.mod.tileentity.TileATM;
import gunn.modcurrency.mod.tileentity.TileExchanger;
import gunn.modcurrency.mod.tileentity.TileHydrometer;
import gunn.modcurrency.mod.tileentity.TileVending;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Distributed with the Currency-Mod for Minecraft.
 * Copyright (C) 2016  Brady Gunn
 *
 * File Created on 2016-11-02.
 */
public class GuiHandler implements IGuiHandler{
    //Id 30 = BlockVending
    //Id 31 = BlockSeller
    //Id 32 = Wallet
    //Id 33 = ATM
    //ID 34 = Guide Book


    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos xyz = new BlockPos(x,y,z);
        TileEntity tileEntity = world.getTileEntity(xyz);

        if(tileEntity instanceof TileVending && ID == 30){
            TileVending tilevendor = (TileVending) tileEntity;
            return new ContainerVending(player, tilevendor);
        }

        if(tileEntity instanceof TileExchanger && ID == 31){
            TileExchanger tileExchanger = (TileExchanger) tileEntity;
            return new ContainerExchanger(player.inventory, tileExchanger);
        }

        if(ID == 32 && player.getHeldItemMainhand().getItem().equals(ModItems.itemWallet)){
            return new ContainerWallet(player, player.getHeldItemMainhand());
        }

        if(tileEntity instanceof TileATM && ID == 33){
            TileATM tileATM = (TileATM) tileEntity;
            return new ContainerATM(player, tileATM);
        }

        if(tileEntity instanceof TileHydrometer && ID == 35){
            TileHydrometer tileHydrometer = (TileHydrometer) tileEntity;
            return new ContainerHydroMeter(player, tileHydrometer);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos xyz = new BlockPos(x,y,z);
        TileEntity tileEntity = world.getTileEntity(xyz);

        if(tileEntity instanceof TileVending && ID == 30){
            TileVending tilevendor = (TileVending) tileEntity;
            return new GuiVending(player, tilevendor);
        }

        if(tileEntity instanceof TileExchanger && ID == 31){
            TileExchanger tileSeller = (TileExchanger) tileEntity;
            return new GuiExchanger(player, tileSeller);
        }

        if(ID == 32 && player.getHeldItemMainhand().getItem().equals(ModItems.itemWallet)){
            return new GuiWallet(player, player.getHeldItemMainhand());
        }

        if(tileEntity instanceof TileATM && ID == 33){
            TileATM tileATM = (TileATM) tileEntity;
            return new GuiATM(player, tileATM);
        }

        if(ID == 34 && player.inventory.getCurrentItem().getItem() == ModItems.itemGuide){
            return new GuiGuide(player.inventory.getCurrentItem());
        }

        if(tileEntity instanceof TileHydrometer && ID == 35){
            TileHydrometer tileHydrometer = (TileHydrometer) tileEntity;
            return new GuiHydrometer(player, tileHydrometer);
        }

        return null;
    }
}

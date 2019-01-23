package gunn.modcurrency.mod.block;

import gunn.modcurrency.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Distributed with the Currency-Mod for Minecraft.
 * Copyright (C) 2016  Brady Gunn
 *
 * File Created on 2016-10-30.
 */
public class ModBlocks {
    public static BlockVending blockVending = new BlockVending();
    public static BlockExchanger blockExchanger = new BlockExchanger();
    public static BlockHydrometer blockHydrometer = new BlockHydrometer();
    //public static BlockATM blockATM = new BlockATM();

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        if(ModConfig.enableSeller) event.getRegistry().register(blockExchanger);
        if(ModConfig.enableVendor) event.getRegistry().register(blockVending);
        event.getRegistry().register(blockHydrometer);
        //if(ModConfig.enableATM) event.getRegistry().register(blockATM);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event){
        if(ModConfig.enableSeller) event.getRegistry().register(new ItemBlock(blockExchanger).setRegistryName(blockExchanger.getRegistryName()));
        if(ModConfig.enableVendor) event.getRegistry().register(new ItemBlock(blockVending).setRegistryName(blockVending.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockHydrometer).setRegistryName(blockHydrometer.getRegistryName()));
       // if(ModConfig.enableATM) event.getRegistry().register(new ItemBlock(blockATM).setRegistryName(blockATM.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event){
        if(ModConfig.enableSeller) blockExchanger.initModel();
        if(ModConfig.enableVendor) blockVending.initModel();
        blockHydrometer.initModel();
        //if(ModConfig.enableATM) blockATM.initModel();
    }

}

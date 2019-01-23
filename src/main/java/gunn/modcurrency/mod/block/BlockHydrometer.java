package gunn.modcurrency.mod.block;

import gunn.modcurrency.mod.ModCurrency;
import gunn.modcurrency.mod.handler.StateHandler;
import gunn.modcurrency.mod.tileentity.TileATM;
import gunn.modcurrency.mod.tileentity.TileHydrometer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class was created by BeardlessBrady. It is distributed as
 * part of The Currency-Mod. Source Code located on github:
 * https://github.com/BeardlessBrady/Currency-Mod
 * -
 * Copyright (C) All Rights Reserved
 * File Created 2019-01-22
 */

public class BlockHydrometer extends Block implements ITileEntityProvider {
    public BlockHydrometer() {
        super(Material.ROCK);
        setRegistryName("blockhydrometer");
        setUnlocalizedName(this.getRegistryName().toString());

        setHardness(3.0F);
        setCreativeTab(ModCurrency.tabCurrency);
        setSoundType(SoundType.METAL);

        GameRegistry.registerTileEntity(TileHydrometer.class, ModCurrency.MODID + "_tehydrometer");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileHydrometer();
    }

    private TileHydrometer getTile(World world, BlockPos pos) {
        return (TileHydrometer) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (getTile(world, pos).getPlayerUsing() == null && UUID.fromString(getTile(world, pos).getOwner()).equals(UUID.fromString(player.getUniqueID().toString()))) {
                System.out.println("DDE");
                getTile(world, pos).openGui(player, world, pos);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(placer instanceof EntityPlayer) getTile(worldIn, pos).setOwner((placer).getUniqueID().toString());
    }

    void initModel() {
       // ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}

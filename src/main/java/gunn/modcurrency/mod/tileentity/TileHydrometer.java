package gunn.modcurrency.mod.tileentity;

import gunn.modcurrency.mod.ModCurrency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This class was created by BeardlessBrady. It is distributed as
 * part of The Currency-Mod. Source Code located on github:
 * https://github.com/BeardlessBrady/Currency-Mod
 * -
 * Copyright (C) All Rights Reserved
 * File Created 2019-01-22
 */

public class TileHydrometer extends TileEntity implements IOwnable {
    private EntityPlayer playerUsing = null;
    private String owner;
    boolean isOwner;

    public TileHydrometer(){
        owner = "";
        isOwner = true;
    }

    public void openGui(EntityPlayer player, World world, BlockPos pos) {
        player.openGui(ModCurrency.instance, 35, world, pos.getX(), pos.getY(), pos.getZ());
        playerUsing = player;
    }

    //<editor-fold desc="NBT & Packet Stoof--------------------------------------------------------------------------------------------------">
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("owner", owner);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("owner")) owner = compound.getString("owner");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("owner", owner);

        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        owner = pkt.getNbtCompound().getString("owner");
    }

    //</editor-fold>

    public int getFieldCount(){
        return 1;
    }

    public void setField(int id, int value){
        switch(id){
            case 0:
                isOwner = value == 1;
                break;
        }
    }

    public int getField(int id){
        switch(id){
            case 0:
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                markDirty();
                return (isOwner) ? 1 : 0;
        }
        return -1;
    }

    public EntityPlayer getPlayerUsing(){
        return playerUsing;
    }

    public void voidPlayerUsing(){
        playerUsing = null;
    }

    public void setOwner(String player){
        owner = player;
    }

    public String getOwner(){
        return owner;
    }
}

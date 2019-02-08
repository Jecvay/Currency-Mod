package gunn.modcurrency.mod.tileentity;

import gunn.modcurrency.mod.ModCurrency;
import gunn.modcurrency.mod.container.util.INBTInventory;
import gunn.modcurrency.mod.capabilities.itemhandler.ItemHandlerCustom;
import gunn.modcurrency.mod.handler.StateHandler;
import gunn.modcurrency.mod.item.ModItems;
import gunn.modcurrency.mod.network.PacketHandler;
import gunn.modcurrency.mod.network.PacketSetLongToClient;
import gunn.modcurrency.mod.network.PacketUpdateFufilledRequestToClient;
import gunn.modcurrency.mod.utils.UtilMethods;
import gunn.modcurrency.mod.ModConfig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-05-28
 */
public class TileExchanger extends TileEntity implements ICapabilityProvider, ITickable, INBTInventory, IOwnable{
    private static final int INPUT_SLOT_COUNT = 1;
    public static final int VEND_SLOT_COUNT = 30;
    public static final int BUFFER_SLOT_COUNT = 4;

    private long bank, cashRegister;
    private int selectedSlot;
    private String owner, selectedName;
    private boolean locked, mode, creative, infinite, gearExtended, twoBlock, voidBlock, upgradeReq;
    private int[] itemCosts = new int[VEND_SLOT_COUNT];
    private int[] itemAmounts = new int[VEND_SLOT_COUNT];
    private int[] bundleAmount= new int[VEND_SLOT_COUNT];
    private ItemStackHandler inputStackHandler = new ItemStackHandler(INPUT_SLOT_COUNT);
    private ItemStackHandler vendStackHandler = new ItemStackHandler(VEND_SLOT_COUNT);
    private ItemStackHandler bufferStackHandler = new ItemStackHandler(BUFFER_SLOT_COUNT);
    private ItemHandlerCustom automationInputStackHandler = new ItemHandlerCustom(1);
    private EntityPlayer playerUsing = null;

    public final byte FIELD_LOCKED = 0;
    public final byte FIELD_MODE = 1;
    public final byte FIELD_SELECTSLOT = 2;
    public final byte FIELD_CREATIVE = 3;
    public final byte FIELD_INFINITE = 4;
    public final byte FIELD_TWOBLOCK = 5;
    public final byte FIELD_GEAREXT = 6;
    public final byte FIELD_VOID = 7;
    public final byte FIELD_UPGRADEREQ = 8;

    public final byte LONG_BANK = 0;
    public final byte LONG_CASHREG = 1;

    public TileExchanger() {
        bank = 0;
        cashRegister = 0;
        selectedSlot = 37;
        owner = "";
        selectedName = "No Item";
        locked = false;
        mode = false;
        creative = false;
        infinite = false;
        voidBlock = true;
        gearExtended = false;
        twoBlock = false;
        automationInputStackHandler.setAllowedItem(ModItems.itemBanknote);
        automationInputStackHandler.setAllowedItem(ModItems.itemCoin);

        upgradeReq = false;

        for (int i = 0; i < itemCosts.length; i++){
            itemCosts[i] = 0;
            itemAmounts[i] = 0;
            bundleAmount[i] = 1;
        }
    }

    public void openGui(EntityPlayer player, World world, BlockPos pos) {
        if(world.getBlockState(pos).getValue(StateHandler.TWOTALL) == StateHandler.EnumTwoBlock.TWOTOP){
            player.openGui(ModCurrency.instance, 31, world, pos.getX(), pos.down().getY(), pos.getZ());
        }else player.openGui(ModCurrency.instance, 31, world, pos.getX(), pos.getY(), pos.getZ());
        playerUsing = player;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (playerUsing != null) {
                if (!automationInputStackHandler.getStackInSlot(0).isEmpty()) {

                    if (automationInputStackHandler.getStackInSlot(0).getItem().equals(ModItems.itemBanknote)) {
                        int amount;
                        int subId = automationInputStackHandler.getStackInSlot(0).getItemDamage();
                        if (0 <= subId && subId <= 5) {
                            amount = ModConfig.billValueList.get(subId);
                        } else {
                            amount = -1;
                        }
                        amount = amount * automationInputStackHandler.getStackInSlot(0).getCount();
                        automationInputStackHandler.setStackInSlot(0, ItemStack.EMPTY);
                        cashRegister = cashRegister + amount;

                        if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                            PacketSetLongToClient pack = new PacketSetLongToClient();
                            pack.setData(getPos(), LONG_CASHREG, cashRegister);
                            PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                        }
                    } else if (automationInputStackHandler.getStackInSlot(0).getItem().equals(ModItems.itemCoin)) {
                        int amount;
                        int subId = automationInputStackHandler.getStackInSlot(0).getItemDamage();
                        if (0 <= subId && subId <= 5) {
                            amount = ModConfig.coinValueList.get(subId);
                        } else {
                            amount = -1;
                        }
                        amount = amount * automationInputStackHandler.getStackInSlot(0).getCount();
                        automationInputStackHandler.setStackInSlot(0, ItemStack.EMPTY);
                        cashRegister = cashRegister + amount;

                        if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                            PacketSetLongToClient pack = new PacketSetLongToClient();
                            pack.setData(getPos(), LONG_CASHREG, cashRegister);
                            PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                        }
                    }
                }

                if (!mode) {        //SELL MODE
                    if (!inputStackHandler.getStackInSlot(0).isEmpty()) {
                        searchLoop:
                        for (int i = 0; i < vendStackHandler.getSlots(); i++) {
                            if (!vendStackHandler.getStackInSlot(i).isEmpty()) {
                                if (UtilMethods.equalStacks(inputStackHandler.getStackInSlot(0), vendStackHandler.getStackInSlot(i)) &&
                                        inputStackHandler.getStackInSlot(0).getItemDamage() == vendStackHandler.getStackInSlot(i).getItemDamage()) {
                                    int amount = getBundleAmnt(i);
                                    int cost = getItemCost(i);
                                    boolean isThereRoom = false;
                                    int buffSlot = 0;

                                    //checks if there are enough items in the input slot to sell as the bundled amount specified
                                    if(inputStackHandler.getStackInSlot(0).getCount() >= amount) {

                                        //Search buffer to see if it has an empty slot OR a slot has the sae block as what is sold
                                        bufferLoop:
                                        for (int j = 0; j < bufferStackHandler.getSlots(); j++) {
                                            if (!bufferStackHandler.getStackInSlot(j).isEmpty()) {
                                                if (UtilMethods.equalStacks(bufferStackHandler.getStackInSlot(j), inputStackHandler.getStackInSlot(0))
                                                        && (bufferStackHandler.getStackInSlot(j).getCount() < bufferStackHandler.getStackInSlot(j).getMaxStackSize() - amount + 1)) {
                                                    isThereRoom = true;
                                                    buffSlot = j;
                                                    break bufferLoop;
                                                }
                                            } else {
                                                isThereRoom = true;
                                                buffSlot = j;
                                                break bufferLoop;
                                            }
                                        }
                                        if ((cashRegister >= cost || infinite) && isThereRoom) {
                                            ItemStack inputItem = inputStackHandler.getStackInSlot(0);
                                            bank = bank + cost;

                                            if (!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null) {
                                                PacketSetLongToClient pack = new PacketSetLongToClient();
                                                pack.setData(getPos(), LONG_BANK, bank);
                                                PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                                            }

                                            if (!infinite) {
                                                cashRegister = cashRegister - cost;

                                                if (!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null) {
                                                    PacketSetLongToClient pack = new PacketSetLongToClient();
                                                    pack.setData(getPos(), LONG_CASHREG, cashRegister);
                                                    PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                                                }
                                            }

                                            if (voidBlock) {
                                                if (!bufferStackHandler.getStackInSlot(buffSlot).isEmpty())
                                                    bufferStackHandler.getStackInSlot(buffSlot).grow(amount);
                                                if (bufferStackHandler.getStackInSlot(buffSlot).isEmpty()) {
                                                    ItemStack newStack = inputItem.copy();
                                                    newStack.setCount(amount);
                                                    bufferStackHandler.setStackInSlot(buffSlot, newStack);
                                                }
                                            }
                                            inputItem.shrink(amount);
                                            if (itemAmounts[i] > amount) {
                                                vendStackHandler.getStackInSlot(i).shrink(amount);
                                                itemAmounts[i] -= amount;
                                            } else if (itemAmounts[i] == amount) {
                                                vendStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                                                itemAmounts[i] = -1;

                                                PacketUpdateFufilledRequestToClient pack = new PacketUpdateFufilledRequestToClient();
                                                pack.setData(getPos(), i, -1);
                                                PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                                            }

                                        }
                                        break searchLoop;
                                    }
                                }

                            }
                            if (inputStackHandler.getStackInSlot(0).getCount() == 0) {
                                inputStackHandler.setStackInSlot(0, ItemStack.EMPTY);
                                break searchLoop;
                            }
                        }
                    }
                } else {        //EDIT MODE
                    if (!inputStackHandler.getStackInSlot(0).isEmpty()) {
                        if (inputStackHandler.getStackInSlot(0).getItem().equals(ModItems.itemBanknote)) {
                            int amount;
                            int subId = inputStackHandler.getStackInSlot(0).getItemDamage();
                            if (0 <= subId && subId <= 5) {
                                amount = ModConfig.billValueList.get(subId);
                            } else {
                                amount = -1;
                            }
                            amount = amount * inputStackHandler.getStackInSlot(0).getCount();
                            inputStackHandler.setStackInSlot(0, ItemStack.EMPTY);
                            cashRegister = cashRegister + amount;

                            if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                                PacketSetLongToClient pack = new PacketSetLongToClient();
                                pack.setData(getPos(), LONG_CASHREG, cashRegister);
                                PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                            }
                        } else if (inputStackHandler.getStackInSlot(0).getItem().equals(ModItems.itemCoin)) {
                            int amount;
                            int subId = inputStackHandler.getStackInSlot(0).getItemDamage();
                            if (0 <= subId && subId <= 5) {
                                amount = ModConfig.coinValueList.get(subId);
                            } else {
                                amount = -1;
                            }
                            amount = amount * inputStackHandler.getStackInSlot(0).getCount();
                            inputStackHandler.setStackInSlot(0, ItemStack.EMPTY);
                            cashRegister = cashRegister + amount;

                            if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                                PacketSetLongToClient pack = new PacketSetLongToClient();
                                pack.setData(getPos(), LONG_CASHREG, cashRegister);
                                PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                            }
                        }
                    }
                }
            }
            markDirty();
        }
    }

    //Drop Items
    public void dropItems() {
        for (int i = 0; i < bufferStackHandler.getSlots(); i++){
            ItemStack item = bufferStackHandler.getStackInSlot(i);
            if (!item.isEmpty()) {
                world.spawnEntity(new EntityItem(world, getPos().getX(), getPos().getY(), getPos().getZ(), item));
                vendStackHandler.setStackInSlot(i, ItemStack.EMPTY);   //Just in case
            }
        }
        //Drop upgrades it has when broken
        if(upgradeReq) world.spawnEntity(new EntityItem(world, getPos().getX(), getPos().getY(), getPos().getZ(), new ItemStack(ModItems.itemUpgrade, 1, 1)));
    }

    public boolean canInteractWith(EntityPlayer player) {
        return !isInvalid() && player.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    //<editor-fold desc="Money Methods-------------------------------------------------------------------------------------------------------">

    public boolean canAfford(int slot){
        return bank >= itemCosts[slot];
    }

    public void outChange() {
        long amount = bank;
        if (mode) amount = cashRegister;

        int[] dollarOut = new int[6];
        for(int i = 5; i > 0; i--) {
            int billValue = ModConfig.billValueList.get(i);
            dollarOut[i] = Math.round(amount / billValue);
            amount = amount - (dollarOut[i] * billValue);
        }
        dollarOut[0] = 0;

        int[] coinOut = new int[6];
        for(int i = 5; i > 0; i--) {
            int coinValue = ModConfig.coinValueList.get(i);
            coinOut[i] = Math.round(amount / coinValue);
            amount = amount - (coinOut[i] * coinValue);
        }
        coinOut[0] = Math.round(amount);

        if (!world.isRemote) {
            if (mode) {
                cashRegister = 0;
            } else {
                bank = 0;
            }

            for(int i = 0; i < dollarOut.length + coinOut.length; i++){
                ItemStack item;

                if(i < dollarOut.length){
                    item = new ItemStack(ModItems.itemBanknote);
                    item.setItemDamage(i);
                    item.setCount(dollarOut[i]);
                }else{
                    item = new ItemStack(ModItems.itemCoin);
                    item.setItemDamage(i - dollarOut.length);
                    item.setCount(coinOut[i - dollarOut.length]);
                }

                boolean check;
                if(i < dollarOut.length){
                    check = dollarOut[i] != 0;
                }else{
                    check = coinOut[i - dollarOut.length] != 0;
                }

                if(check){
                    boolean playerInGui= false;
                    if (playerUsing != null) playerInGui = true;

                    if (playerInGui) {
                        InventoryPlayer inventoryPlayer = playerUsing.inventory;
                        boolean placed = false;

                        //Looks for item in inventory before putting in a empty slot
                        searchLoop:
                        for (int j = 0; j < 36; j++) {
                            if (UtilMethods.equalStacks(item, inventoryPlayer.getStackInSlot(j))) {
                                if (inventoryPlayer.getStackInSlot(j).getCount() + item.getCount() <= inventoryPlayer.getStackInSlot(j).getMaxStackSize()) {
                                    inventoryPlayer.getStackInSlot(j).setCount(inventoryPlayer.getStackInSlot(j).getCount() + item.getCount());
                                    placed = true;
                                    break searchLoop;
                                }
                            }
                        }

                        if (!placed) {
                            if (inventoryPlayer.getFirstEmptyStack() != -1) {     //If Players Inventory has room
                                //Todo include a warning symbol that tells user they have no room in their inventory
                                //Todo if player has wallet try to place in WALLET first before inventory
                                inventoryPlayer.setInventorySlotContents(inventoryPlayer.getFirstEmptyStack(), item);
                            } else {
                                playerInGui = false;
                            }
                        }
                    }

                    if (!playerInGui) {       //If no room, spawn
                        int x = getPos().getX();
                        int z = getPos().getZ();

                        switch (this.getBlockMetadata()) {
                            case 0:
                                z = z + 1; //North
                                break;
                            case 1:
                                x = x - 1; //East
                                break;
                            case 2:
                                z = z - 2; //South
                                break;
                            case 3:
                                x = x + 1;//West
                                break;
                        }
                        world.spawnEntity(new EntityItem(world, x, getPos().up().getY(), z, item));
                    }
                }
            }

            if (mode) {
                if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                    PacketSetLongToClient pack = new PacketSetLongToClient();
                    pack.setData(getPos(), LONG_CASHREG, cashRegister);
                    PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                }
            } else {
                if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
                    PacketSetLongToClient pack = new PacketSetLongToClient();
                    pack.setData(getPos(), LONG_BANK, bank);
                    PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
                }
            }
        }
    }

    public void outInputSlot(){
        if (inputStackHandler.getStackInSlot(0).getItem() != Item.getItemFromBlock(Blocks.AIR)) {
            if (!world.isRemote) {
                ItemStack item = inputStackHandler.getStackInSlot(0);
                inputStackHandler.setStackInSlot(0, ItemStack.EMPTY);

                int x = getPos().getX();
                int z = getPos().getZ();
                switch (this.getBlockMetadata()) {
                    case 0:
                        z = z - 2; //North
                        break;
                    case 1:
                        x = x + 2; //East
                        break;
                    case 2:
                        z = z + 2; //South
                        break;
                    case 3:
                        x = x - 2;//West
                        break;
                }

                world.spawnEntity(new EntityItem(world, x, getPos().up().getY(), z, item));
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="NBT & Packet Stoof--------------------------------------------------------------------------------------------------">
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("item", vendStackHandler.serializeNBT());
        compound.setTag("buffer", bufferStackHandler.serializeNBT());
        compound.setTag("input", inputStackHandler.serializeNBT());
        compound.setTag("autoInput", automationInputStackHandler.serializeNBT());
        compound.setLong("bank", bank);
        compound.setLong("cashRegister", cashRegister);
        compound.setBoolean("locked", locked);
        compound.setBoolean("mode", mode);
        compound.setBoolean("creative", creative);
        compound.setBoolean("infinite", infinite);
        compound.setBoolean("voidBlock", voidBlock);
        compound.setBoolean("gearExtended", gearExtended);
        compound.setBoolean("twoBlock", twoBlock);
        compound.setInteger("selectedSlot", selectedSlot);
        compound.setString("selectedName", selectedName);
        compound.setString("owner", owner);
        compound.setBoolean("upgradeReq", upgradeReq);

        NBTTagCompound itemCostsNBT = new NBTTagCompound();
        NBTTagCompound itemAmountNBT = new NBTTagCompound();
        NBTTagCompound itemAmntNBT = new NBTTagCompound();
        for (int i = 0; i < itemCosts.length; i++){
            itemCostsNBT.setInteger("cost" + i, itemCosts[i]);
            itemAmountNBT.setInteger("amount" + i, itemAmounts[i]);
            itemAmntNBT.setInteger("amnt" + i, bundleAmount[i]);
        }
        compound.setTag("itemCosts", itemCostsNBT);
        compound.setTag("itemAmounts", itemAmountNBT);
        compound.setTag("itemAmnts", itemAmntNBT);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("item")) vendStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("item"));
        if (compound.hasKey("buffer")) bufferStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("buffer"));
        if (compound.hasKey("input")) inputStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("input"));
        if (compound.hasKey("autoInput")) automationInputStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("autoInput"));
        if (compound.hasKey("bank")) bank = compound.getLong("bank");
        if (compound.hasKey("cashRegister")) cashRegister = compound.getLong("cashRegister");
        if (compound.hasKey("locked")) locked = compound.getBoolean("locked");
        if (compound.hasKey("mode")) mode = compound.getBoolean("mode");
        if (compound.hasKey("creative")) creative = compound.getBoolean("creative");
        if (compound.hasKey("infinite")) infinite = compound.getBoolean("infinite");
        if (compound.hasKey("voidBlock")) voidBlock = compound.getBoolean("voidBlock");
        if (compound.hasKey("gearExtended")) gearExtended = compound.getBoolean("gearExtended");
        if (compound.hasKey("twoBlock")) twoBlock = compound.getBoolean("twoBlock");
        if (compound.hasKey("selectedSlot")) selectedSlot = compound.getInteger("selectedSlot");
        if (compound.hasKey("selectedName")) selectedName = compound.getString("selectedName");
        if (compound.hasKey("owner")) owner = compound.getString("owner");
        if (compound.hasKey("upgradeReq")) upgradeReq = compound.getBoolean("upgradeReq");

        if (compound.hasKey("itemCosts")) {
            NBTTagCompound itemCostsNBT = compound.getCompoundTag("itemCosts");
            for (int i = 0; i < itemCosts.length; i++) itemCosts[i] = itemCostsNBT.getInteger("cost" + i);
        }

        if (compound.hasKey("itemAmounts")) {
            NBTTagCompound itemAmountsNBT = compound.getCompoundTag("itemAmounts");
            for (int i = 0; i < itemAmounts.length; i++) itemAmounts[i] = itemAmountsNBT.getInteger("amount" + i);
        }

        if (compound.hasKey("itemAmnts")) {
            NBTTagCompound itemSizeNBT = compound.getCompoundTag("itemAmnts");
            for (int i = 0; i < VEND_SLOT_COUNT; i++) bundleAmount[i] = itemSizeNBT.getInteger("amnt" + i);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("bank", bank);
        tag.setLong("cashRegister", cashRegister);
        tag.setBoolean("locked", locked);
        tag.setBoolean("mode", mode);
        tag.setBoolean("creative", creative);
        tag.setBoolean("infinite", infinite);
        tag.setBoolean("voidBlock", voidBlock);
        tag.setBoolean("gearExtended", gearExtended);
        tag.setBoolean("twoBlock", twoBlock);
        tag.setInteger("selectedSlot", selectedSlot);
        tag.setString("selectedName", selectedName);
        tag.setString("owner", owner);
        tag.setBoolean("upgradeReq", upgradeReq);

        NBTTagCompound itemCostsNBT = new NBTTagCompound();
        NBTTagCompound itemAmountsNBT = new NBTTagCompound();
        NBTTagCompound itemAmntNBT = new NBTTagCompound();

        for (int i = 0; i < VEND_SLOT_COUNT; i++) {
            itemCostsNBT.setInteger("cost" + i, itemCosts[i]);
            itemAmountsNBT.setInteger("amount" + i, itemAmounts[i]);
            itemAmntNBT.setInteger("amnt" + i, bundleAmount[i]);
        }

        tag.setTag("itemCosts", itemCostsNBT);
        tag.setTag("itemAmounts", itemAmountsNBT);
        tag.setTag("itemAmnts", itemAmntNBT);

        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        bank = pkt.getNbtCompound().getLong("bank");
        cashRegister = pkt.getNbtCompound().getLong("cashRegister");
        locked = pkt.getNbtCompound().getBoolean("locked");
        mode = pkt.getNbtCompound().getBoolean("mode");
        creative = pkt.getNbtCompound().getBoolean("creative");
        infinite = pkt.getNbtCompound().getBoolean("infinite");
        voidBlock = pkt.getNbtCompound().getBoolean("voidBlock");
        gearExtended = pkt.getNbtCompound().getBoolean("gearExtended");
        twoBlock = pkt.getNbtCompound().getBoolean("twoBlock");
        selectedSlot = pkt.getNbtCompound().getInteger("selectedSlot");
        selectedName = pkt.getNbtCompound().getString("selectedName");
        owner = pkt.getNbtCompound().getString("owner");
        upgradeReq = pkt.getNbtCompound().getBoolean("upgradeReq");

        NBTTagCompound itemCostsNBT = pkt.getNbtCompound().getCompoundTag("itemCosts");
        NBTTagCompound itemAmountsNBT = pkt.getNbtCompound().getCompoundTag("itemAmounts");
        NBTTagCompound itemAmntNBT = pkt.getNbtCompound().getCompoundTag("itemAmnts");

        for (int i = 0; i < VEND_SLOT_COUNT; i++) {
            itemCosts[i] = itemCostsNBT.getInteger("cost" + i);
            itemAmounts[i] = itemAmountsNBT.getInteger("amount" + i);
            bundleAmount[i] = itemAmntNBT.getInteger("amnt" + i);
        }
    }
    //</editor-fold>--------------------------------

    //<editor-fold desc="ItemStackHandler Methods--------------------------------------------------------------------------------------------">
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return facing == null || locked ;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) return (T) new CombinedInvWrapper(inputStackHandler, vendStackHandler, bufferStackHandler); //Inside Itself
            if (facing == EnumFacing.DOWN) return (T) bufferStackHandler;
            if (facing != EnumFacing.DOWN) return (T) automationInputStackHandler;
        }
        return super.getCapability(capability, facing);
    }
    //</editor-fold>

    //<editor-fold desc="Getter & Setter Methods---------------------------------------------------------------------------------------------">
    public int getFieldCount() {
        return 9;
    }

    public void setField(int id, int value) {
        switch (id) {
            case FIELD_LOCKED:
                locked = (value == 1);
                break;
            case FIELD_MODE:
                mode = (value == 1);
                break;
            case FIELD_SELECTSLOT:
                selectedSlot = value;
                break;
            case FIELD_CREATIVE:
                creative = (value == 1);
                break;
            case FIELD_INFINITE:
                infinite = (value == 1);
                break;
            case FIELD_TWOBLOCK:
                twoBlock = (value == 1);
                break;
            case FIELD_GEAREXT:
                gearExtended = (value == 1);
                break;
            case FIELD_VOID:
                if(infinite) {
                    voidBlock = (value == 1);
                }else voidBlock = true;
                break;
            case FIELD_UPGRADEREQ:
                upgradeReq = (value == 1);
                break;
        }
    }

    public int getField(int id) {
        switch (id) {
            case FIELD_LOCKED:
                return (locked) ? 1 : 0;
            case FIELD_MODE:
                return (mode) ? 1 : 0;
            case FIELD_SELECTSLOT:
                return selectedSlot;
            case FIELD_CREATIVE:
                return (creative) ? 1 : 0;
            case FIELD_INFINITE:
                return (infinite) ? 1 : 0;
            case FIELD_TWOBLOCK:
                return (twoBlock) ? 1 : 0;
            case FIELD_GEAREXT:
                return (gearExtended) ? 1 : 0;
            case FIELD_VOID:
                return (voidBlock) ? 1 : 0;
            case FIELD_UPGRADEREQ:
                return (upgradeReq) ? 1 : 0;
        }
        return -1;
    }

    public void setLong(byte id, long value){
        switch(id){
            case LONG_BANK:
                bank = value;
                break;
            case LONG_CASHREG:
                cashRegister = value;
                break;
        }
        if(!getWorld().isRemote && getPlayerUsing() != null && PacketHandler.INSTANCE != null){
            PacketSetLongToClient pack = new PacketSetLongToClient();
            pack.setData(getPos(), id, value);
            PacketHandler.INSTANCE.sendTo(pack, (EntityPlayerMP) getPlayerUsing());
        }
    }

    public long getLong(int id){
        switch(id){
            case LONG_BANK:
                return bank;
            case LONG_CASHREG:
                return cashRegister;
        }
        return -1;
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(String name) {
        selectedName = name;
    }

    public int getItemCost(int index) {
        return itemCosts[index];
    }

    public void setItemCost(int amount) {
        itemCosts[selectedSlot - 37] = amount;
    }

    public void setItemCost(int amount, int index) {
        itemCosts[index] = amount;
    }

    public int getItemAmount(int index) {
        return itemAmounts[index];
    }

    public void setItemAmount(int amount, int index){
        itemAmounts[index] = amount;
        if(amount == -1){
            vendStackHandler.getStackInSlot(index).setCount(1);
        }else {
            vendStackHandler.getStackInSlot(index).setCount(itemAmounts[index]);
        }
    }

    public int getBundleAmnt(int index) {
        return bundleAmount[index];
    }

    public void setBundleAmnt(int amount) {
        bundleAmount[selectedSlot - 37] = amount;
    }

    public ItemStackHandler getBufferStackHandler(){
        return bufferStackHandler;
    }

    public ItemStackHandler getInputStackHandler(){
        return inputStackHandler;
    }

    public ItemStackHandler getVendStackHandler(){
        return vendStackHandler;
    }

    public void setBufferStackHandler(ItemStackHandler buf){
        bufferStackHandler = buf;
    }

    public void setInputStackHandler(ItemStackHandler input){
        inputStackHandler = input;
    }

    public void setVendStackHandler(ItemStackHandler vend){
        vendStackHandler = vend;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public EntityPlayer getPlayerUsing(){
        return playerUsing;
    }

    public void voidPlayerUsing(){
        playerUsing = null;
    }
    //</editor-fold>
}
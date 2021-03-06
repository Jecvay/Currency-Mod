package gunn.modcurrency.mod.client.gui;

import gunn.modcurrency.mod.client.gui.util.TabButton;
import gunn.modcurrency.mod.container.ContainerExchanger;
import gunn.modcurrency.mod.container.ContainerVending;
import gunn.modcurrency.mod.network.*;
import gunn.modcurrency.mod.tileentity.TileExchanger;
import gunn.modcurrency.mod.utils.UtilMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Distributed with the Currency-Mod for Minecraft
 * Copyright (C) 2017  Brady Gunn
 *
 * File Created on 2017-05-09
 */
public class GuiExchanger extends GuiContainer {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("modcurrency", "textures/gui/guivendortexture.png");
    private static final ResourceLocation TAB_TEXTURE = new ResourceLocation("modcurrency", "textures/gui/guivendortabtexture.png");
    private TileExchanger tile;
    private boolean creativeExtended;
    private EntityPlayer player;
    private GuiTextField priceField, requestField, amountField;

    private static final int CHANGEBUTTON_ID = 0;
    private static final int INFINITEBUTTON_ID = 1;
    private static final int VOIDITEMSBUTTON_ID = 2;
    private static final int MODE_ID = 3;
    private static final int LOCK_ID = 4;
    private static final int GEAR_ID = 5;
    private static final int CREATIVE_ID = 6;


    public GuiExchanger(EntityPlayer entityPlayer, TileExchanger te) {
        super(new ContainerExchanger(entityPlayer.inventory, te));
        tile = te;
        xSize = 176;
        ySize = 235;
        creativeExtended = false;
        player = entityPlayer;
    }

    @Override
    public void initGui() {
        super.initGui();
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        String ChangeButton = "Cash";

        this.buttonList.add(new GuiButton(CHANGEBUTTON_ID, i + 103, j + 7, 45, 20, ChangeButton));
        this.buttonList.add(new GuiButton(INFINITEBUTTON_ID, i + 198, j + 85, 45, 20, "BORKED"));
        this.buttonList.add(new GuiButton(VOIDITEMSBUTTON_ID, i + 180, j + 85, 20, 20, ""));
        this.buttonList.add(new TabButton("Mode", MODE_ID, i - 20, j + 20, 0, 88, 20, 21, 0,"", TAB_TEXTURE));
        this.buttonList.add(new TabButton("Lock", LOCK_ID, i - 20, 22 + ((TabButton) this.buttonList.get(MODE_ID)).getButtonY(), 0, 22, 20, 21, 0,"", TAB_TEXTURE));
        this.buttonList.add(new TabButton("Gear", GEAR_ID, i - 20, 22 + ((TabButton) this.buttonList.get(LOCK_ID)).getButtonY(), 0, 0, 20, 21, 0,"", TAB_TEXTURE));
        this.buttonList.add(new TabButton("Creative", CREATIVE_ID, i - 20, 22 + ((TabButton) this.buttonList.get(GEAR_ID)).getButtonY(), 0, 44, 20, 21, 0,"", TAB_TEXTURE));

        this.priceField = new GuiTextField(0, fontRenderer, i - 48, j + 91, 45, 10);        //Setting Costs
        this.priceField.setTextColor(Integer.parseInt("0099ff", 16));
        this.priceField.setEnableBackgroundDrawing(false);
        this.priceField.setMaxStringLength(7);

        this.requestField = new GuiTextField(0, fontRenderer, i - 48, j + 111, 25, 10);        //Setting Amounts
        this.requestField.setTextColor(Integer.parseInt("0099ff", 16));
        this.requestField.setEnableBackgroundDrawing(false);
        this.requestField.setMaxStringLength(3);

        this.amountField = new GuiTextField(1, fontRenderer, i - 58, j + 100, 50, 8); //Setting Amount to sell
        this.amountField.setTextColor(Integer.parseInt("0099ff", 16));
        this.amountField.setEnableBackgroundDrawing(false);
        this.amountField.setMaxStringLength(3);
        this.amountField.setEnabled(false);

        this.buttonList.get(MODE_ID).visible = false;
        this.buttonList.get(LOCK_ID).visible = false;
        this.buttonList.get(GEAR_ID).visible = false;
        this.buttonList.get(CREATIVE_ID).visible = false;
        this.buttonList.get(INFINITEBUTTON_ID).visible = false;
        this.buttonList.get(VOIDITEMSBUTTON_ID).visible = false;

        this.priceField.setEnabled(false);
        this.requestField.setEnabled(false);
    }

    private void setCost() {
        if (this.priceField.getText().length() > 0) {
            int newCost = 0;

            if (priceField.getText().contains(".")) {
                if (priceField.getText().lastIndexOf(".") + 1 != priceField.getText().length()) {
                    if (priceField.getText().lastIndexOf(".") + 2 == priceField.getText().length()) {
                        newCost = Integer.valueOf(this.priceField.getText().substring(priceField.getText().lastIndexOf(".") + 1) + "0");
                    } else {
                        newCost = Integer.valueOf(this.priceField.getText().substring(priceField.getText().lastIndexOf(".") + 1));
                    }
                }

                if (priceField.getText().lastIndexOf(".") != 0)
                    newCost += Integer.valueOf(this.priceField.getText().substring(0, priceField.getText().lastIndexOf("."))) * 100;

            } else {
                newCost = Integer.valueOf(this.priceField.getText()) * 100;
            }

            tile.setItemCost(newCost);
            PacketSetItemCostToServer pack = new PacketSetItemCostToServer();
            pack.setData(newCost, tile.getPos());
            PacketHandler.INSTANCE.sendToServer(pack);

            tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
        }
    }

    private void setRequested() {
        if (this.requestField.getText().length() > 0) {
            int newAmount = Integer.valueOf(this.requestField.getText());
            if (Integer.valueOf(this.requestField.getText()) == 0) newAmount = -1;

            tile.setItemAmount(newAmount, tile.getField(tile.FIELD_SELECTSLOT) - 37);
            PacketSetItemAmountToServer pack = new PacketSetItemAmountToServer();
            pack.setData(newAmount, tile.getPos(), tile.getField(tile.FIELD_SELECTSLOT) - 37);
            PacketHandler.INSTANCE.sendToServer(pack);

            tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
            updateTextField();
        }
    }

    private void setAmnt(){
        if (this.amountField.getText().length() > 0){
            int newAmnt = Integer.valueOf(amountField.getText());
            if (newAmnt == 0) newAmnt = 1;

            tile.setBundleAmnt(newAmnt);
            PacketSetItemBundleToServer pack = new PacketSetItemBundleToServer();
            pack.setData(newAmnt, tile.getPos());
            PacketHandler.INSTANCE.sendToServer(pack);

            tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    private void updateTextField() {
        priceField.setText(UtilMethods.translateMoney(tile.getItemCost(tile.getField(tile.FIELD_SELECTSLOT) - 37)));
        amountField.setText(Integer.toString(tile.getBundleAmnt(tile.getField(tile.FIELD_SELECTSLOT) - 37)));

        if(tile.getItemAmount(tile.getField(tile.FIELD_SELECTSLOT) - 37) == -1){
            requestField.setText("0");
        }else {
            requestField.setText(String.valueOf(tile.getItemAmount(tile.getField(tile.FIELD_SELECTSLOT) - 37)));
        }
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);
        PacketSetFieldToServer pack = new PacketSetFieldToServer();
        pack.setData(0, tile.FIELD_GEAREXT, tile.getPos());
        PacketHandler.INSTANCE.sendToServer(pack);

        creativeExtended = false;
    }

    //<editor-fold desc="Draw and Renders">
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
        if (tile.getField(tile.FIELD_GEAREXT) == 1 && tile.getField(tile.FIELD_MODE) == 1){
            priceField.drawTextBox();
            amountField.drawTextBox();
            if(tile.getField(tile.FIELD_UPGRADEREQ) == 1)requestField.drawTextBox();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(tile.getField(tile.FIELD_TWOBLOCK) == 1){
            drawTexturedModalRect(guiLeft + 43, guiTop + 31, 7, 210, 90, 18);
            drawTexturedModalRect(guiLeft + 43, guiTop + 103, 7, 210, 90, 18);
            drawTexturedModalRect(guiLeft + 43, guiTop + 121, 7, 210, 90, 18);
        }
        drawTexturedModalRect(guiLeft + 43, guiTop + 49, 7, 210, 90, 18);
        drawTexturedModalRect(guiLeft + 43, guiTop + 67, 7, 210, 90, 18);
        drawTexturedModalRect(guiLeft + 43, guiTop + 85, 7, 210, 90, 18);

        //Draw Input Icons
        if (tile.getField(tile.FIELD_MODE) == 0) {
            drawTexturedModalRect(guiLeft + 152, guiTop + 9, 198, 17, 15, 15);
        } else {
            drawTexturedModalRect(guiLeft + 152, guiTop + 9, 198, 0, 15, 15);
            //Draw Buffer Slot Backgrounds
            int y = 33;
            if(tile.getField(tile.FIELD_TWOBLOCK) == 1) y = 41;
            drawTexturedModalRect(guiLeft + 10, guiTop + y, 178, 34, 22, 82);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TAB_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        if (tile.getOwner().equals(player.getUniqueID().toString()) || player.isCreative()) {  //If owner or creative
            this.buttonList.get(MODE_ID).visible = true;

            if(tile.getField(tile.FIELD_MODE) == 1) { //If in EDIT mode
                this.buttonList.get(LOCK_ID).visible = true;
                this.buttonList.get(GEAR_ID).visible = true;

                //Hiding/Revealing slots in EDIT mode
                for (int k = 0; k < ((ContainerExchanger) this.inventorySlots).TE_BUFFER_COUNT; k++){
                    this.inventorySlots.getSlot(((ContainerExchanger) this.inventorySlots).TE_BUFFER_FIRST_SLOT_INDEX+ k).xPos= 13;
                }

                ((TabButton)buttonList.get(GEAR_ID)).setOpenState(tile.getField(tile.FIELD_GEAREXT) == 1, 38);
                if(((TabButton)buttonList.get(GEAR_ID)).openState()){
                    drawTexturedModalRect(-91, 64, 27, 0, 91, 47);
                    drawTexturedModalRect(-91, 84, 27, 8, 91, 40);
                    drawSelectionOverlay();
                    this.priceField.setEnabled(true);
                    this.amountField.setEnabled(true);


                    if(tile.getField(tile.FIELD_UPGRADEREQ) == 1) this.requestField.setEnabled(true);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(TAB_TEXTURE);
                    this.buttonList.set(CREATIVE_ID, new TabButton("Creative", CREATIVE_ID, i - 20, 22 + ((TabButton)this.buttonList.get(GEAR_ID)).getButtonY(),0, 44, 20, 21, 0,"", TAB_TEXTURE));
                }else{
                    this.priceField.setEnabled(false);
                    this.amountField.setEnabled(false);
                    this.requestField.setEnabled(false);
                    this.buttonList.set(CREATIVE_ID, new TabButton("Creative", CREATIVE_ID, i - 20, 22 + ((TabButton)this.buttonList.get(GEAR_ID)).getButtonY(),0, 44, 20, 21, 0,"", TAB_TEXTURE));
                }
                if(player.isCreative()){    //If in Creative
                    this.buttonList.get(CREATIVE_ID).visible = true;

                    ((TabButton)buttonList.get(CREATIVE_ID)).setOpenState(creativeExtended, 26 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY());
                    if(((TabButton)buttonList.get(CREATIVE_ID)).openState()){
                        this.buttonList.set(INFINITEBUTTON_ID, (new GuiButton(INFINITEBUTTON_ID, i - 79, j + 106 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), 45, 20, ((tile.getField(tile.FIELD_INFINITE) == 1) ? "Enabled" : "Disabled"))));
                        drawTexturedModalRect(-91, 86 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), 27, 96, 91, 47);
                        this.buttonList.set(VOIDITEMSBUTTON_ID, (new GuiButton(VOIDITEMSBUTTON_ID, i - 32, j + 106 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), 20, 20, "")));
                    }else{
                        this.buttonList.get(INFINITEBUTTON_ID).visible = false;
                        this.buttonList.get(VOIDITEMSBUTTON_ID).visible = false;
                    }
                }else{  //If in Survival
                    this.buttonList.get(CREATIVE_ID).visible = false;
                    this.buttonList.get(INFINITEBUTTON_ID).visible = false;
                    this.buttonList.get(VOIDITEMSBUTTON_ID).visible = false;

                }

            }else{  //In SELL mode
                this.buttonList.get(LOCK_ID).visible = false;
                this.buttonList.get(GEAR_ID).visible = false;
                this.buttonList.get(CREATIVE_ID).visible = false;
                this.buttonList.get(INFINITEBUTTON_ID).visible = false;
                this.buttonList.get(VOIDITEMSBUTTON_ID).visible = false;

                //Hiding/Revealing slots in SELL mode
                for (int k = 0; k < ((ContainerExchanger) this.inventorySlots).TE_BUFFER_COUNT; k++){
                    this.inventorySlots.getSlot(((ContainerExchanger) this.inventorySlots).TE_BUFFER_FIRST_SLOT_INDEX + k).xPos= -1000;
                }
            }
            drawIcons();
        }
        drawText();
    }

    private void drawText(){
        fontRenderer.drawString(I18n.format("Exchange Machine"), 5, 6, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("tile.modcurrency:gui.playerinventory"), 4, 142, Color.darkGray.getRGB());
        if (tile.getField(tile.FIELD_MODE) == 1){
            fontRenderer.drawString(I18n.format("tile.modcurrency:guivending.funds") + ": $" + UtilMethods.translateMoney(tile.getLong(tile.LONG_CASHREG)), 5, 15, Color.darkGray.getRGB());
        }else{
            fontRenderer.drawString(I18n.format("tile.modcurrency:guivending.cash") + ": $" + UtilMethods.translateMoney(tile.getLong(tile.LONG_BANK)), 5, 15, Color.darkGray.getRGB());
        }

        if (tile.getField(tile.FIELD_GEAREXT) == 1) {
            fontRenderer.drawStringWithShadow(I18n.format("tile.modcurrency:guivending.slotsettings"), -83, 70, Integer.parseInt("fff200", 16));
            fontRenderer.drawStringWithShadow(I18n.format("tile.modcurrency:guivending.cost"), -84, 91, Color.lightGray.getRGB());
            fontRenderer.drawString(I18n.format("$"), -55, 91, Integer.parseInt("0099ff", 16));
            fontRenderer.drawStringWithShadow(I18n.format("tile.modcurrency:guivending.bundle"), -84, 100, Color.lightGray.getRGB());

            if(tile.getField(tile.FIELD_UPGRADEREQ) == 1) {
                fontRenderer.drawString(I18n.format("x"), -54, 111, Integer.parseInt("0099ff", 16));
                fontRenderer.drawStringWithShadow(I18n.format("tile.modcurrency:guivending.amount"), -84, 111, Color.lightGray.getRGB());
            }

            GL11.glPushMatrix();
            GL11.glScaled(0.7, 0.7, 0.7);
            fontRenderer.drawString(I18n.format("[" + tile.getSelectedName() + "]"), -117, 115, Integer.parseInt("001f33", 16));
            fontRenderer.drawString(I18n.format("[" + tile.getSelectedName() + "]"), -118, 114, Integer.parseInt("0099ff", 16));
            GL11.glPopMatrix();
        }
        if (creativeExtended) {
            fontRenderer.drawStringWithShadow(I18n.format("tile.modcurrency:guivending.infinitestock"), -85, 92 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), Integer.parseInt("fff200", 16));
        }

    }

    private void drawIcons() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int size = 1;
        if(tile.getField(tile.FIELD_MODE) == 1){
            size = 3;
            if(tile.getField(tile.FIELD_CREATIVE) == 1) size = 4;
        }

        if(creativeExtended){
           drawTexturedModalRect(-31, 108 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), 216, 37, 19, 17);
           if(tile.getField(tile.FIELD_VOID) == 0) drawTexturedModalRect(-31, 108 + ((TabButton)this.buttonList.get(GEAR_ID)).openExtY(), 216, 19, 19, 17);
        }

        for (int k = 0; k < size; k++) {
            int tabLoc = 22 * (k + 1);
            int offSet2 = 0;
            if (tile.getField(tile.FIELD_GEAREXT) == 1) offSet2 = ((TabButton)this.buttonList.get(GEAR_ID)).openExtY();

            switch (k) {
                case 0:
                    drawTexturedModalRect(-19, tabLoc, 236, 73, 19, 16);
                    break;
                case 1:
                    if (tile.getField(tile.FIELD_LOCKED) == 0) {
                        drawTexturedModalRect(-19, tabLoc, 236, 1, 19, 16);
                    }else drawTexturedModalRect(-19, tabLoc, 216, 1, 19, 16);
                    break;
                case 2:
                    drawTexturedModalRect(-19, tabLoc, 236, 19, 19, 16); //Icon
                    break;
                case 3:
                    drawTexturedModalRect(-19, tabLoc + offSet2, 236, 37, 19, 16);
                    break;
            }
        }
    }

    private void drawSelectionOverlay() {
        if (tile.getField(tile.FIELD_GEAREXT) == 1) {
            int slotId = tile.getField(tile.FIELD_SELECTSLOT) -37;
            int slotColumn, slotRow;

            if (slotId >= 0 && slotId <= 4) {
                slotColumn = 0;
                slotRow = slotId + 1;
            } else if (slotId >= 5 && slotId <= 9) {
                slotColumn = 1;
                slotRow = (slotId + 1) - 5;
            } else if (slotId >= 10 && slotId <= 14) {
                slotColumn = 2;
                slotRow = (slotId + 1) - 10;
            } else if (slotId >= 15 && slotId <= 19) {
                slotColumn = 3;
                slotRow = (slotId + 1) - 15;
            } else if (slotId >= 20 && slotId <= 24) {
                slotColumn = 4;
                slotRow = (slotId + 1) - 20;
            } else {
                slotColumn = 5;
                slotRow = (slotId + 1) - 25;
            }

            if(tile.getField(tile.FIELD_TWOBLOCK) != 1) slotColumn++;

            Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
            drawTexturedModalRect(24 + (18 * slotRow), 30 + (18 * slotColumn), 177, 0, 20, 20); //Selection Box
        }
    }

    @Override
    protected void renderToolTip(ItemStack stack, int x, int y) {
        int i = (x - (this.width - this.xSize) / 2);
        int j = (y - (this.height - this.ySize) / 2);

        if (j < 140 && j > 30 && i >= 43) {
            int startX = 43;
            int startY = 31;
            int row = ((i - startX) / 18);
            int column = ((j - startY) / 18);
            int slot = row + (column * 5);
            if (tile.getField(tile.FIELD_TWOBLOCK) != 1) slot = slot - 5;
            List<String> list = new ArrayList<>();
            List<String> ogTooltip = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
            int tooltipStart = 1;


            //Adding name and subname of item before price and such
            if (ogTooltip.size() > 0) {
                list.add(ogTooltip.get(0));
                list.set(0, stack.getRarity().rarityColor + (String) list.get(0));
            }
            if (ogTooltip.size() > 1) if (ogTooltip.get(1) != "") {
                list.add(TextFormatting.GRAY + ogTooltip.get(1));
                tooltipStart = 2;
            }

            //Adding Vending Strings
            TextFormatting color = TextFormatting.YELLOW;
            if (tile.getField(tile.FIELD_MODE) == 0) {
                if (!tile.canAfford(slot)) {
                    color = TextFormatting.RED;
                } else {
                    color = TextFormatting.GREEN;
                }
            }

            list.add(("x" + tile.getBundleAmnt(slot) + " for " + color + "$" + UtilMethods.translateMoney((tile.getItemCost(slot)))));

            //adding original extra stuff AFTER price and such
            for (; tooltipStart < stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL).size(); tooltipStart++) {
                list.add(TextFormatting.GRAY + stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL).get(tooltipStart));
            }

            FontRenderer font = stack.getItem().getFontRenderer(stack);
            net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
            this.drawHoveringText(list, x, y, (font == null ? fontRenderer : font));
            net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();

            tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
        } else {
            super.renderToolTip(stack, x, y);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Keyboard and Mouse Inputs">
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(tile.getField(tile.FIELD_MODE) == 1) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            priceField.mouseClicked(mouseX, mouseY, mouseButton);
            amountField.mouseClicked(mouseX, mouseY, mouseButton);
            requestField.mouseClicked(mouseX, mouseY, mouseButton);

            if (tile.getField(tile.FIELD_GEAREXT) == 1 && mouseButton == 0) updateTextField();
        }else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        int numChar = Character.getNumericValue(typedChar);
        if ((tile.getField(tile.FIELD_MODE) == 1) && ((numChar >= 0 && numChar <= 9) || (keyCode == 14) || keyCode == 211 || (keyCode == 203) || (keyCode == 205)|| (keyCode == 52))) { //Ensures keys input are only numbers or backspace type keys

            if((keyCode == 52 && !priceField.getText().contains(".")) || keyCode != 52) {
                if (this.priceField.textboxKeyTyped(typedChar, keyCode)) setCost();
            }

            if ((keyCode != 52)){
                if (this.amountField.textboxKeyTyped(typedChar, keyCode)) setAmnt();
            }

            if(keyCode != 52 && this.requestField.textboxKeyTyped(typedChar, keyCode)) setRequested();

            if(priceField.getText().length() > 0)
                if(priceField.getText().substring(priceField.getText().length()-1).equals(".") )
                    priceField.setMaxStringLength(priceField.getText().length() + 2);
            if(!priceField.getText().contains(".")) priceField.setMaxStringLength(7);


        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    //Button Actions
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case CHANGEBUTTON_ID:  //Change Button
                PacketItemSpawnToServer pack0 = new PacketItemSpawnToServer();
                pack0.setBlockPos(tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack0);
                break;
            case INFINITEBUTTON_ID: //Infinite? Button
                PacketSetFieldToServer pack1 = new PacketSetFieldToServer();
                pack1.setData((tile.getField(tile.FIELD_INFINITE) == 1) ? 0 : 1, tile.FIELD_INFINITE, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack1);

                if(tile.getField(tile.FIELD_INFINITE) == 1){
                    PacketSetFieldToServer pack5 = new PacketSetFieldToServer();
                    pack5.setData((tile.getField(tile.FIELD_VOID) == 1) ? 0 : 1, tile.FIELD_VOID, tile.getPos());
                    PacketHandler.INSTANCE.sendToServer(pack5);
                }
                break;
            case VOIDITEMSBUTTON_ID: //Void Button
                PacketSetFieldToServer pack5 = new PacketSetFieldToServer();
                pack5.setData((tile.getField(tile.FIELD_VOID) == 1) ? 0 : 1, tile.FIELD_VOID, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack5);
                break;
            case MODE_ID: //Mode Button
                PacketSetFieldToServer pack2 = new PacketSetFieldToServer();
                pack2.setData((tile.getField(tile.FIELD_MODE) == 1) ? 0 : 1, tile.FIELD_MODE, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack2);

                creativeExtended = false;

                tile.setField(tile.FIELD_GEAREXT, 0);
                PacketSetFieldToServer pack2b = new PacketSetFieldToServer();
                pack2b.setData(0, tile.FIELD_GEAREXT, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack2b);
                tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
                break;
            case LOCK_ID: //Lock Button
                PacketSetFieldToServer pack3 = new PacketSetFieldToServer();
                pack3.setData((tile.getField(tile.FIELD_LOCKED) == 1) ? 0 : 1, tile.FIELD_LOCKED, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack3);
                tile.getWorld().notifyBlockUpdate(tile.getPos(), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 3);
                break;
            case GEAR_ID: //Gear Button
                int newGear = tile.getField(tile.FIELD_GEAREXT) == 1 ? 0 : 1;
                tile.setField(tile.FIELD_GEAREXT, newGear);
                PacketSetFieldToServer pack4 = new PacketSetFieldToServer();
                pack4.setData(newGear, tile.FIELD_GEAREXT, tile.getPos());
                PacketHandler.INSTANCE.sendToServer(pack4);
                break;
            case CREATIVE_ID: //Creative Button
                creativeExtended = !creativeExtended;
                break;
        }
    }
    //</editor-fold>
}

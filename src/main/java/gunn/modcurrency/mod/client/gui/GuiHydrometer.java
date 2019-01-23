package gunn.modcurrency.mod.client.gui;

import gunn.modcurrency.mod.container.ContainerATM;
import gunn.modcurrency.mod.tileentity.TileHydrometer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * This class was created by BeardlessBrady. It is distributed as
 * part of The Currency-Mod. Source Code located on github:
 * https://github.com/BeardlessBrady/Currency-Mod
 * -
 * Copyright (C) All Rights Reserved
 * File Created 2019-01-22
 */

public class GuiHydrometer extends GuiContainer {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("modcurrency", "textures/gui/guihydrotexture.png");
    private EntityPlayer player;
    private TileHydrometer te;

    public GuiHydrometer(EntityPlayer entityPlayer, TileHydrometer tile) {
        super(new ContainerHydroMeter(entityPlayer, tile));
        te = tile;
        player = entityPlayer;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        //Background
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop,0 ,0 , 176, 192);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(I18n.format("tile.modcurrency:blockatm.name"), 5, 6, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("tile.modcurrency:gui.playerinventory"), 7, 100, Color.darkGray.getRGB());
    }
}

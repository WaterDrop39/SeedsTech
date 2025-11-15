package org.seed.seedstech.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class FluidSlot extends Slot
{
	public FluidSlot(Container pContainer, int pSlot, int pX, int pY)
	{
		super(pContainer, pSlot, pX, pY);
	}
	public void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot)
	{
		LogUtils.getLogger().info("Mixin success");
	}
}

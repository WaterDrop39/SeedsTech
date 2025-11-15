package org.seed.seedstech.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.seed.seedstech.screens.FluidSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen
{
	@Shadow
	private void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {}

	@Inject(
			method = "renderSlot",
			at = @At("HEAD"),
			cancellable = true
	)
	private void mixinRenderSlot(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci)
	{
		LogUtils.getLogger().info("Mixin 27");
		if (pSlot instanceof FluidSlot s)
		{
			s.renderSlot(pGuiGraphics, pSlot);
			ci.cancel();
		}
	}
}

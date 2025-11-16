package org.seed.seedstech.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.seed.seedstech.lib.Lib;
import org.seed.seedstech.screens.FluidSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T>
{
	@Shadow
	@Nullable
	private Slot clickedSlot;

	@Shadow
	private ItemStack draggingItem;

	@Shadow
	private boolean isSplittingStack;

	@Shadow
	protected boolean isQuickCrafting;

	@Shadow
	@Final
	protected Set<Slot> quickCraftSlots;

	@Shadow
	private int quickCraftingType;

	@Shadow
	protected abstract void recalculateQuickCraftRemaining();

	@Shadow
	public abstract T getMenu();

	protected MixinContainerScreen(Component pTitle)
	{
		super(pTitle);
	}

	@Inject(
			method = "renderSlot",
			at = @At("HEAD"),
			cancellable = true
	)
	private void mixinRenderSlot(GuiGraphics pGuiGraphics, Slot __slot, CallbackInfo ci)
	{
		if ((__slot instanceof FluidSlot pSlot) && !this.minecraft.options.touchscreen().get())
		{
			ci.cancel();

			var fluid = pSlot.getFluid();
			if (fluid.isEmpty())
				return;

			pGuiGraphics.pose().pushPose();
			pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

			var fluidType = IClientFluidTypeExtensions.of(fluid.getFluid());
			pGuiGraphics.blit(fluidType.getStillTexture(fluid), pSlot.x, pSlot.y, 0,0,16,16);

			pGuiGraphics.pose().pushPose();
			pGuiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
			var str = Lib.getFluidAmountString(fluid.getAmount());
			pGuiGraphics.drawString(this.font, str, pSlot.x + 17 - this.font.width(str), pSlot.y + 9, 0xffffff, true);
			pGuiGraphics.pose().popPose();

			pGuiGraphics.pose().popPose();

		}
	}
}

package org.seed.seedstech.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlotItemHandler.class)
class MixinSlotItemHandler
{
	@Accessor("emptyInventory")
	public static Container getEmptyContainer()
	{
		throw new AssertionError();
	}
}

public class MixinSlotGetter
{
	public static Container getEmptyContainer()
	{
		return MixinSlotItemHandler.getEmptyContainer();
	}
}

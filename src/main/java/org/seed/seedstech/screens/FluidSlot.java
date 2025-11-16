package org.seed.seedstech.screens;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.seed.seedstech.lib.FluidStackHandler;
import org.seed.seedstech.mixin.MixinSlotGetter;

import java.util.Optional;

// 咱不可用，必须性有待商榷
public class FluidSlot extends Slot
{
	protected FluidStackHandler handler;
	public FluidSlot(FluidStackHandler handler, int pSlot, int pX, int pY)
	{
		super(MixinSlotGetter.getEmptyContainer(), pSlot, pX, pY);
		this.handler = handler;
	}

	public FluidStack getFluid()
	{
		return handler.getFluidInTank(getSlotIndex()).copy();
	}

	/**
	 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
	 *
	 * @param pStack
	 */
	@Override
	public boolean mayPlace(ItemStack pStack)
	{
		return false;
	}

	/**
	 * Helper function to get the stack in the slot.
	 */
	@Override
	public ItemStack getItem()
	{
		return ItemStack.EMPTY;
	}

	/**
	 * Returns if this slot contains a stack.
	 */
	@Override
	public boolean hasItem()
	{
		return false;
	}

	/**
	 * Helper method to put a stack in the slot.
	 *
	 * @param pStack
	 */
	@Override
	public void set(ItemStack pStack)
	{
		//throw new RuntimeException("Put a ItemStack in a FluidSlot");
	}

	/**
	 * Called when the stack in a Slot changes
	 */
	@Override
	public void setChanged()
	{
		super.setChanged();
	}

	/**
	 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
	 * of armor slots)
	 */
	@Override
	public int getMaxStackSize()
	{
		return handler.getTankCapacity(getContainerSlot());
	}

	@Override
	public int getMaxStackSize(ItemStack pStack)
	{
		return 0;
	}

	/**
	 * Return whether this slot's stack can be taken from this slot.
	 *
	 * @param pPlayer
	 */
	@Override
	public boolean mayPickup(Player pPlayer)
	{
		return false;
	}

	/**
	 * Checks if the other slot is in the same inventory, by comparing the inventory reference.
	 *
	 * @param other
	 * @return true if the other slot is in the same inventory
	 */
	@Override
	public boolean isSameInventory(Slot other)
	{
		return other instanceof FluidSlot s && handler == s.handler;
	}

	@Override
	public Optional<ItemStack> tryRemove(int pCount, int pDecrement, Player pPlayer)
	{
		return Optional.of(ItemStack.EMPTY);
	}

	@Override
	public ItemStack safeTake(int pCount, int pDecrement, Player pPlayer)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack safeInsert(ItemStack pStack)
	{
		/*if (pStack.isEmpty())
			return ItemStack.EMPTY;

		if (pStack.getItem() instanceof BucketItem)
		{
			var bucket = new FluidBucketWrapper(pStack);
			if (!handler.getFluidInTank(getContainerSlot()).isFluidEqual(bucket.getFluid()))
				return pStack;
			if (handler.getSpace(getContainerSlot()) < bucket.getTankCapacity(0))
				return pStack;
			handler.fill(getContainerSlot(), bucket.getFluid(), IFluidHandler.FluidAction.EXECUTE);
			return Items.BUCKET.getDefaultInstance();
		}
		else if (pStack.getItem() instanceof IFluidHandlerItem i)
		{
			if (i.getTanks() <= 0)
				return pStack;
			if (!handler.getFluidInTank(getContainerSlot()).isFluidEqual(i.getFluidInTank(0)))
				return pStack;
			if (handler.getSpace(getContainerSlot()) < bucket.getTankCapacity(0))
				return pStack;
			handler.fill(getContainerSlot(), bucket.getFluid(), IFluidHandler.FluidAction.EXECUTE);
			return Items.BUCKET.getDefaultInstance();
		}
		return pStack;*/
		return pStack;
	}

	@Override
	public ItemStack safeInsert(ItemStack pStack, int pIncrement)
	{
		return super.safeInsert(pStack, pIncrement);
	}

	@Override
	public boolean allowModification(Player pPlayer)
	{
		return super.allowModification(pPlayer);
	}
}

package org.seed.seedstech.blockentities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.seed.seedstech.Config;
import org.seed.seedstech.blocks.BaseMachineDefinition;
import org.seed.seedstech.lib.*;
import org.seed.seedstech.menus.BaseMachineMenu;
import org.seed.seedstech.recipes.MachineRecipe;

import java.security.InvalidParameterException;

public class BaseMachineEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible
{
	public static final int INPUT_SLOT_COUNT = 12;
	public static final int OUTPUT_SLOT_COUNT = 12;
	public static final int EXTRA_SLOT_COUNT = 18;
	public static final int INPUT_TANK_COUNT = 12;
	public static final int OUTPUT_TANK_COUNT = 12;

	public static final int DATA_PROCESS_INDEX = 0;
	public static final int DATA_PROCESS_NEED_TIME_INDEX = 1;
	public static final int DATA_MAX_INDEX = 1;

	public final BaseMachineDefinition machineType;

	protected int progress = 0;

	public final ContainerData data = new ContainerData()
	{
		@Override
		public int get(int pIndex)
		{
			return switch (pIndex)
			{
				case DATA_PROCESS_INDEX -> progress;
				case DATA_PROCESS_NEED_TIME_INDEX -> workingRecipe == null ? 1 : workingRecipe.needTime;
				default ->
						throw new InvalidParameterException(String.format("ContainerData index %d out of [0, %d)", pIndex, DATA_MAX_INDEX + 1));
			};
		}

		@Override
		public void set(int pIndex, int pValue)
		{
			throw new UnsupportedOperationException("ContainerData is read only");
		}

		@Override
		public int getCount()
		{
			return 2;
		}
	};

	public LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

	protected class OnChangedItemStackHandler extends ItemStackHandler
	{
		OnChangedItemStackHandler(int size)
		{
			super(size);
		}

		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	}

	protected class OnChangedFluidStackHandler extends FluidStackHandler
	{
		public OnChangedFluidStackHandler(int size, int capacity)
		{
			super(size, capacity);
		}

		@Override
		protected void onContentsChanged(int tank)
		{
			setChanged();
		}
	}

	protected final OnChangedItemStackHandler inItemStack = new OnChangedItemStackHandler(INPUT_SLOT_COUNT);
	protected final OnChangedItemStackHandler outItemStack = new OnChangedItemStackHandler(OUTPUT_SLOT_COUNT);
	protected final OnChangedItemStackHandler extraItemStack = new OnChangedItemStackHandler(EXTRA_SLOT_COUNT);
	protected final OnChangedFluidStackHandler inFluidStack;
	protected final OnChangedFluidStackHandler outFluidStack;

	private String loadRecipe = null;
	@Nullable public MachineRecipe workingRecipe = null;

	public BaseMachineEntity(BlockPos pos, BlockState state, BaseMachineDefinition type)
	{
		super(type.blockEntityType.get(), pos, state);
		machineType = type;

		var volume = Config.SINGLE_BLOCK_FLUID_VOLUME.get();

		inFluidStack = new OnChangedFluidStackHandler(INPUT_TANK_COUNT, volume);
		outFluidStack = new OnChangedFluidStackHandler(OUTPUT_TANK_COUNT, volume);
	}

	/**
	 * 用于向自动化暴露的{@link net.minecraftforge.fluids.capability.IFluidHandler}
	 */
	/*private class FluidHandlerWrapper implements IFluidHandler
	{
		private void validateTankIndex(int tank)
		{
			if (tank < 0 || tank >= getTanks())
				throw new IndexOutOfBoundsException(String.format("Slot %d not in valid range - [0,%d)", tank, getTanks()));
		}

		private FluidTank getTankByIndex(int index)
		{
			validateTankIndex(index);
			return index >= inFluidStack.length ?
					outFluidStack[index - inFluidStack.length] :
					inFluidStack[index];
		}

		@Override
		public int getTanks()
		{
			return inFluidStack.length + outFluidStack.length;
		}

		@Override
		public @NotNull FluidStack getFluidInTank(int tank)
		{
			return getTankByIndex(tank).getFluid().copy();
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return getTankByIndex(tank).getCapacity();
		}

		@Override
		public boolean isFluidValid(int tank, @NotNull FluidStack stack)
		{
			if (getTankByIndex(tank).getFluid().isFluidEqual(stack) || getTankByIndex(tank).isEmpty())
				return getTankByIndex(tank).getSpace() >= stack.getAmount();
			return false;
		}

		*//**
		 * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
		 *
		 * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
		 * @param action   If SIMULATE, fill will only be simulated.
		 * @return Amount of resource that was (or would have been, if simulated) filled.
		 *//*
		@Override
		public int fill(FluidStack resource, FluidAction action)
		{
			if (resource.isEmpty())
				return 0;
			for (var fluidTank : inFluidStack)
				if ((fluidTank.getFluid().isFluidEqual(resource) && fluidTank.getSpace() > 0) || fluidTank.isEmpty())
				{
					var putAmount = Math.min(fluidTank.getSpace(), resource.getAmount());
					var remainFluid = resource.copy();
					remainFluid.shrink(putAmount);
					if (action.execute())
						fluidTank.getFluid().grow(putAmount);
					return putAmount + fill(remainFluid, action);
				}
			if (action.execute())
				setChanged();
			return 0;
		}

		public FluidStack drainFromAll(FluidStack resource, FluidAction action)
		{
			if (resource.isEmpty())
				return FluidStack.EMPTY;
			var getFluid = resource.copy();
			getFluid.setAmount(0);
			int needAmount = resource.getAmount();
			for (int i = getTanks() - 1; i >= 0 && needAmount > 0; --i)
			{
				var fluidTank = getTankByIndex(i);
				if (fluidTank.getFluid().isFluidEqual(resource))
				{
					var putAmount = Math.min(fluidTank.getFluidAmount(), needAmount);
					if (action.execute())
						fluidTank.getFluid().shrink(putAmount);
					getFluid.grow(putAmount);
					needAmount -= putAmount;
				}
			}
			if (action.execute())
				setChanged();
			return getFluid;
		}

		// 只能从输出槽中提取
		@Override
		public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
		{
			if (resource.isEmpty())
				return FluidStack.EMPTY;
			var getFluid = resource.copy();
			getFluid.setAmount(0);
			int needAmount = resource.getAmount();
			for (int i = outFluidStack.length - 1; i >= 0 && needAmount > 0; --i)
			{
				var fluidTank = outFluidStack[i];
				if (fluidTank.getFluid().isFluidEqual(resource))
				{
					var putAmount = Math.min(fluidTank.getFluidAmount(), needAmount);
					if (action == FluidAction.EXECUTE)
						fluidTank.getFluid().shrink(putAmount);
					getFluid.grow(putAmount);
					needAmount -= putAmount;
				}
			}
			if (action.execute())
				setChanged();
			return getFluid;
		}

		*//**
		 * 只能从输出槽中提取
		 *
		 * @param maxDrain Maximum amount of fluid to drain.
		 * @param action   If SIMULATE, drain will only be simulated.
		 * @return 取出的流体FluidStack
		 *//*
		@Override
		public @NotNull FluidStack drain(int maxDrain, FluidAction action)
		{
			if (maxDrain <= 0)
				return FluidStack.EMPTY;
			for (int i = outFluidStack.length - 1; i >= 0; --i)
			{
				var fluidTank = outFluidStack[i];
				if (!fluidTank.isEmpty())
				{
					var fluid = fluidTank.getFluid().copy();
					fluid.setAmount(maxDrain);
					return drain(fluid, action);
				}
			}
			if (action.execute())
				setChanged();
			return FluidStack.EMPTY;
		}
	}


	*//**
	 * 用于向自动化暴露的{@link net.minecraftforge.items.IItemHandler}
	 *//*
	private class ItemHandlerWrapper implements IItemHandler
	{
		private ItemStack getByIndex(int index)
		{
			if (index < inItemStack.getSlots())
				return inItemStack.getStackInSlot(index);
			else if (index < inItemStack.getSlots() + outItemStack.getSlots())
				return outItemStack.getStackInSlot(index - inItemStack.getSlots());
			else
				throw new IndexOutOfBoundsException();
		}

		@Override
		public int getSlots()
		{
			return inItemStack.getSlots() + outItemStack.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot)
		{
			return getByIndex(slot).copy();
		}

		@Override
		@NotNull
		public ItemStack insertItem(int slot, @Nullable ItemStack itemStack, boolean simulate)
		{
			if (itemStack == null || itemStack.isEmpty())
				return ItemStack.EMPTY;
			if (slot >= inItemStack.getSlots() || slot < 0)
				return itemStack;
			if (!simulate)
				setChanged();
			return inItemStack.insertItem(slot, itemStack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if (slot >= inItemStack.getSlots() + outItemStack.getSlots() || slot < inItemStack.getSlots())
				return ItemStack.EMPTY;
			if (!simulate)
				setChanged();
			return outItemStack.extractItem(slot - inItemStack.getSlots(), amount, simulate);
		}

		@Override
		public int getSlotLimit(int index)
		{
			if (index < inItemStack.getSlots())
				return inItemStack.getSlotLimit(index);
			else if (index < inItemStack.getSlots() + outItemStack.getSlots())
				return outItemStack.getSlotLimit(index - inItemStack.getSlots());
			else
				throw new IndexOutOfBoundsException();
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack)
		{
			return slot >= 0 && slot < inItemStack.getSlots();
		}
	}

	*//**
	 * 用于向Screen暴露的{@link net.minecraftforge.items.IItemHandler}
	 *//*
	private class ScreenItemHandler implements IItemHandler
	{
		private ItemStack getByIndex(int index)
		{
*//*			if (tank < 0 || tank >= getTanks())
				throw new IndexOutOfBoundsException(String.format("Slot %d not in valid range - [0,%d)", tank, getTanks()));*//*
			if (index < inItemStack.getSlots())
				return inItemStack.getStackInSlot(index);
			else if (index < inItemStack.getSlots() + outItemStack.getSlots())
				return outItemStack.getStackInSlot(index - inItemStack.getSlots());
			else
				throw new IndexOutOfBoundsException();
		}

		@Override
		public int getSlots()
		{
			return inItemStack.getSlots() + outItemStack.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot)
		{
			return getByIndex(slot).copy();
		}

		@Override
		@NotNull
		public ItemStack insertItem(int slot, @Nullable ItemStack itemStack, boolean simulate)
		{
			if (itemStack == null || itemStack.isEmpty())
				return ItemStack.EMPTY;
			if (slot >= inItemStack.getSlots() || slot < 0)
				return itemStack;
			if (!simulate)
				setChanged();
			return inItemStack.insertItem(slot, itemStack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if (slot >= inItemStack.getSlots() + outItemStack.getSlots() || slot < inItemStack.getSlots())
				return ItemStack.EMPTY;
			if (!simulate)
				setChanged();
			return outItemStack.extractItem(slot - inItemStack.getSlots(), amount, simulate);
		}

		@Override
		public int getSlotLimit(int index)
		{
			if (index < inItemStack.getSlots())
				return inItemStack.getSlotLimit(index);
			else if (index < inItemStack.getSlots() + outItemStack.getSlots())
				return outItemStack.getSlotLimit(index - inItemStack.getSlots());
			else
				throw new IndexOutOfBoundsException();
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack)
		{
			return slot >= 0 && slot < inItemStack.getSlots();
		}
	}*/

	public LazyOptional<IFluidHandler> getFluidHandler()
	{
		return LazyOptional.of(
				()-> new CombinedFluidHandlerWrap(
						new FillOnlyFluidHandlerWrap(inFluidStack),
						new DrainOnlyFluidHandlerWrap(outFluidStack)
				)
		);
	}

	@Override
	@NotNull
	protected IItemHandler createUnSidedHandler()
	{
		return new CombinedInvWrapper(
			new InsertOnlyItemHandlerWrap(inItemStack),
			new ExtractOnlyItemHandlerWrap(outItemStack)
		);
	}


	@Override
	public void clearContent()
	{
		for (int i = 0; i < inItemStack.getSlots(); ++i)
			inItemStack.setStackInSlot(i, ItemStack.EMPTY);
		for (int i = 0; i < outItemStack.getSlots(); ++i)
			outItemStack.setStackInSlot(i, ItemStack.EMPTY);
		for (int i = 0; i < extraItemStack.getSlots(); ++i)
			extraItemStack.setStackInSlot(i, ItemStack.EMPTY);
		for (int i = 0; i < inFluidStack.getTanks(); ++i)
			inFluidStack.setFluid(i, FluidStack.EMPTY);
		for (int i = 0; i < outFluidStack.getTanks(); ++i)
			outFluidStack.setFluid(i, FluidStack.EMPTY);
		setChanged();
	}

	@Override
	public int getContainerSize()
	{
		return inItemStack.getSlots() + outItemStack.getSlots() + extraItemStack.getSlots();
	}

	public boolean isEmpty(boolean includeTanks)
	{
		for (int i = 0; i < inItemStack.getSlots(); ++i)
			if (!inItemStack.getStackInSlot(i).isEmpty())
				return false;
		for (int i = 0; i < outItemStack.getSlots(); ++i)
			if (!outItemStack.getStackInSlot(i).isEmpty())
				return false;
		for (int i = 0; i < extraItemStack.getSlots(); ++i)
			if (!extraItemStack.getStackInSlot(i).isEmpty())
				return false;
		return includeTanks ? (inFluidStack.isEmpty() && outFluidStack.isEmpty()) : true;
	}

	/**
	 * @return 仅考虑物品槽是否为空
	 */
	@Override
	public boolean isEmpty()
	{
		return isEmpty(false);
	}

	@Override
	public ItemStack getItem(int pSlot)
	{
		if (pSlot < 0 || pSlot >= getContainerSize())
			throw new IndexOutOfBoundsException("Try access " + pSlot + "th item, but there are only " + getContainerSize() + " items.");

		if (pSlot < inItemStack.getSlots())
			return inItemStack.getStackInSlot(pSlot);
		pSlot -= inItemStack.getSlots();

		if (pSlot < outItemStack.getSlots())
			return outItemStack.getStackInSlot(pSlot);
		pSlot -= outItemStack.getSlots();

		return extraItemStack.getStackInSlot(pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount)
	{
		var items = getItem(pSlot).copy();
		items.setCount(Math.min(items.getCount(), pAmount));
		getItem(pSlot).shrink(items.getCount());
		setChanged();
		return items;
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot)
	{
		var items = getItem(pSlot);
		setItem(pSlot, ItemStack.EMPTY);
		return items;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		var nbt = new CompoundTag();
		nbt.put("InputItems", inItemStack.serializeNBT());
		nbt.put("OutputItems", outItemStack.serializeNBT());
		nbt.put("ExtraItems", extraItemStack.serializeNBT());
		nbt.put("InputFluid", inFluidStack.serializeNBT());
		nbt.put("OutputFluid", outFluidStack.serializeNBT());

		if (workingRecipe != null)
			nbt.putString("WorkingRecipe", workingRecipe.getId().toString());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		inItemStack.deserializeNBT(nbt.getCompound("InputItems"));
		outItemStack.deserializeNBT(nbt.getCompound("OutputItems"));
		extraItemStack.deserializeNBT(nbt.getCompound("ExtraItems"));
		inFluidStack.deserializeNBT(nbt.getCompound("InputFluid"));
		outFluidStack.deserializeNBT(nbt.getCompound("OutputFluid"));

		workingRecipe = null;
		if (nbt.contains("WorkingRecipe"))
			loadRecipe = nbt.getString("WorkingRecipe");
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack)
	{
		//LOGGER.debug("pSlot = {}", pSlot);

		if (pSlot < 0 || pSlot >= getContainerSize())
			throw new IndexOutOfBoundsException("Try access " + pSlot + "th item, but there are only " + getContainerSize() + " items.");

		if (pSlot < inItemStack.getSlots())
		{
			inItemStack.setStackInSlot(pSlot, pStack);
			return;
		}
		pSlot -= inItemStack.getSlots();

		//LOGGER.debug("pSlot = {}", pSlot);

		if (pSlot < outItemStack.getSlots())
		{
			outItemStack.setStackInSlot(pSlot, pStack);
			return;
		}
		pSlot -= outItemStack.getSlots();

		//LOGGER.debug("pSlot = {}", pSlot);

		extraItemStack.setStackInSlot(pSlot, pStack);
		setChanged();
	}

	@Override
	public boolean stillValid(Player pPlayer)
	{
		return !this.remove && pPlayer.distanceToSqr(this.worldPosition.getCenter()) <= Config.playerUseMachineDistanceSqr;
	}

	private boolean insertItemAnyWhere(@NotNull OnChangedItemStackHandler handler, ItemStack itemStack, boolean simulate)
	{
		var is = itemStack.copy();
		for (int i = 0; i < handler.getSlots(); ++i)
		{
			if (ItemStack.isSameItemSameTags(handler.getStackInSlot(i), is))
				is = handler.insertItem(i, is, simulate);
			if (is.isEmpty())
			{
				if (!simulate)
					setChanged();
				return true;
			}
		}
		for (int i = 0; i < handler.getSlots(); ++i)
			if (handler.getStackInSlot(i).isEmpty())
			{
				if (!simulate)
				{
					handler.setStackInSlot(i, is);
					setChanged();
				}
				return true;
			}
		return false;
	}

/*	private boolean fillFluidAnyWhere(@NotNull IFluidHandler handle, FluidStack fluidStack, boolean simulate)
	{
		var needFill = fluidStack.copy();
		for (int i = 0; i < handle.getTanks(); i++)
		{
			var fluidInTank = handle.getFluidInTank(i);
			if (fluidInTank.isFluidEqual(needFill))
				needFill.shrink(fluidInTank.fill(needFill, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE));
			if (needFill.isEmpty())
			{
				if (!simulate)
					setChanged();
				return true;
			}
		}
		for (int i = 0; i < handle.getTanks(); i++)
		{
			var oft = handle.getFluidInTank(i);
			if (oft.isEmpty())
			{
				if (!simulate)
				{
					oft.setFluid(needFill);
					setChanged();
				}
				return true;
			}
		}
		return false;
	}*/

	public void dropItems(Level level, BlockPos pos)
	{
		var c = new SimpleContainer(inItemStack.getSlots());
		for (int i = 0; i < inItemStack.getSlots(); ++i)
			c.addItem(inItemStack.getStackInSlot(i));
		Containers.dropContents(level, pos, c);

		c = new SimpleContainer(outItemStack.getSlots());
		for (int i = 0; i < outItemStack.getSlots(); ++i)
			c.addItem(outItemStack.getStackInSlot(i));
		Containers.dropContents(level, pos, c);

		c = new SimpleContainer(extraItemStack.getSlots());
		for (int i = 0; i < extraItemStack.getSlots(); ++i)
			c.addItem(extraItemStack.getStackInSlot(i));
		Containers.dropContents(level, pos, c);
	}

	@Override
	public void fillStackedContents(StackedContents pContents)
	{

	}

	@Override
	public int[] getSlotsForFace(Direction pSide)
	{
		return new int[0];
	}

	@Override
	public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection)
	{
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection)
	{
		return false;
	}

	@Override
	public void setRecipeUsed(@Nullable Recipe<?> pRecipe)
	{
		if (pRecipe instanceof MachineRecipe r)
			workingRecipe = r;
	}

	@Override
	public @Nullable Recipe<?> getRecipeUsed()
	{
		return workingRecipe;
	}

	/**
	 * 本函数由Server调用，Client使用在注册表中注册的构造方式构造Menu
	 *
	 * @param pContainerId Container ID
	 * @param pInventory   玩家物品栏
	 * @return 创建的Menu
	 */
	@Override
	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory)
	{
		return new BaseMachineMenu(pContainerId, pInventory, this.machineType, this, this.data);
	}

	@Override
	@NotNull
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == ForgeCapabilities.FLUID_HANDLER && !this.remove)
			return lazyFluidHandler.cast();
		return super.getCapability(cap, side);
	}

	@Override
	@NotNull
	public Component getDisplayName()
	{
		return Component.translatable("block.seedstech." + machineType.getSerializedName());
	}

	@Override
	@NotNull
	protected Component getDefaultName()
	{
		return getDisplayName();
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		lazyFluidHandler = getFluidHandler();

		if (loadRecipe != null)
			level.getRecipeManager().byKey(ResourceLocation.parse(loadRecipe)).ifPresent((recipe) -> {
				if (recipe instanceof MachineRecipe r)
					workingRecipe = r;
			});
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		lazyFluidHandler.invalidate();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		progress = nbt.getInt("Progress");
		deserializeNBT(nbt.getCompound("Container"));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt)
	{
		nbt.putInt("Progress", progress);
		nbt.put("Container", serializeNBT());

		super.saveAdditional(nbt);
	}


	public void dropItems()
	{
		dropItems(level, worldPosition);
	}


	/**
	 * 为Recipe泛型参数提供的仅读包装类
	 */
	public class RecipeWrapper implements Container
	{
		@Override
		public void clearContent()
		{
		}

		@Override
		public int getContainerSize()
		{
			return INPUT_SLOT_COUNT;
		}

		@Override
		public boolean isEmpty()
		{
			return BaseMachineEntity.this.isEmpty(true);
		}

		@Override
		@NotNull
		public ItemStack getItem(int pSlot)
		{
			if (pSlot < 0 || pSlot >= getContainerSize())
			{
				LogUtils.getLogger().error("Access index pSlot {}, but only has {} elements.", pSlot, getContainerSize());
				return ItemStack.EMPTY;
			}
			return inItemStack.getStackInSlot(pSlot);
		}

		@Override
		@NotNull
		public ItemStack removeItem(int pSlot, int pAmount)
		{
			return ItemStack.EMPTY;
		}

		@Override
		@NotNull
		public ItemStack removeItemNoUpdate(int pSlot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public void setItem(int pSlot, @Nullable ItemStack pStack)
		{
		}

		@Override
		public int getMaxStackSize()
		{
			return 2048;
		}

		@Override
		public void setChanged()
		{
		}

		@Override
		public boolean stillValid(@Nullable Player pPlayer)
		{
			return false;
		}

		@Override
		public boolean canPlaceItem(int pIndex, @Nullable ItemStack pStack)
		{
			return false;
		}

		@Override
		public boolean canTakeItem(@Nullable Container pTarget, int pIndex, @NotNull ItemStack pStack)
		{
			return false;
		}

		// 以上均为重载Container函数

		boolean hasUpgrade(Object obj)
		{
			return false;
		}

		public int getTankCount()
		{
			return INPUT_SLOT_COUNT;
		}

		@NotNull
		public FluidStack getFluid(int pSlot)
		{
			if (pSlot < 0 || pSlot >= getTankCount())
			{
				LogUtils.getLogger().error("Access index pSlot {}, but only has {} elements.", pSlot, getTankCount());
				return FluidStack.EMPTY;
			}
			return inFluidStack.getFluidInTank(pSlot);
		}
	}

	private void removeInputs(MachineRecipe recipe)
	{
		int n, minN;
		if (recipe.inputItems != null)
			for (var pair : recipe.inputItems)
			{
				n = pair.getRight();
				for (int i = inItemStack.getSlots() - 1; i >= 0; --i)
					if (pair.getLeft().test(inItemStack.getStackInSlot(i)))
					{
						minN = Math.min(n, inItemStack.getStackInSlot(i).getCount());
						n -= minN;
						inItemStack.getStackInSlot(i).shrink(minN);
						if (n <= 0)
							break;
					}
				if (n > 0)
					throw new IllegalArgumentException("Method \"removeInputs\" must called when have enough inputs");
			}
		if (recipe.inputFluid != null)
			for (var inFluid : recipe.inputFluid)
			{
				n = inFluid.getAmount();
				for (int i = inFluidStack.getTanks() - 1; i >= 0; --i)
					if (inFluid.isFluidEqual(inFluidStack.getFluidInTank(i)))
					{
						minN = Math.min(n, inFluidStack.getFluidInTank(i).getAmount());
						n -= minN;
						inFluidStack.getFluidInTank(i).shrink(minN);
						if (n <= 0)
							break;
					}
				if (n > 0)
					throw new IllegalArgumentException("Method \"removeInputs\" must called when have enough inputs");
			}
	}

	private boolean insertOutputs(MachineRecipe recipe, boolean simulate)
	{
		if (recipe.outputItems != null)
			for (var oi : recipe.outputItems)
				if (!insertItemAnyWhere(outItemStack, oi, simulate))
					return false;
		if (recipe.outputFluid != null)
			for (var of : recipe.outputFluid)
				if (outFluidStack.fill(of, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE) < of.getAmount())
					return false;
		return true;
	}

	protected boolean tryStartProcess(Level level)
	{
		var rm = level.getRecipeManager();
		var recipes = rm.getRecipesFor(machineType.recipeType.get(), new RecipeWrapper(), level);
		if (recipes.size() != 1)
			return false;
		var r = recipes.get(0);
		if (!insertOutputs(r, true))
			return false;
		workingRecipe = r;
		removeInputs(workingRecipe);
		return true;
	}

	protected void doProcess()
	{
		++progress;
		// 需要减少能量
	}

	protected boolean isProcessFinished()
	{
		return progress >= workingRecipe.needTime;
	}

	protected void finishProcess()
	{
		progress = 0;
		insertOutputs(workingRecipe, false);
		workingRecipe = null;
	}

	public void serverTick(Level level, BlockPos pos, BlockState state)
	{
		if (workingRecipe != null)
		{
			doProcess();
			if (isProcessFinished())
			{
				finishProcess();
				tryStartProcess(level);
			}
		}
		// 若当前没在处理配方则尝试开始
		else
			// 若无法开始配方则直接返回
			if (!tryStartProcess(level))
				return;

		setChanged();
	}

	public void clientTick(Level level, BlockPos pos, BlockState state)
	{
		if (workingRecipe != null)
			doProcess();
	}
}

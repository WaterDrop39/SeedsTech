package org.seed.seedstech.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

public class FluidStackHandler implements IFluidHandler, INBTSerializable<CompoundTag>
{
	protected FluidStack[] fluids;
	protected IntUnaryOperator capacity;

	private FluidStackHandler(int size)
	{
		fluids = new FluidStack[size];
		this.capacity = null;
	}

	public FluidStackHandler(int size, IntUnaryOperator capacity)
	{
		fluids = new FluidStack[size];
		for (int i = 0; i < size; i++)
			fluids[i] = FluidStack.EMPTY.copy();

		this.capacity = capacity;
	}

	public FluidStackHandler(int size, int capacity)
	{
		this(size, (i) -> capacity);
	}

	public FluidStackHandler(boolean dummy, int... capacity)
	{
		this(capacity.length, (i) -> capacity[i]);
	}

	public int getSpace(int tank)
	{
		validateTankIndex(tank);
		return Math.max(capacity.applyAsInt(tank) - fluids[tank].getAmount(), 0);
	}

	/**
	 * Returns the number of fluid storage units ("tanks") available
	 *
	 * @return The number of tanks available
	 */
	@Override
	public int getTanks()
	{
		return fluids.length;
	}

	/**
	 * Returns the FluidStack in a given tank.
	 *
	 * <p>
	 * <strong>IMPORTANT:</strong> This FluidStack <em>MUST NOT</em> be modified. This method is not for
	 * altering internal contents. Any implementers who are able to detect modification via this method
	 * should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
	 * </p>
	 *
	 * <p>
	 * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLUIDSTACK</em></strong>
	 * </p>
	 *
	 * @param tank Tank to query.
	 * @return 指定位置的FluidStack的副本
	 */
	@Override
	public @NotNull FluidStack getFluidInTank(int tank)
	{
		validateTankIndex(tank);
		return fluids[tank].copy();
	}

	/**
	 * Retrieves the maximum fluid amount for a given tank.
	 *
	 * @param tank Tank to query.
	 * @return The maximum fluid amount held by the tank.
	 */
	@Override
	public int getTankCapacity(int tank)
	{
		validateTankIndex(tank);
		return capacity.applyAsInt(tank);
	}

	/**
	 * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
	 * basically always return TRUE for this.
	 *
	 * @param tank  Tank to query for validity
	 * @param stack Stack to test with for validity
	 * @return TRUE if the tank can hold the FluidStack, not considering current state.
	 * (Basically, is a given fluid EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
	 */
	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack)
	{
		return true;
	}

	/**
	 * 向指定tank填充amount mB的流体
	 *
	 * @param tank   指定tank, 不进行范围检查
	 * @param amount 流体体积
	 * @param action 是否为模拟
	 * @return 剩余的未能填充的流体量
	 */
	protected int fill(int tank, int amount, FluidAction action)
	{
		int filled = Math.min(getSpace(tank), amount);
		if (action.execute())
		{
			fluids[tank].grow(filled);
			onContentsChanged(tank);
		}
		return amount - filled;
	}

	/**
	 * 向指定tank填充的流体
	 *
	 * @param tank   指定tank
	 * @param fluid 流体
	 * @param action 是否为模拟
	 * @return 剩余的未能填充的流体量
	 */
	public int fill(int tank, FluidStack fluid, FluidAction action)
	{
		validateTankIndex(tank);
		if (fluids[tank].isFluidEqual(fluid))
			return fill(tank, fluid.getAmount(), action);
		return fluid.getAmount();
	}

	/**
	 * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
	 *
	 * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
	 * @param action   If SIMULATE, fill will only be simulated.
	 * @return Amount of resource that was (or would have been, if simulated) filled.
	 */
	@Override
	public int fill(@NotNull FluidStack resource, FluidAction action)
	{
		if (resource.isEmpty())
			return 0;
		int needFill = resource.getAmount();
		for (int i = 0; i < fluids.length; i++)
			if (fluids[i].isFluidEqual(resource))
				needFill = fill(i, needFill, action);

		return resource.getAmount() - needFill;
	}

	/**
	 * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
	 *
	 * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
	 * @param action   If SIMULATE, drain will only be simulated.
	 * @return FluidStack representing the Fluid and amount that was (or would have been, if
	 * simulated) drained.
	 */
	@Override
	@NotNull
	public FluidStack drain(FluidStack resource, FluidAction action)
	{
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		int needDrain = resource.getAmount();
		for (int i = fluids.length - 1; i >= 0; --i)
			if (fluids[i].isFluidEqual(resource))
			{
				int canDrain = Math.min(fluids[i].getAmount(), needDrain);
				if (action.execute())
				{
					fluids[i].shrink(canDrain);
					onContentsChanged(i);
				}
				needDrain -= canDrain;
			}
		return new FluidStack(resource, resource.getAmount() - needDrain);
	}

	/**
	 * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
	 * <p>
	 * This method is not Fluid-sensitive.
	 *
	 * @param maxDrain Maximum amount of fluid to drain.
	 * @param action   If SIMULATE, drain will only be simulated.
	 * @return 取出的流体FluidStack
	 */
	@Override
	@NotNull
	public FluidStack drain(int maxDrain, FluidAction action)
	{
		if (maxDrain <= 0)
			return FluidStack.EMPTY;
		for (int i = fluids.length - 1; i >= 0; --i)
			if (!fluids[i].isEmpty())
			{
				var fluid = fluids[i].copy();
				fluid.setAmount(maxDrain);
				return drain(fluid, action);
			}
		return FluidStack.EMPTY;
	}

	protected void validateTankIndex(int tank)
	{
		if (tank < 0 || tank >= getTanks())
			throw new IndexOutOfBoundsException(String.format("Tank %d not in valid range - [0,%d)", tank, getTanks()));
	}

	@Override
	public CompoundTag serializeNBT()
	{
		ListTag nbtTagList = new ListTag();
		for (int i = 0; i < fluids.length; i++)
		{
			CompoundTag fluidTag = new CompoundTag();
			fluidTag.putInt("Capacity", getTankCapacity(i));
			if (!fluids[i].isEmpty())
				fluids[i].writeToNBT(fluidTag);
			nbtTagList.add(fluidTag);
		}
		CompoundTag nbt = new CompoundTag();
		nbt.put("Tanks", nbtTagList);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		ListTag fluidList = nbt.getList("Tanks", CompoundTag.TAG_COMPOUND);
		int size = fluidList.size();

		fluids = new FluidStack[size];
		int[] cap = new int[size];
		for (int i = 0; i < size; ++i)
		{
			CompoundTag fluidNBT = fluidList.getCompound(i);
			fluids[i] = FluidStack.loadFluidStackFromNBT(fluidNBT);
			cap[i] = fluidNBT.getInt("Capacity");
		}

		boolean isSame = true;
		for (int k : cap)
			if (k != cap[0])
			{
				isSame = false;
				break;
			}

		if (isSame)
		{
			int c = cap[0];
			capacity = (i) -> c;
		}
		else
			capacity = (i) -> cap[i];

		onLoad();
	}

	public void writeToPacket(FriendlyByteBuf buf)
	{
		buf.writeVarInt(fluids.length);
		for (int i = 0; i < fluids.length; i++)
		{
			buf.writeVarInt(capacity.applyAsInt(i));
			fluids[i].writeToPacket(buf);
		}
	}

	public static FluidStackHandler readFromPacket(FriendlyByteBuf buf)
	{
		int size = buf.readVarInt();
		int[] cap = new int[size];
		var result = new FluidStackHandler(size);

		for (int i = 0; i < size; ++i)
		{
			cap[i] = buf.readVarInt();
			result.fluids[i] = FluidStack.readFromPacket(buf);
		}

		boolean isSame = true;
		for (int k : cap)
			if (k != cap[0])
			{
				isSame = false;
				break;
			}

		if (isSame)
		{
			int c = cap[0];
			result.capacity = (i) -> c;
		}
		else
			result.capacity = (i) -> cap[i];

		return result;
	}

	protected void onLoad()
	{

	}

	protected void onContentsChanged(int tank)
	{
	}

	public void setFluid(int tank, FluidStack stack)
	{
		validateTankIndex(tank);
		fluids[tank] = new FluidStack(stack, Math.min(capacity.applyAsInt(tank), stack.getAmount()));
		onContentsChanged(tank);
	}

	public boolean isEmpty(int tank)
	{
		validateTankIndex(tank);
		return fluids[tank].isEmpty();
	}

	public boolean isEmpty()
	{
		for (var fluid : fluids)
			if (!fluid.isEmpty())
				return false;
		return true;
	}

	public String getFluidAmountString(int tank)
	{
		validateTankIndex(tank);
		return Lib.getFluidAmountString(getFluidInTank(tank).getAmount());
	}
}

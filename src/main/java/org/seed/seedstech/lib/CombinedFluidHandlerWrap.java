package org.seed.seedstech.lib;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class CombinedFluidHandlerWrap implements IFluidHandler
{

	protected final IFluidHandler[] handlers;

	public CombinedFluidHandlerWrap(IFluidHandler... handlers)
	{
		this.handlers = handlers;
	}

	protected Pair<IFluidHandler, Integer> getHandler(int index)
	{
		for (IFluidHandler handler : handlers)
			if (index - handler.getTanks() < 0)
				return Pair.of(handler, index);
			else
				index -= handler.getTanks();
		return null;
	}

	/**
	 * Returns the number of fluid storage units ("tanks") available
	 *
	 * @return The number of tanks available
	 */
	@Override
	public int getTanks()
	{
		int tot = 0;
		for (var handler : handlers)
			tot += handler.getTanks();
		return tot;
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
	 * @return FluidStack in a given tank. FluidStack.EMPTY if the tank is empty.
	 */
	@Override
	public @NotNull FluidStack getFluidInTank(int tank)
	{
		validateTankIndex(tank);
		var p = getHandler(tank);
		return p.getLeft().getFluidInTank(p.getRight());
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
		var p = getHandler(tank);
		return p.getLeft().getTankCapacity(p.getRight());
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
		validateTankIndex(tank);
		var p = getHandler(tank);
		return p.getLeft().isFluidValid(p.getRight(), stack);
	}

	/**
	 * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
	 *
	 * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
	 * @param action   If SIMULATE, fill will only be simulated.
	 * @return Amount of resource that was (or would have been, if simulated) filled.
	 */
	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		var needFill = resource.getAmount();
		for (var handle : handlers)
		{
			needFill -= handle.fill(new FluidStack(resource, needFill), action);
			if (needFill <= 0)
				break;
		}
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
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
	{
		var needDrain = resource.getAmount();
		for (int i = handlers.length - 1; i >= 0; i--)
		{
			needDrain -= handlers[i].drain(new FluidStack(resource, needDrain), action).getAmount();
			if (needDrain <= 0)
				break;
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
	 * @return FluidStack representing the Fluid and amount that was (or would have been, if
	 * simulated) drained.
	 */
	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action)
	{
		for (int i = handlers.length - 1; i >= 0; i--)
		{
			FluidStack f = handlers[i].drain(maxDrain, FluidAction.SIMULATE);
			if (!f.isEmpty())
				return drain(new FluidStack(f, maxDrain), action);
		}
		return FluidStack.EMPTY;
	}

	protected void validateTankIndex(int tank)
	{
		if (tank < 0 || tank >= getTanks())
			throw new IndexOutOfBoundsException(String.format("Tank %d not in valid range - [0,%d)", tank, getTanks()));
	}
}

package org.seed.seedstech.recipes;

import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.seed.seedstech.blockentities.BaseMachineEntity;
import org.seed.seedstech.blocks.BaseMachineDefinition;

public class MachineRecipe implements Recipe<BaseMachineEntity.RecipeWrapper>
{
	public final ResourceLocation id;
	public final BaseMachineDefinition machineType;
	public final Pair<Ingredient, Integer>[] inputItems;
	public final ItemStack[] outputItems;
	public final FluidStack[] inputFluid;
	public final FluidStack[] outputFluid;
	public final int energyPerTick, needTime;

	public MachineRecipe(ResourceLocation id,
	                     BaseMachineDefinition type,
	                     Pair<Ingredient, Integer>[] inputItems,
	                     ItemStack[] outputItems,
	                     FluidStack[] inputFluid,
	                     FluidStack[] outputFluid,
	                     int energyPerTick, int needTime)
	{
		this.id = id;
		this.machineType = type;
		this.inputItems = inputItems;
		this.outputItems = outputItems;
		this.inputFluid = inputFluid;
		this.outputFluid = outputFluid;
		this.energyPerTick = energyPerTick;
		this.needTime = needTime;

		LogUtils.getLogger().info(
				"Load recipe {}, input {}, output {}, needTime {}",
				this.id.toString(),
				inputItems[0].getLeft().getItems()[0].getDisplayName().getString(),
				outputItems[0].getDisplayName().getString(),
				this.needTime
				);
	}

	@Override
	public boolean matches(@NotNull BaseMachineEntity.RecipeWrapper pContainer, Level pLevel)
	{
		int n;
		if (inputItems != null)
			for (var pair : inputItems)
			{
				n = pair.getRight();
				for (int i = 0; i < pContainer.getContainerSize(); ++i)
					if (pair.getLeft().test(pContainer.getItem(i)))
					{
						n -= pContainer.getItem(i).getCount();
						if (n <= 0)
							break;
					}
				if (n > 0)
					return false;
			}
		if (inputFluid != null)
			for (var inFluid : inputFluid)
			{
				n = inFluid.getAmount();
				for (int i = 0; i < pContainer.getTankCount(); ++i)
					if (inFluid.isFluidEqual(pContainer.getFluid(i)))
					{
						n -= pContainer.getFluid(i).getAmount();
						if (n <= 0)
							break;
					}
				if (n > 0)
					return false;
			}
		return true;
	}

	@Override
	public ItemStack assemble(BaseMachineEntity.RecipeWrapper pContainer, RegistryAccess pRegistryAccess)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight)
	{
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess pRegistryAccess)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return machineType.recipeSerializer.get();
	}

	@Override
	public RecipeType<?> getType()
	{
		return machineType.recipeType.get();
	}
}
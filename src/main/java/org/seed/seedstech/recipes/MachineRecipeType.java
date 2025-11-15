package org.seed.seedstech.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import org.seed.seedstech.SeedsTech;

public class MachineRecipeType
{
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTER =
			DeferredRegister.create(Registries.RECIPE_TYPE, SeedsTech.MODID);

	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER =
			DeferredRegister.create(Registries.RECIPE_SERIALIZER, SeedsTech.MODID);
}

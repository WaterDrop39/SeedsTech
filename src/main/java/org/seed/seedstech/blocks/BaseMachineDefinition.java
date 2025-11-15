package org.seed.seedstech.blocks;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.seed.seedstech.blockentities.BaseMachineEntity;
import org.seed.seedstech.menus.BaseMachineMenu;
import org.seed.seedstech.recipes.MachineRecipe;
import org.seed.seedstech.recipes.MachineRecipeSerializer;
import org.seed.seedstech.recipes.MachineRecipeType;
import org.seed.seedstech.registies.CommonRegisty;

import java.util.HashMap;
import java.util.Map;

public class BaseMachineDefinition implements StringRepresentable
{
	private static final Map<String, BaseMachineDefinition> stringToMachine = new HashMap<>();

	public static final BaseMachineDefinition CASING = new BaseMachineDefinition("casing");
	public static final BaseMachineDefinition MACERATOR = new BaseMachineDefinition("macerator");
	public static final BaseMachineDefinition ORE_WASHER = new BaseMachineDefinition("ore_washer");

	private final String serializedName;

	public final RegistryObject<BaseMachine> block;
	public final RegistryObject<? extends Item> item;
	public final RegistryObject<RecipeType<MachineRecipe>> recipeType;
	public final RegistryObject<RecipeSerializer<MachineRecipe>> recipeSerializer;
	public final RegistryObject<BlockEntityType<BaseMachineEntity>> blockEntityType;
	public final RegistryObject<MenuType<BaseMachineMenu>> menuType;

	BaseMachineDefinition(String name)
	{
		serializedName = name;
		recipeType = MachineRecipeType.RECIPE_TYPE_REGISTER.register(name, () -> new RecipeType<>()
		{
			@Override
			public String toString()
			{
				return serializedName;
			}
		});
		recipeSerializer = MachineRecipeType.RECIPE_SERIALIZER_REGISTER.register(name, () -> new MachineRecipeSerializer(this));
		block = CommonRegisty.registerBlock(name, () -> new BaseMachine(this));
		item = CommonRegisty.registerBlockItem(name, block, new Item.Properties());
		blockEntityType = CommonRegisty.registerBlockEntity(name, block, (pos, state) -> new BaseMachineEntity(pos, state, this));
		menuType = CommonRegisty.registerMenuType(name, (pContainerId, pPlayerInventory) -> new BaseMachineMenu(pContainerId, pPlayerInventory, this));

		stringToMachine.put(serializedName, this);
	}

	public static BaseMachineDefinition getByName(String name)
	{
		return stringToMachine.get(name);
	}

	@Override @NotNull
	public String getSerializedName()
	{
		return serializedName;
	}

	public static void registerMachines()
	{
	}
}

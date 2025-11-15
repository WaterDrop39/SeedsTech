package org.seed.seedstech.recipes;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.seed.seedstech.blocks.BaseMachineDefinition;

import java.util.ArrayList;

public class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe>
{
	private final BaseMachineDefinition machineType;

	public MachineRecipeSerializer(BaseMachineDefinition machineType)
	{
		super();
		this.machineType = machineType;
	}

	private static FluidStack parseFluid(JsonObject jsonObject)
	{
		var fs = new FluidStack(
				ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(jsonObject.get("fluid").getAsString())),
				jsonObject.get("amount").getAsInt()
		);
		if (jsonObject.has("nbt"))
			fs.setTag((CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, jsonObject.get("nbt")));
		return fs;
	}

	@Override
	public MachineRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe)
	{
		LogUtils.getLogger().info("fromJson run");

		var inFluid = new ArrayList<FluidStack>();
		var outFluid = new ArrayList<FluidStack>();
		var inItems = new ArrayList<Pair<Ingredient, Integer>>();
		var outItems = new ArrayList<ItemStack>();

		var inputs = GsonHelper.getAsJsonArray(pSerializedRecipe, "input");
		for (int i = 0; i < inputs.size(); ++i)
		{
			var jo = inputs.get(i).getAsJsonObject();
			if (jo.has("fluid"))
				inFluid.add(parseFluid(jo));
			else
				inItems.add(Pair.of(
						Ingredient.fromJson(jo, false),
						GsonHelper.getAsInt(jo, "count")
				));
		}

		var outputs = GsonHelper.getAsJsonArray(pSerializedRecipe, "output");
		for (int i = 0; i < outputs.size(); ++i)
		{
			var jo = outputs.get(i).getAsJsonObject();
			if (jo.has("fluid"))
				outFluid.add(parseFluid(jo));
			else
				outItems.add(ShapedRecipe.itemStackFromJson(jo));
		}

		return new MachineRecipe(
				pRecipeId,
				machineType,
				inItems.toArray(Pair[]::new),
				outItems.toArray(new ItemStack[0]),
				inFluid.toArray(new FluidStack[0]),
				outFluid.toArray(new FluidStack[0]),
				GsonHelper.getAsInt(pSerializedRecipe, "energy"),
				GsonHelper.getAsInt(pSerializedRecipe, "time")
		);
	}

	@Override
	public @Nullable MachineRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
	{
		var inFluid = new ArrayList<FluidStack>();
		var outFluid = new ArrayList<FluidStack>();
		var inItems = new ArrayList<Pair<Ingredient, Integer>>();
		var outItems = new ArrayList<ItemStack>();

		var energy = pBuffer.readInt();
		var time = pBuffer.readInt();

		var l = pBuffer.readInt();
		for (int i = 0; i < l; ++i)
			inItems.add(Pair.of(Ingredient.fromNetwork(pBuffer), pBuffer.readInt()));

		l = pBuffer.readInt();
		for (int i = 0; i < l; ++i)
			inFluid.add(pBuffer.readFluidStack());

		l = pBuffer.readInt();
		for (int i = 0; i < l; ++i)
			outItems.add(pBuffer.readItem());

		l = pBuffer.readInt();
		for (int i = 0; i < l; ++i)
			outFluid.add(pBuffer.readFluidStack());

		return new MachineRecipe(
				pRecipeId,
				machineType,
				inItems.toArray(new Pair[0]),
				outItems.toArray(new ItemStack[0]),
				inFluid.toArray(new FluidStack[0]),
				outFluid.toArray(new FluidStack[0]),
				energy,
				time
		);
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, MachineRecipe recipe)
	{
		pBuffer.writeInt(recipe.energyPerTick);
		pBuffer.writeInt(recipe.needTime);

		pBuffer.writeInt(recipe.inputItems.length);
		for (var p : recipe.inputItems)
		{
			p.getLeft().toNetwork(pBuffer);
			pBuffer.writeInt(p.getRight());
		}

		pBuffer.writeInt(recipe.inputFluid.length);
		for (var f : recipe.inputFluid)
			pBuffer.writeFluidStack(f);

		pBuffer.writeInt(recipe.outputItems.length);
		for (var i : recipe.outputItems)
			pBuffer.writeItemStack(i, false);

		pBuffer.writeInt(recipe.outputFluid.length);
		for (var f : recipe.outputFluid)
			pBuffer.writeFluidStack(f);
	}
}

package org.seed.seedstech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SeedsTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
			.comment("Whether to log the dirt block on common setup")
			.define("logDirtBlock", true);

	public static final ForgeConfigSpec.IntValue SINGLE_BLOCK_FLUID_VOLUME = BUILDER
			.translation("singleBlockFluidVolume")
			.defineInRange("singleBlockFluidVolume", 8000, 1000, 64000);

	public static final ForgeConfigSpec.DoubleValue PLAYER_USE_MACHINE_DISTANCE = BUILDER
			.translation("playerUseMachineDistance")
			.defineInRange("playerUseMachineDistance", 8.0, 0.0, 64.0);

	public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
			.comment("What you want the introduction message to be for the magic number")
			.define("magicNumberIntroduction", "The magic number is... ");

	// a list of strings that are treated as resource locations for items
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
			.comment("A list of items to log on common setup.")
			.defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

	static final ForgeConfigSpec SPEC = BUILDER.build();

	public static boolean logDirtBlock;
	public static int singleBlockFluidVolume;
	public static double playerUseMachineDistanceSqr;
	public static String magicNumberIntroduction;
	public static Set<Item> items;

	private static boolean validateItemName(final Object obj)
	{
		return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(itemName));
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event)
	{
		logDirtBlock = LOG_DIRT_BLOCK.get();
		singleBlockFluidVolume = SINGLE_BLOCK_FLUID_VOLUME.get();
		playerUseMachineDistanceSqr = Mth.square(PLAYER_USE_MACHINE_DISTANCE.get());
		magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

		// convert the list of strings into a set of items
		items = ITEM_STRINGS.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
				.collect(Collectors.toSet());
	}
}

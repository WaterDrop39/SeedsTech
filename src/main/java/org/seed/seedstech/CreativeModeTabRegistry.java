package org.seed.seedstech;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.seed.seedstech.registies.CommonRegisty;

import java.util.LinkedList;
import java.util.function.Supplier;

public class CreativeModeTabRegistry
{
	public static final String TAB_NAME = "seedstech_creative_tab";
	public static final String TAB_REGISTER_NAME = "creativetab." + TAB_NAME;

	private static final DeferredRegister<CreativeModeTab> REGISTRT = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SeedsTech.MODID);

	private static LinkedList<Supplier<? extends Item>> items = new LinkedList<>();

	public static <T extends Item> void addItem(Supplier<T> item)
	{
		items.addLast(item);
	}

	private static void addItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output)
	{
		for (var item : items)
			output.accept(item.get());
		// release memory
		// items = null;
	}

	public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = REGISTRT.register("seedstech_creative_tab",
			() -> CreativeModeTab.builder().icon(() -> new ItemStack(CommonRegisty.STEEL_INGOT.get()))
					.title(Component.translatable(TAB_REGISTER_NAME))
					.displayItems(CreativeModeTabRegistry::addItems)
					.build()
	);

	public static void register(IEventBus bus)
	{
		REGISTRT.register(bus);
	}
}

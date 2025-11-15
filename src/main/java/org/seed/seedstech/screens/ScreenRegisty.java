package org.seed.seedstech.screens;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import org.seed.seedstech.menus.BaseMachineMenu;

import java.util.HashMap;
import java.util.Map;

public class ScreenRegisty
{
	private static final Map<RegistryObject<MenuType<BaseMachineMenu>>, MenuScreens.ScreenConstructor<AbstractContainerMenu, AbstractContainerScreen<AbstractContainerMenu>>> map = new HashMap<>();

	public static void registerScreen()
	{

	}

	public static void register(IEventBus bus)
	{

	}
}

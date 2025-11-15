package org.seed.seedstech.registies;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.types.Type;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.seed.seedstech.CreativeModeTabRegistry;
import org.seed.seedstech.SeedsTech;
import org.seed.seedstech.recipes.MachineRecipeType;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonRegisty
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SeedsTech.MODID);
	public static final DeferredRegister<Block> BLOCK_REGISTER =
			DeferredRegister.create(Registries.BLOCK, SeedsTech.MODID);
	public static final DeferredRegister<Item> ITEM_REGISTER =
			DeferredRegister.create(Registries.ITEM, SeedsTech.MODID);
	public static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER =
			DeferredRegister.create(Registries.MENU, SeedsTech.MODID);

	public static final RegistryObject<Item> STEEL_INGOT = registerItem("steel_ingot");

	// Register Block Entity

	public static <B extends Block, T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(
			String name,
			RegistryObject<B> block,
			BlockEntityType.BlockEntitySupplier<T> constructor,
			@Nullable DataFixer fixer
	)
	{
		return BLOCK_ENTITY_TYPE_REGISTER.register(name,
				() -> BlockEntityType.Builder.of(constructor, block.get()).build((Type<?>) fixer)
		);
	}

	public static <B extends Block, T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(
			String name,
			RegistryObject<B> block,
			BlockEntityType.BlockEntitySupplier<T> constructor
	)
	{
		return registerBlockEntity(name, block, constructor, null);
	}

	// Register Block Item

	public static <T extends BlockItem> RegistryObject<? extends Item> registerBlockItem(String name, Supplier<T> blockItem)
	{
		var item = ITEM_REGISTER.register(name, blockItem);
		CreativeModeTabRegistry.addItem(item);
		return item;
	}

	public static <T extends Block> RegistryObject<? extends Item> registerBlockItem(String name, Supplier<T> block, Item.Properties properties)
	{
		return registerBlockItem(name, () -> new BlockItem(block.get(), properties));
	}

	// Register Block

	public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> constructor, Item.Properties properties)
	{
		return BLOCK_REGISTER.register(name, constructor);
	}

	public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> constructor)
	{
		return registerBlock(name, constructor, new Item.Properties());
	}

	public static RegistryObject<Block> registerBlock(String name, BlockBehaviour.Properties bp, Item.Properties properties)
	{
		return registerBlock(name, () -> new Block(bp), properties);
	}

	public static RegistryObject<Block> registerBlock(String name, BlockBehaviour.Properties bp)
	{
		return registerBlock(name, bp, new Item.Properties());
	}

	// Register Entity Block

	public static <T extends Block, E extends BlockEntity> RegistryObject<T> registerEntityBlock(
			String name,
			Supplier<T> blockConstructor,
			BlockEntityType.BlockEntitySupplier<E> blockEntityConstructor,
			Consumer<RegistryObject<BlockEntityType<E>>> blockEntitySetter,
			Item.Properties itemProperties,
			@Nullable DataFixer fixer
	)
	{
		var block = BLOCK_REGISTER.register(name, blockConstructor);
		registerBlockItem(name, block, itemProperties);
		blockEntitySetter.accept(registerBlockEntity(name, block, blockEntityConstructor, fixer));
		return block;
	}

	public static <T extends Block, E extends BlockEntity> RegistryObject<T> registerEntityBlock(
			String name,
			Supplier<T> blockConstructor,
			BlockEntityType.BlockEntitySupplier<E> blockEntityConstructor,
			Consumer<RegistryObject<BlockEntityType<E>>> blockEntitySetter,
			Item.Properties itemProperties
	)
	{
		return registerEntityBlock(name, blockConstructor, blockEntityConstructor, blockEntitySetter, itemProperties, null);
	}

	public static <T extends Block, E extends BlockEntity> RegistryObject<T> registerEntityBlock(
			String name,
			Supplier<T> blockConstructor,
			BlockEntityType.BlockEntitySupplier<E> blockEntityConstructor,
			Consumer<RegistryObject<BlockEntityType<E>>> blockEntitySetter,
			@Nullable DataFixer fixer
	)
	{
		return registerEntityBlock(name, blockConstructor, blockEntityConstructor, blockEntitySetter, new Item.Properties(), fixer);
	}

	public static <T extends Block, E extends BlockEntity> RegistryObject<T> registerEntityBlock(
			String name,
			Supplier<T> blockConstructor,
			BlockEntityType.BlockEntitySupplier<E> blockEntityConstructor,
			Consumer<RegistryObject<BlockEntityType<E>>> blockEntitySetter
	)
	{
		return registerEntityBlock(name, blockConstructor, blockEntityConstructor, blockEntitySetter, new Item.Properties(), null);
	}

	// Register Item

	public static RegistryObject<Item> registerItem(String name, Item.Properties properties)
	{
		var item = ITEM_REGISTER.register(name, () -> new Item(properties));
		CreativeModeTabRegistry.addItem(item);
		return item;
	}

	public static RegistryObject<Item> registerItem(String name)
	{
		return registerItem(name, new Item.Properties());
	}

	/**
	 * 不带{@link net.minecraft.network.FriendlyByteBuf}的Menu构造函数
	 * @param name 注册名
	 * @param pConstructor Menu的构造函数
	 * @return 待注册的MenuType
	 * @param <T> Menu类型
	 */
	public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(final String name, MenuType.MenuSupplier<T> pConstructor)
	{
		return MENU_TYPE_REGISTER.register(name, () -> new MenuType<>(pConstructor, FeatureFlags.DEFAULT_FLAGS));
	}

	/**
	 * 带{@link net.minecraft.network.FriendlyByteBuf}的Menu构造函数
	 * @param name 注册名
	 * @param factory Menu的构造函数
	 * @return 待注册的MenuType
	 * @param <T> Menu类型
	 */
	public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory)
	{
		return MENU_TYPE_REGISTER.register(name, () -> IForgeMenuType.create(factory));
	}

	public static void register(IEventBus bus)
	{
		ITEM_REGISTER.register(bus);
		BLOCK_REGISTER.register(bus);
		BLOCK_ENTITY_TYPE_REGISTER.register(bus);
		CreativeModeTabRegistry.register(bus);
		MachineRecipeType.RECIPE_SERIALIZER_REGISTER.register(bus);
		MachineRecipeType.RECIPE_TYPE_REGISTER.register(bus);
		MENU_TYPE_REGISTER.register(bus);
	}
}

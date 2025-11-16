package org.seed.seedstech.menus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.seed.seedstech.blockentities.BaseMachineEntity;
import org.seed.seedstech.blocks.BaseMachineDefinition;
import org.seed.seedstech.screens.BaseMachineScreen;
import org.seed.seedstech.screens.FluidSlot;

import java.util.Optional;

import static org.seed.seedstech.blockentities.BaseMachineEntity.DATA_MAX_INDEX;

public class BaseMachineMenu extends AbstractContainerMenu
{
	public static final int BORDER_HEIGHT = 3;
	public static final int BORDER_WIDTH = 3;
	public static final int PADDING_HEIGHT = 4;
	public static final int PADDING_WIDTH = 4;
	public static final int TEXT_PADDING_HEIGHT = 3;
	public static final int TEXT_OFFSET_WIDTH = 1;
	public static final int TEXT_HEIGHT = 8;
	// TEXT_WIDTH depends on text
	public static final int SLOT_BORDER_HEIGHT = 1;
	public static final int SLOT_BORDER_WIDTH = 1;
	public static final int SLOT_HEIGHT = 16;
	public static final int SLOT_WIDTH = 16;
	public static final int SLOT_WITH_BORDER_HEIGHT = SLOT_BORDER_HEIGHT + SLOT_HEIGHT + SLOT_BORDER_HEIGHT;
	public static final int SLOT_WITH_BORDER_WIDTH = SLOT_BORDER_WIDTH + SLOT_WIDTH + SLOT_BORDER_WIDTH;
	public static final int BAG_HEIGHT = SLOT_WITH_BORDER_HEIGHT * 3;
	public static final int BAG_WIDTH = SLOT_WITH_BORDER_WIDTH * 9;
	public static final int INVENTORY_HEIGHT = BAG_HEIGHT + PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT;
	public static final int INVENTORY_WIDTH = BAG_WIDTH;
	public static final int INVENTORY_WITH_LABEL_HEIGHT = INVENTORY_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT;
	public static final int INVENTORY_WITH_LABEL_WIDTH = BAG_WIDTH;

	public static final int DEFAULT_ARROW_OFFSET_X = 79;
	public static final int DEFAULT_ARROW_OFFSET_Y = 34;
	public static final int LIGHT_ARROW_OFFSET_X = 176;
	public static final int LIGHT_ARROW_OFFSET_Y = 14;
	public static final int ARROW_WIDTH = 24;
	public static final int ARROW_HEIGHT = 16;

	public final BaseMachineDefinition machineType;
	public final Container container;
	public final ContainerData data;

	/**
	 * 构造函数, 客户端
	 *
	 * @param pContainerId Container ID
	 * @param playerInv    玩家物品栏
	 * @param machineType  机器类型
	 */
	public BaseMachineMenu(int pContainerId, Inventory playerInv, BaseMachineDefinition machineType)
	{
		this(pContainerId, playerInv, machineType, new SimpleContainer(24 + 18), new SimpleContainerData(DATA_MAX_INDEX + 1));
	}

	/**
	 * 构造函数, 服务器端
	 *
	 * @param pContainerId 该Menu的ID
	 * @param playerInv    玩家物品栏
	 * @param machineType  机器类型
	 * @param container    容器
	 * @param data         传输数据
	 */
	public BaseMachineMenu(
			int pContainerId,
			Inventory playerInv,
			BaseMachineDefinition machineType,
			Container container,
			ContainerData data
	)
	{
		super(machineType.menuType.get(), pContainerId);
		this.machineType = machineType;
		this.container = container;
		this.data = data;

		// 1. 添加机器自身槽位（物品输入+输出）
		addMachineSlots();

		// 2. 添加玩家背包和快捷栏槽位
		addInventorySlots(playerInv);

		// 3. 监听 BlockEntity 数据变化，自动同步到客户端
		addDataSlots(data);
		//blockEntity.addPropertyChangeListener(this::onBlockEntityDataChange);
	}

	/**
	 * 获取BlockEntity, 在Server上可正常获取, 在Client上为空
	 *
	 * @return {@link BaseMachineEntity}
	 */
	public Optional<BaseMachineEntity> getBlockEntity()
	{
		if (container instanceof BaseMachineEntity e)
			return Optional.of(e);
		return Optional.empty();
	}

	// ------------------------------
	// 1. 添加机器槽位（物品+流体逻辑关联）
	// ------------------------------
	private void addMachineSlots()
	{
		for (int i = 0; i < container.getContainerSize(); ++i)
		{
			var rect = getSlotRectangle(i);
			addSlot(new Slot(container, i, rect.x + SLOT_BORDER_WIDTH, rect.y + SLOT_BORDER_HEIGHT));
		}
	}

	// ------------------------------
	// 2. 添加玩家槽位（标准布局）
	// ------------------------------
	private void addInventorySlots(Inventory inventory)
	{
		for (int i = 0; i < 9; i++)
			this.addSlot(new Slot(inventory, i,
					getScreenInventoryX() + SLOT_WITH_BORDER_WIDTH * i + SLOT_BORDER_WIDTH,
					getScreenInventoryY() + SLOT_WITH_BORDER_HEIGHT * 3 + PADDING_HEIGHT + SLOT_BORDER_HEIGHT
			));

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new Slot(inventory, 9 + i * 9 + j,
						getScreenInventoryX() + SLOT_WITH_BORDER_WIDTH * j + SLOT_BORDER_WIDTH,
						getScreenInventoryY() + SLOT_WITH_BORDER_HEIGHT * i + SLOT_BORDER_HEIGHT
				));
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex)
	{

		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return container.stillValid(player);
	}

/*
	// ------------------------------
	// 5. GUI 打开/关闭监听（startOpen/stopOpen）
	// ------------------------------
	@Override
	public void startOpen(Player player) {
		super.startOpen(player);
		if (!level.isClientSide) {
			blockEntity.setLocked(true);
			// 同步初始数据到客户端
			syncDataToClient();
		}
	}

	@Override
	public void stopOpen(Player player) {
		super.stopOpen(player);
		if (!level.isClientSide) {
			blockEntity.setLocked(false);
			blockEntity.setChanged();
		}
	}*/

	// ------------------------------
	// 6. 数据同步（服务器→客户端）
	// ------------------------------
	// BlockEntity 数据变化时触发
/*	private void onBlockEntityDataChange()
	{
		if (!level.isClientSide)
		{
			syncDataToClient();
		}
	}*/

	// 同步关键数据到客户端（加工进度、流体量）
	private void syncDataToClient()
	{
		/*this.processProgress = blockEntity.getProcessProgress();
		this.processTime = blockEntity.getProcessTime();
		this.fluidInputStack = blockEntity.getFluidTanks()[0].getFluid().copy();
		this.fluidOutputStack = blockEntity.getFluidTanks()[1].getFluid().copy();

		// 发送数据到客户端（通过 Menu 的数据列表）
		for (Player player : players) {
			if (player instanceof ServerPlayer serverPlayer) {
				sendDataToPlayer(serverPlayer);
			}
		}*/
	}

	// 向单个玩家发送数据
	private void sendDataToPlayer(ServerPlayer player)
	{
		/*FriendlyByteBuf buf = this.getOutputBuffer();
		buf.writeInt(processProgress);
		buf.writeInt(processTime);
		buf.writeFluidStack(fluidInputStack);
		buf.writeFluidStack(fluidOutputStack);
		sendData(player, this, buf);*/
	}

	/*
	// 客户端接收服务器数据
	@Override
	public void dataChanged(int id, FriendlyByteBuf buf) {
		super.dataChanged(id, buf);
		this.processProgress = buf.readInt();
		this.processTime = buf.readInt();
		this.fluidInputStack = buf.readFluidStack();
		this.fluidOutputStack = buf.readFluidStack();
	}

	 */

	public int getScreenWidth()
	{
		return BORDER_WIDTH + PADDING_WIDTH + SLOT_WITH_BORDER_WIDTH * 4 + PADDING_WIDTH + ARROW_WIDTH + PADDING_WIDTH + SLOT_WITH_BORDER_WIDTH * 4 + PADDING_WIDTH + BORDER_WIDTH;
	}

	public int getScreenHeight()
	{
		return BORDER_HEIGHT +
				TEXT_PADDING_HEIGHT +
				TEXT_HEIGHT +                 // Machine title
				TEXT_PADDING_HEIGHT +
				SLOT_WITH_BORDER_HEIGHT * 3 +      // Item slot
				PADDING_HEIGHT +
				SLOT_WITH_BORDER_HEIGHT * 3 +      // Fluid slot
				PADDING_HEIGHT +
				SLOT_WITH_BORDER_HEIGHT * 2 +      // Extra Item slot
				TEXT_PADDING_HEIGHT +
				INVENTORY_WITH_LABEL_HEIGHT + // Player inventory
				PADDING_HEIGHT +
				BORDER_HEIGHT;
	}

	public int getScreenInventoryX()
	{
		return (getScreenWidth() - SLOT_WITH_BORDER_WIDTH * 9) / 2;
	}

	public int getScreenInventoryY()
	{
		return getScreenHeight() - BORDER_HEIGHT - PADDING_HEIGHT - INVENTORY_HEIGHT;
	}

	public BaseMachineScreen.Rectangle getSlotRectangle(int slot)
	{
		if (slot < 0 || slot >= container.getContainerSize())
			throw new IndexOutOfBoundsException(slot);

		if (slot < 12)
			return new BaseMachineScreen.Rectangle(
					BORDER_WIDTH + PADDING_WIDTH + SLOT_WITH_BORDER_WIDTH * (slot & 3),
					BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * (slot >> 2),
					SLOT_WITH_BORDER_WIDTH,
					SLOT_WITH_BORDER_HEIGHT
			);
		slot -= 12;

		if (slot < 12)
			return new BaseMachineScreen.Rectangle(
					getScreenWidth() - BORDER_WIDTH - PADDING_WIDTH - SLOT_WITH_BORDER_WIDTH * 4 + SLOT_WITH_BORDER_WIDTH * (slot & 3),
					BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * (slot >> 2),
					SLOT_WITH_BORDER_WIDTH,
					SLOT_WITH_BORDER_HEIGHT
			);
		slot -= 12;

		return new BaseMachineScreen.Rectangle(
				getScreenInventoryX() + SLOT_WITH_BORDER_WIDTH * (slot % 9),
				BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * 6 + PADDING_HEIGHT * 2 + SLOT_WITH_BORDER_HEIGHT * (slot / 9),
				SLOT_WITH_BORDER_WIDTH,
				SLOT_WITH_BORDER_HEIGHT
		);
	}

	/**
	 * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default. Called
	 * to check if the slot can be added to a list of Slots to split the held ItemStack across.
	 *
	 * @param pSlot
	 */
	@Override
	public boolean canDragTo(Slot pSlot)
	{
		return !(pSlot instanceof FluidSlot);
	}
}
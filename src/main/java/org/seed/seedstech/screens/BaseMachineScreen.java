package org.seed.seedstech.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.seed.seedstech.SeedsTech;
import org.seed.seedstech.menus.BaseMachineMenu;

import static org.seed.seedstech.menus.BaseMachineMenu.*;
import static org.seed.seedstech.blockentities.BaseMachineEntity.DATA_PROCESS_INDEX;
import static org.seed.seedstech.blockentities.BaseMachineEntity.DATA_PROCESS_NEED_TIME_INDEX;

public class BaseMachineScreen extends AbstractContainerScreen<BaseMachineMenu>
{
	protected static final ResourceLocation TEXTURE_BASE_LOCATION =
			ResourceLocation.fromNamespaceAndPath(SeedsTech.MODID, "gui_base.png");
	protected static final ResourceLocation TEXTURE_ARROW_LOCATION =
			ResourceLocation.fromNamespaceAndPath(ResourceLocation.DEFAULT_NAMESPACE, "textures/gui/container/furnace.png");

	protected int arrowX, arrowY;

	public static class Rectangle
	{
		public int x, y, width, height;

		Rectangle()
		{
			x = y = width = height = 0;
		}

		public Rectangle(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public int getRight()
		{
			return x + width - 1;
		}

		public int getBottom()
		{
			return y + height - 1;
		}

		public void fill(GuiGraphics pGuiGraphics, int color)
		{
			pGuiGraphics.fill(x, y, getRight() + 1, getBottom() + 1, color);
		}

		public void blit(GuiGraphics pGuiGraphics, ResourceLocation resource, int hOffset, int vOffset)
		{
			pGuiGraphics.blit(resource, x, y, hOffset, vOffset, width, height);
		}
	}

	public BaseMachineScreen(BaseMachineMenu pMenu, Inventory pPlayerInventory, Component pTitle)
	{
		super(pMenu, pPlayerInventory, pTitle);

		this.imageHeight = menu.getScreenHeight();
		this.imageWidth = menu.getScreenWidth();

		this.inventoryLabelX = menu.getScreenInventoryX() + TEXT_OFFSET_WIDTH;
		this.inventoryLabelY = menu.getScreenInventoryY() - TEXT_PADDING_HEIGHT - TEXT_HEIGHT;

		arrowX = (imageWidth - ARROW_WIDTH) / 2;
		arrowY = BORDER_HEIGHT +
				TEXT_PADDING_HEIGHT +
				TEXT_HEIGHT +
				TEXT_PADDING_HEIGHT +
				(SLOT_WITH_BORDER_HEIGHT * 3 +
						PADDING_HEIGHT +
						SLOT_WITH_BORDER_HEIGHT * 3 -
						ARROW_HEIGHT) / 2;
	}

	@Override
	protected void init()
	{
		super.init();
	}

	private PoseStack translatePose(GuiGraphics pGuiGraphics)
	{
		var poseStack = pGuiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(leftPos, topPos, 0);
		return poseStack;
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY)
	{
		//RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		var poseStack = drawForm(pGuiGraphics);

		// Draw item slot
		for (int i = 0; i < menu.container.getContainerSize(); i++)
		{
			var rect = menu.getSlotRectangle(i);
			rect.blit(pGuiGraphics, TEXTURE_BASE_LOCATION, 8, 0);
		}
/*		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 4; ++j)
			{
				pGuiGraphics.blit(
						TEXTURE_BASE_LOCATION,
						BORDER_WIDTH + PADDING_WIDTH + SLOT_BORDER_WIDTH * j,
						BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_BORDER_HEIGHT * i,
						8,
						0,
						SLOT_BORDER_WIDTH,
						SLOT_BORDER_HEIGHT
				);
				pGuiGraphics.blit(
						TEXTURE_BASE_LOCATION,
						imageWidth - BORDER_WIDTH - PADDING_WIDTH - SLOT_BORDER_WIDTH * j - SLOT_BORDER_WIDTH,
						BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_BORDER_HEIGHT * i,
						8,
						0,
						SLOT_BORDER_WIDTH,
						SLOT_BORDER_HEIGHT
				);
			}*/

		// Draw fluid slot
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 4; ++j)
			{
				pGuiGraphics.blit(
						TEXTURE_BASE_LOCATION,
						BORDER_WIDTH + PADDING_WIDTH + SLOT_WITH_BORDER_WIDTH * j,
						BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * 3 + PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * i,
						8 + SLOT_WITH_BORDER_WIDTH + SLOT_WITH_BORDER_WIDTH,
						0,
						SLOT_WITH_BORDER_WIDTH,
						SLOT_WITH_BORDER_HEIGHT
				);
				pGuiGraphics.blit(
						TEXTURE_BASE_LOCATION,
						imageWidth - BORDER_WIDTH - PADDING_WIDTH - SLOT_WITH_BORDER_WIDTH * j - SLOT_WITH_BORDER_WIDTH,
						BORDER_HEIGHT + TEXT_PADDING_HEIGHT + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * 3 + PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * i,
						8 + SLOT_WITH_BORDER_WIDTH + SLOT_WITH_BORDER_WIDTH,
						0,
						SLOT_WITH_BORDER_WIDTH,
						SLOT_WITH_BORDER_HEIGHT
				);
			}

		// draw inventory
		for (int i = 0; i < 9; ++i)
		{
			for (int j = 0; j < 3; ++j)
				pGuiGraphics.blit(
						TEXTURE_BASE_LOCATION,
						inventoryLabelX - TEXT_OFFSET_WIDTH + SLOT_WITH_BORDER_WIDTH * i,
						inventoryLabelY + TEXT_HEIGHT + TEXT_PADDING_HEIGHT + SLOT_WITH_BORDER_HEIGHT * j,
						8,
						0,
						SLOT_WITH_BORDER_WIDTH,
						SLOT_WITH_BORDER_HEIGHT
				);
			pGuiGraphics.blit(
					TEXTURE_BASE_LOCATION,
					inventoryLabelX - TEXT_OFFSET_WIDTH + SLOT_WITH_BORDER_WIDTH * i,
					imageHeight - BORDER_HEIGHT - PADDING_HEIGHT - SLOT_WITH_BORDER_HEIGHT,
					8,
					0,
					SLOT_WITH_BORDER_WIDTH,
					SLOT_WITH_BORDER_HEIGHT
			);
		}

		drawArrow(pGuiGraphics, menu.data.get(DATA_PROCESS_INDEX), menu.data.get(DATA_PROCESS_NEED_TIME_INDEX));

		poseStack.popPose();
	}

	/**
	 * 绘制灰色容器框, 必须在Pose转换后调用
	 *
	 * @param pGuiGraphics 画板
	 * @return 返回转换后的PoseStack
	 */
	protected PoseStack drawForm(GuiGraphics pGuiGraphics)
	{
		renderBackground(pGuiGraphics);

		var poseStack = translatePose(pGuiGraphics);

		int heightWithoutCorner = Math.max(imageHeight - 8, 0);
		int widthWithoutCorner = Math.max(imageWidth - 8, 0);
		var imageRect = new Rectangle(0, 0, imageWidth, imageHeight);

		int white = FastColor.ARGB32.color(255, 255, 255, 255);
		int black = FastColor.ARGB32.color(255, 0, 0, 0);
		int darkGrey = FastColor.ARGB32.color(255, 85, 85, 85);

		new Rectangle(4, 0, widthWithoutCorner, 1).fill(pGuiGraphics, black);
		new Rectangle(0, 4, 1, heightWithoutCorner).fill(pGuiGraphics, black);
		new Rectangle(4, imageRect.getBottom(), widthWithoutCorner, 1).fill(pGuiGraphics, black);
		new Rectangle(imageRect.getRight(), 4, 1, heightWithoutCorner).fill(pGuiGraphics, black);

		new Rectangle(3, 3, imageWidth - 6, imageHeight - 6).fill(pGuiGraphics, FastColor.ARGB32.color(255, 0xC6, 0xC6, 0xC6));

		new Rectangle(4, 1, widthWithoutCorner, 2).fill(pGuiGraphics, white);
		new Rectangle(1, 4, 2, heightWithoutCorner).fill(pGuiGraphics, white);
		new Rectangle(4, imageRect.getBottom() - 2, widthWithoutCorner, 2).fill(pGuiGraphics, darkGrey);
		new Rectangle(imageRect.getRight() - 2, 4, 2, heightWithoutCorner).fill(pGuiGraphics, darkGrey);

		pGuiGraphics.blit(TEXTURE_BASE_LOCATION, 0, 0, 0, 0, 4, 4);
		pGuiGraphics.blit(TEXTURE_BASE_LOCATION, imageRect.getRight() - 3, 0, 4, 0, 4, 4);
		pGuiGraphics.blit(TEXTURE_BASE_LOCATION, 0, imageRect.getBottom() - 3, 0, 4, 4, 4);
		pGuiGraphics.blit(TEXTURE_BASE_LOCATION, imageRect.getRight() - 3, imageRect.getBottom() - 3, 4, 4, 4, 4);

		return poseStack;
	}

	private void drawArrow(GuiGraphics pGuiGraphics, int l)
	{
		pGuiGraphics.blit(TEXTURE_ARROW_LOCATION, arrowX, arrowY, DEFAULT_ARROW_OFFSET_X, DEFAULT_ARROW_OFFSET_Y, ARROW_WIDTH, ARROW_HEIGHT);
		pGuiGraphics.blit(TEXTURE_ARROW_LOCATION, arrowX, arrowY, LIGHT_ARROW_OFFSET_X, LIGHT_ARROW_OFFSET_Y, l, ARROW_HEIGHT);
	}

	protected void drawArrow(GuiGraphics pGuiGraphics, float percent)
	{
		drawArrow(pGuiGraphics, (int) (Math.ceil(percent * ARROW_WIDTH) + 0.5));
	}

	protected void drawArrow(GuiGraphics pGuiGraphics, int process, int tot)
	{
		drawArrow(pGuiGraphics, process * ARROW_WIDTH / tot);
	}
}

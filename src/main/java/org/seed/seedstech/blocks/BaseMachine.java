package org.seed.seedstech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.seed.seedstech.blockentities.BaseMachineEntity;

public class BaseMachine extends BaseEntityBlock
{
	protected final BaseMachineDefinition machineType;

	public BaseMachine(BaseMachineDefinition type)
	{
		super(Properties.copy(Blocks.STONE));
		machineType = type;
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return machineType.blockEntityType.get().create(pos, state);
	}

	@Override
	@NotNull
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}

	@Override
	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (oldState.getBlock() != newState.getBlock())
			if (level.getBlockEntity(pos) instanceof BaseMachineEntity machineEntity)
				machineEntity.dropItems();

		super.onRemove(oldState, level, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState statue, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if (!level.isClientSide())
			if (level.getBlockEntity(pos) instanceof BaseMachineEntity machineEntity)
				NetworkHooks.openScreen((ServerPlayer) player, machineEntity, pos);
		return InteractionResult.sidedSuccess(level.isClientSide());
		//return super.use(statue, level, pos, player, hand, hitResult);
	}

	private <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, T pBlockEntity)
	{
		if (pBlockEntity instanceof BaseMachineEntity t)
			if (pLevel.isClientSide())
				t.clientTick(pLevel, pPos, pState);
			else
				t.serverTick(pLevel, pPos, pState);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, machineType.blockEntityType.get(), this::tick);
	}
}

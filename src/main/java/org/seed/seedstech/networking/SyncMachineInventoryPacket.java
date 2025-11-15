package org.seed.seedstech.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.seed.seedstech.blockentities.BaseMachineEntity;

import java.util.function.Supplier;

public class SyncMachineInventoryPacket implements BasePacket
{
	/**
	 * Server端构造函数
	 * @param entity 要同步的机器实体
	 */
	SyncMachineInventoryPacket(BaseMachineEntity entity)
	{

	}

	/**
	 * Client端构造函数
	 * @param buf 来自服务器的数据
	 */
	SyncMachineInventoryPacket(FriendlyByteBuf buf)
	{

	}

	/**
	 * 将类转换成字节流
	 * @param buf 字节流
	 */
	@Override
	public void encode(FriendlyByteBuf buf)
	{

	}

	@Override
	public boolean handle(Supplier<NetworkEvent.Context> context)
	{
		return false;
	}
}

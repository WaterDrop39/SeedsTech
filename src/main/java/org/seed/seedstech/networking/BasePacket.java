package org.seed.seedstech.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface BasePacket
{
	void encode(FriendlyByteBuf buf);
	boolean handle(Supplier<NetworkEvent.Context> context);
}

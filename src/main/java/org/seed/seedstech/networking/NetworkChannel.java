package org.seed.seedstech.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.seed.seedstech.SeedsTech;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkChannel
{
	private static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.ChannelBuilder
			.named(ResourceLocation.fromNamespaceAndPath(SeedsTech.MODID, "main_channel"))
			.clientAcceptedVersions(s -> true)
			.serverAcceptedVersions(s -> true)
			.networkProtocolVersion(() -> "1.0")
			.simpleChannel();

	private static int id = 0;

	private static int getId()
	{
		return id++;
	}

	public static <MSG extends BasePacket> void registerMessage(Class<MSG> objClass, Function<FriendlyByteBuf, MSG> constructor)
	{
		NETWORK_CHANNEL.messageBuilder(objClass, getId(), NetworkDirection.PLAY_TO_CLIENT)
				.decoder(constructor)
				.encoder(MSG::encode)
				.consumerMainThread(MSG::handle)
				.add();
	}

	public static void register(IEventBus bus)
	{
		registerMessage(SyncMachineInventoryPacket.class, SyncMachineInventoryPacket::new);
	}

	public static <MSG> void sendToServer(MSG msg)
	{
		NETWORK_CHANNEL.sendToServer(msg);
	}

	public static <MSG> void sendToClient(MSG msg, ServerPlayer player)
	{
		NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
	}
}

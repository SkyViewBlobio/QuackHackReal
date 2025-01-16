package me.kopamed.galacticc.events.Network;

import io.netty.channel.ChannelPipeline;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class NettyInjector {

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            ChannelPipeline pipeline = event.getManager().channel().pipeline();

            if (pipeline.get("packet_handler") != null) {
                // Inject our custom Netty handler
                pipeline.addBefore("packet_handler", "galacticc_packet_handler", new NettyPacketHandler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        try {
            ChannelPipeline pipeline = event.getManager().channel().pipeline();

            if (pipeline.get("galacticc_packet_handler") != null) {
                pipeline.remove("galacticc_packet_handler");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

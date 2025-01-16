package me.kopamed.galacticc.events.Network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.kopamed.galacticc.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;

public class NettyPacketHandler extends ChannelDuplexHandler {
    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet<?>) {
            Packet<?> packet = (Packet<?>) msg;

            // Fire the receive event
            PacketEvent event = new PacketEvent(packet, PacketEvent.Time.Receive);
            MinecraftForge.EVENT_BUS.post(event);

            // Cancel packet if event is canceled
            if (event.isCanceled()) {
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet<?>) {
            Packet<?> packet = (Packet<?>) msg;

            // Fire the send event
            PacketEvent event = new PacketEvent(packet, PacketEvent.Time.Send);
            MinecraftForge.EVENT_BUS.post(event);

            // Cancel packet if event is canceled
            if (event.isCanceled()) {
                return;
            }
        }
        super.write(ctx, msg, promise);
    }
}

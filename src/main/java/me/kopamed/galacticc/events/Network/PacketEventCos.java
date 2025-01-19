package me.kopamed.galacticc.events.Network;

import me.kopamed.galacticc.events.Event;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class PacketEventCos extends Event {

    private final Packet<?> packet;

    public PacketEventCos(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    @Cancelable
    public static class PacketReceiveEvent extends PacketEventCos {
        public PacketReceiveEvent(Packet<?> packet) {
            super(packet);
        }
    }

    @Cancelable
    public static class PacketSendEvent extends PacketEventCos {
        public PacketSendEvent(Packet<?> packet) {
            super(packet);
        }
    }
}
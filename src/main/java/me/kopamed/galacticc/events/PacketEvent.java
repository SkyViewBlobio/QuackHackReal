package me.kopamed.galacticc.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PacketEvent extends Event {
    private final Packet<?> packet;
    private final Time time;
    private boolean canceled;

    public PacketEvent(Packet<?> packet, Time time) {
        this.packet = packet;
        this.time = time;
    }

    public Time getTime() {
        return this.time;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    public static enum Time {
        Send,
        Receive;
    }
}
package me.kopamed.galacticc.events.Network;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;


@Cancelable
public class ReachEventCos extends Event {

    // reach value
    private float reach;


    public void setReach(float reach) {
        this.reach = reach;
    }


    public float getReach() {
        return reach;
    }
}
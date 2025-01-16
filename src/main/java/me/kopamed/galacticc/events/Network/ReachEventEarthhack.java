package me.kopamed.galacticc.events.Network;


import me.kopamed.galacticc.events.Event;

public class ReachEventEarthhack extends Event
{
    private float reach;
    private float hitBox;

    public ReachEventEarthhack(float reach, float hitBox)
    {
        this.reach  = reach;
        this.hitBox = hitBox;
    }

    public float getReach()
    {
        return reach;
    }

    public void setReach(float reach)
    {
        this.reach = reach;
    }

    public float getHitBox()
    {
        return hitBox;
    }

    public void setHitBox(float hitBox)
    {
        this.hitBox = hitBox;
    }

}
package com.enioka.api.admin;

import java.io.Serializable;

public class HistorySlotDto implements Serializable
{
    private String slotStart;
    private int ended;
    private int crashed;
    private int killed;
    private int cancelled;

    public String getSlotStart()
    {
        return slotStart;
    }

    public void setSlotStart(String slotStart)
    {
        this.slotStart = slotStart;
    }

    public int getEnded()
    {
        return ended;
    }

    public void setEnded(int ended)
    {
        this.ended = ended;
    }

    public int getCrashed()
    {
        return crashed;
    }

    public void setCrashed(int crashed)
    {
        this.crashed = crashed;
    }

    public int getKilled()
    {
        return killed;
    }

    public void setKilled(int killed)
    {
        this.killed = killed;
    }

    public int getCancelled()
    {
        return cancelled;
    }

    public void setCancelled(int cancelled)
    {
        this.cancelled = cancelled;
    }
}

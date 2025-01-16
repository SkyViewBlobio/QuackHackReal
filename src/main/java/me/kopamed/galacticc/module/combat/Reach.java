package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Reach extends Module {
    private double blockReachDistance, minReach, maxReach;
    private float partialTick;

    public Reach() {
        super("Reichweite", "You have a good gaming chait", true, false, Category.ANGRIFF);

        Setting minReach = new Setting("Min Reach", this, 3, 3, 12, false);
        Setting maxReach = new Setting("Max Reach", this, 3.2, 3, 12, false);

        Setting blockReachDistance = new Setting("brd", this, 3, 0, 6, false);
        Setting partialTicks = new Setting("pt", this, 1, 0, 20, false);

        Galacticc.instance.settingsManager.rSetting(minReach);
        Galacticc.instance.settingsManager.rSetting(maxReach);

        Galacticc.instance.settingsManager.rSetting(blockReachDistance);
        Galacticc.instance.settingsManager.rSetting(partialTicks);
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityPlayer> event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
        }
    }
}



package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    public Velocity() {
        super("Ruckschlag", "Changes the amount of knockback you take", false, false, Category.ANGRIFF);
        Galacticc.instance.settingsManager.rSetting(new Setting("Horizontal", this, 92, 0, 200, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Vertical", this, 100, 0, 200, true));
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
        if (Galacticc.instance.destructed) {return;}
        if (mc.player == null) {
            return;
        }
        float horizontal = (float)Galacticc.instance.settingsManager.getSettingByName(this, "Horizontal").getValDouble();
        float vertical = (float)Galacticc.instance.settingsManager.getSettingByName(this, "Vertical").getValDouble();

        if (mc.player.hurtTime == mc.player.maxHurtTime && mc.player.maxHurtTime > 0) {
            mc.player.motionX *= horizontal / 100;
            mc.player.motionY *= vertical / 100;
            mc.player.motionZ *= horizontal / 100;
        }
    }
}
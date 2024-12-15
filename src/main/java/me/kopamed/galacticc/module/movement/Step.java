package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Step extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public Step() {
        super("Step", "@Hauptinformation: " +
                "Laesst dich ueber Bloecke steigen. " +
                "@Optionen: " +
                "- Steig-Hoehe gibt an, wie hoch du steigst.", true, false, Category.BEWEGUNG);

        Galacticc.instance.settingsManager.rSetting(new Setting("Steig-Hoehe", this, 1, 1, 10, true));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        if (mc.thePlayer != null) {
            double stepHeight = Galacticc.instance.settingsManager.getSettingByName(this, "Steig-Hoehe").getValDouble();
            mc.thePlayer.stepHeight = (float) stepHeight;
        }
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.6F;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        double stepHeight = Galacticc.instance.settingsManager.getSettingByName(this, "Steig-Hoehe").getValDouble();
        mc.thePlayer.stepHeight = (float) stepHeight;

        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isSneaking()) {
            mc.thePlayer.stepHeight = 0.6F;
        }
    }
}

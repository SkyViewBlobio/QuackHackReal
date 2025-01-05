package me.kopamed.galacticc.module.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class Step extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Step() {
        super("Step", "@Hauptinformation: " +
                "Laesst dich ueber Bloecke steigen oder schneller herunterfallen. " +
                "@Optionen: " +
                "- Steig-Hoehe gibt an, wie hoch du steigst. " +
                "- Step Mode bietet drei Modi: " +
                "- Step erlaubt das normale Hinaufsteigen ueber Bloecke, " +
                "- ReverseStep beschleunigt das Herabfallen, und " +
                "- Both kombiniert beide Funktionen. " +
                "- Reverse Geschwindigkeit gibt an, wie schnell du herunterfaellst, alles ueber 1.0 gibt keinen Fallschaden aber falls du dir unsicher bist nutz dieses Modul mit dem Modul namens (NoFall)",
                true, false, Category.BEWEGUNG);

        ArrayList<String> stepModes = new ArrayList<>();
        stepModes.add("Step");
        stepModes.add("ReverseStep");
        stepModes.add("Both");
        Galacticc.instance.settingsManager.rSetting(new Setting("Step Mode", this, "Step", stepModes));
        Galacticc.instance.settingsManager.rSetting(new Setting("Steig-Hoehe", this, 1, 1, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Reverse Geschwindigkeit", this, 1.0, 0.05, 6.0, false));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        updateStepHeight();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (mc.player != null) {
            mc.player.stepHeight = 0.6F;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Step Mode").getValString();
        double stepHeight = Galacticc.instance.settingsManager.getSettingByName(this, "Steig-Hoehe").getValDouble();
        double reverseSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Reverse Geschwindigkeit").getValDouble();

        switch (mode) {
            case "Step":
                // Perform standard step logic
                if (!mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isSneaking()) {
                    mc.player.stepHeight = (float) stepHeight;
                } else {
                    mc.player.stepHeight = 0.6F;
                }
                break;

            case "ReverseStep":
                // Perform reverse step logic (faster stepping down)
                if (mc.player.onGround && mc.player.motionY < 0 && !mc.player.isInWater() && !mc.player.isInLava()) {
                    mc.player.motionY -= reverseSpeed;
                }
                mc.player.stepHeight = 0.6F;
                break;

            case "Both":
                // Perform both step and reverse step logic
                if (!mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isSneaking()) {
                    mc.player.stepHeight = (float) stepHeight;
                } else {
                    mc.player.stepHeight = 0.6F;
                }

                if (mc.player.onGround && mc.player.motionY < 0 && !mc.player.isInWater() && !mc.player.isInLava()) {
                    mc.player.motionY -= reverseSpeed;
                }
                break;
        }
    }

    @Override
    public String getHUDInfo() {
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Step Mode").getValString();
        return ChatFormatting.GRAY + "[" + ChatFormatting.GRAY + mode + ChatFormatting.GRAY + "]";
    }

    private void updateStepHeight() {
        if (mc.player != null) {
            double stepHeight = Galacticc.instance.settingsManager.getSettingByName(this, "Steig-Hoehe").getValDouble();
            mc.player.stepHeight = (float) stepHeight;
        }
    }
}

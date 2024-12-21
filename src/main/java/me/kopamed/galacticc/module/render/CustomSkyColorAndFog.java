package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomSkyColorAndFog extends Module {

    public CustomSkyColorAndFog() {
        super("NebelFarbe", "@Hauptinformationen: " +
                "laesst dich die Farbe des Nebels aendern.", false, false, Category.VISUELLES);

        Galacticc.instance.settingsManager.rSetting(new Setting("Nebel Farbe", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Nebel Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Nebel Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Nebel Blau", this, 0, 0, 255, true));
    }

    @SubscribeEvent
    public void onFogColor(FogColors event) {
        if (!Galacticc.instance.settingsManager.getSettingByName(this, "Nebel Farbe").getValBoolean()) {
            return;
        }

        // Get RGB values from settings
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Nebel Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Nebel Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Nebel Blau").getValDouble();

        // Use event setters (if available) or manipulate event properties safely
        event.setRed(red / 255.0f);
        event.setGreen(green / 255.0f);
        event.setBlue(blue / 255.0f);
    }

    @Override
    public String getHUDInfo() {
        boolean fogEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "Nebel Farbe").getValBoolean();
        return ChatFormatting.GRAY + "[FogColor: " + ChatFormatting.GRAY + (fogEnabled ? "ON" : "OFF") + "]";
    }
}

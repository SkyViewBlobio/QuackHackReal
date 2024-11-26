package me.kopamed.galacticc.module.textstuff;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class Watermark extends Module {

    private final Minecraft mc;

    public Watermark() {
        super("Modname", "Lässt dich den Mod-Namen anzeigen",
                false, false, Category.TEXTSTUFF);

        this.mc = Minecraft.getMinecraft();

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        Galacticc.instance.settingsManager.rSetting(new Setting
                ("X Offset", this, 0, -screenWidth, screenWidth, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Y Offset", this, 0, -screenHeight, screenHeight, true));

        ArrayList<String> watermarkModes = new ArrayList<>();
        watermarkModes.add("Quackig");
        watermarkModes.add("Speziel");
        watermarkModes.add("Entig");
        watermarkModes.add("Normal");
        watermarkModes.add("Nummern");
        watermarkModes.add("Lang");
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Modnamen Modus", this, "Quackig", watermarkModes));

        // Add RGB sliders for text and background colors
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Text Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Text Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Text Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Hintergrund Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Hintergrund Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Hintergrund Blau", this, 0, 0, 255, true));

        // Add Alpha slider for background transparency
        Galacticc.instance.settingsManager.rSetting(new Setting
                ("Hintergrund Dichte", this, 144, 0, 255, true));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (!this.isToggled()) {
            return;
        }

        int xOffset = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Modnamen Modus").getValString();

        // Fetch dynamic version and player name
        String version = Galacticc.VERSION; // Dynamically fetch the client version
        String playerName = mc.getSession().getUsername(); // Fetch the current Minecraft player's name

        String watermarkText;
        switch (mode) {
            case "Speziel":
                watermarkText = "QuackHack | " + version;
                break;
            case "Entig":
                watermarkText = "QuackHack > Liebe sie so sehr mein datz <3 (" + playerName + ")";
                break;
            case "Normal":
                watermarkText = "QuackHack";
                break;
            case "Nummern":
                watermarkText = "0u4ckH4ck";
                break;
            case "Lang":
                watermarkText = "QuackHack | " + version + " | " + playerName + " | Fuhl dich geknutscht!";
                break;
            case "Quackig":
            default:
                watermarkText = "QuackHack quackt halb so viel wie sie.";
                break;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int xPos = sr.getScaledWidth() / 2 + xOffset;
        int yPos = sr.getScaledHeight() / 2 + yOffset;

        FontRenderer fr = mc.fontRendererObj;

        // Get RGB and alpha values for text and background
        int textRed = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Text Rot").getValDouble();
        int textGreen = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Text Green").getValDouble();
        int textBlue = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Text Blau").getValDouble();

        int bgRed = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Rot").getValDouble();
        int bgGreen = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Green").getValDouble();
        int bgBlue = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Blau").getValDouble();
        int bgAlpha = (int)
                Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Dichte").getValDouble();

        // Convert RGB and alpha to ARGB format
        int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;
        int textColor = (0xFF << 24) | (textRed << 16) | (textGreen << 8) | textBlue;
        // Draw background and text
        Gui.drawRect(xPos - 2, yPos - 2, xPos + fr.getStringWidth(watermarkText)
                + 2, yPos + fr.FONT_HEIGHT + 2, backgroundColor);
        fr.drawStringWithShadow(watermarkText, xPos, yPos, textColor);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
    }
}

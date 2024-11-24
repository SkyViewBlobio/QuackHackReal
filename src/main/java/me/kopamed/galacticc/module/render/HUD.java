package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import me.kopamed.galacticc.utils.Arch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class HUD extends Module {
    private boolean watermark, background, textShadow, active;
    private int margin;
    private int waterMarkMargin, topOffSet, rightOffSet, miniboxWidth;
    private int modColor = 0xFFFFFF;
    private int wmColor = 0xFF4500;
    private String sortMode;
    public List<Module> modList;

    public HUD() {
        super("Bildschirmzeugs", "Draws the module list on your screen", false, false, Category.VISUELLES);

        ArrayList<String> sort = new ArrayList<>();
        sort.add("Lang bis > kurz");
        sort.add("kurz bis > lang");
        sort.add("Alphabetisch");
        sort.add("random");

        Galacticc.instance.settingsManager.rSetting(new Setting("Arraylist sort", this, "lang bis > kurz", sort));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modname", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Text Sschatten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Module padding", this, 2, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modname padding", this, 3, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Mini box width", this, 1, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("HUD top offset", this, 4, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("HUD right offset", this, 4, 0, 10, true));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent egoe) {
        if (Galacticc.instance.destructed || egoe.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS || !active) {
            return;
        }

        sortMode = Galacticc.instance.settingsManager.getSettingByName(this, "Arraylist sort").getValString();
        watermark = Galacticc.instance.settingsManager.getSettingByName(this, "Modname").getValBoolean();
        background = Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund").getValBoolean();
        textShadow = Galacticc.instance.settingsManager.getSettingByName(this, "Text Sschatten").getValBoolean();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        margin = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Module padding").getValDouble();
        waterMarkMargin = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Modname padding").getValDouble();
        topOffSet = (int) Galacticc.instance.settingsManager.getSettingByName(this, "HUD top offset").getValDouble();
        rightOffSet = (int) Galacticc.instance.settingsManager.getSettingByName(this, "HUD right offset").getValDouble();
        miniboxWidth = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Mini box width").getValDouble();

        if (watermark) {
            renderWatermark(sr);
        }

        modList = Galacticc.instance.moduleManager.getModulesList();

        // Adjusted sort methods
        if ("Lang bis > kurz".equalsIgnoreCase(sortMode)) {
            modList = Arch.sortByNameLengthDescending();
        } else if ("kurz bis > lang".equalsIgnoreCase(sortMode)) {
            modList = Arch.sortByNameLengthAscending();
        } else if ("Alphabetisch".equalsIgnoreCase(sortMode)) {
            modList = Arch.sortAlphabetically();
        }

        renderModules(sr);
    }

    private void renderWatermark(ScaledResolution sr) {
        String waterMarkText = Galacticc.MODID + Galacticc.VERSION.toUpperCase();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        if (background) {
            Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - waterMarkMargin * 2 - rightOffSet,
                    topOffSet, sr.getScaledWidth() - rightOffSet,
                    topOffSet + waterMarkMargin * 2 + fr.FONT_HEIGHT, 0x90000000);
            Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - waterMarkMargin * 2 - rightOffSet - miniboxWidth,
                    topOffSet, sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - waterMarkMargin * 2 - rightOffSet,
                    topOffSet + waterMarkMargin * 2 + fr.FONT_HEIGHT, 0xffff4500);
        }
        if (textShadow) {
            fr.drawStringWithShadow(waterMarkText,
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - rightOffSet - waterMarkMargin,
                    topOffSet + waterMarkMargin, wmColor);
        } else {
            fr.drawString(waterMarkText,
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - rightOffSet - waterMarkMargin,
                    topOffSet + waterMarkMargin, wmColor);
        }

        topOffSet += fr.FONT_HEIGHT + waterMarkMargin * 2;
    }

    private void renderModules(ScaledResolution sr) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        for (Module mod : modList) {
            if (mod.visible && mod.isToggled()) {
                if (background) {
                    Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - margin * 2 - rightOffSet,
                            topOffSet, sr.getScaledWidth() - rightOffSet,
                            topOffSet + margin * 2 + fr.FONT_HEIGHT, 0x90000000);
                    Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - margin * 2 - rightOffSet - miniboxWidth,
                            topOffSet, sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - margin * 2 - rightOffSet,
                            topOffSet + margin * 2 + fr.FONT_HEIGHT, 0xffff4500);
                }
                if (textShadow) {
                    fr.drawStringWithShadow(mod.getName(),
                            sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - rightOffSet - margin,
                            topOffSet + margin, modColor);
                } else {
                    fr.drawString(mod.getName(),
                            sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - rightOffSet - margin,
                            topOffSet + margin, modColor);
                }

                topOffSet += fr.FONT_HEIGHT + margin * 2;
            }
        }
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        this.active = true;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        this.active = false;
    }
}

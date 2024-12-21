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

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo: cleanup? and fix the funny hunger / heart / armor icon coloring.
public class HUD extends Module {
    private boolean watermark, background, textShadow, active;
    private int margin;
    private int waterMarkMargin, topOffSet, rightOffSet, miniboxWidth;
    private int modColor = 0xFFFFFF;
    private int wmColor = 0xFF4500;
    private String sortMode;
    // Track animation progress for modules
    private final Map<Module, Long> moduleAnimationStartTimes = new HashMap<>();
    private final Map<Module, Float> moduleAnimationProgress = new HashMap<>();
    private final float ANIMATION_DURATION = 500.0F; // Duration of the animation in ms
    public List<Module> modList;

    public HUD() {
        super("Bildschirmzeugs", "@Hauptinformationen: " +
                "Zeigt dir den Modnamen, die aktiven Module, laesst dich die Farben von den benannten Dingen aendern", false, false, Category.VISUELLES);

        ArrayList<String> sort = new ArrayList<>();
        sort.add("Lang bis > kurz");
        sort.add("kurz bis > lang");
        sort.add("Alphabetisch");
        sort.add("random");
//todo fix gethudinfo
        Galacticc.instance.settingsManager.rSetting(new Setting("Arraylist sort", this, "lang bis > kurz", sort));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modname", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Text Schatten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Module padding", this, 2, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modname padding", this, 3, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Mini box width", this, 1, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("HUD top offset", this, 4, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("HUD right offset", this, 4, 0, 10, true));

        // Add settings for Colors
        Galacticc.instance.settingsManager.rSetting(new Setting("Colors", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Alpha", this, 255, 0, 255, true));

        // Add settings for background colors
        Galacticc.instance.settingsManager.rSetting(new Setting("Background Colors", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("BG Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("BG Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("BG Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("BG Alpha", this, 144, 0, 255, true));

        // Add settings for gradient colors
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Colors", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Alpha", this, 255, 0, 255, true));

        // Add setting for animated gradient
        Galacticc.instance.settingsManager.rSetting(new Setting("AnimatedGradient", this, false));
        // Add setting for gradient speed
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Speed", this, 1.0, 0.1, 5.0, false));

    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent egoe) {
        if (Galacticc.instance.destructed || egoe.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS || !active) {
            return;
        }

        sortMode = Galacticc.instance.settingsManager.getSettingByName(this, "Arraylist sort").getValString();
        watermark = Galacticc.instance.settingsManager.getSettingByName(this, "Modname").getValBoolean();
        background = Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund").getValBoolean();
        textShadow = Galacticc.instance.settingsManager.getSettingByName(this, "Text Schatten").getValBoolean();

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
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

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
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Fetch text colors setting and RGBA values
        boolean useColors = Galacticc.instance.settingsManager.getSettingByName(this, "Colors").getValBoolean();
        int textRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Rot").getValDouble();
        int textGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green").getValDouble();
        int textBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blau").getValDouble();
        int textAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Alpha").getValDouble();

        int textColor = (textAlpha << 24) | (textRed << 16) | (textGreen << 8) | textBlue;

        // Fetch background colors setting and RGBA values
        boolean useBackgroundColors = Galacticc.instance.settingsManager.getSettingByName(this, "Background Colors").getValBoolean();
        int bgRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "BG Rot").getValDouble();
        int bgGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "BG Green").getValDouble();
        int bgBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "BG Blau").getValDouble();
        int bgAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "BG Alpha").getValDouble();

        int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;

        long currentTime = System.currentTimeMillis();
        int moduleCount = modList.size();
        int moduleIndex = 0; // Start from bottom

        for (int i = moduleCount - 1; i >= 0; i--) {
            Module mod = modList.get(i);
            if (mod.visible && mod.isToggled()) {
                moduleAnimationStartTimes.putIfAbsent(mod, currentTime);
                float elapsed = currentTime - moduleAnimationStartTimes.get(mod);
                // Calculate vertical animation progress (0.0F to 1.0F)
                float progress = Math.min(elapsed / ANIMATION_DURATION, 1.0F);

                // Calculate horizontal animation position
                int finalX = sr.getScaledWidth() - fr.getStringWidth(mod.getName()) - rightOffSet - margin;
                int startX = sr.getScaledWidth();
                int currentX = (int) (startX - (startX - finalX) * progress);

                // Render background if enabled
                if (useBackgroundColors) {
                    Gui.drawRect(
                            currentX - margin,
                            topOffSet,
                            sr.getScaledWidth() - rightOffSet,
                            topOffSet + margin * 2 + fr.FONT_HEIGHT,
                            backgroundColor
                    );
                }

                // Render module text
                int renderColor = textColor;
                if (useColors) {
                    // Replace the helper method with direct logic (if any special color effect is needed)
                    renderColor = textColor; // Simplify to always use textColor if no special effect is needed
                }

                if (textShadow) {
                    fr.drawStringWithShadow(
                            mod.getName(),
                            currentX,
                            topOffSet + margin,
                            renderColor
                    );
                } else {
                    fr.drawString(
                            mod.getName(),
                            currentX,
                            topOffSet + margin,
                            renderColor
                    );
                }

                topOffSet += fr.FONT_HEIGHT + margin * 2;
                moduleIndex++;
            }
        }

        // Cleanup animation data for removed modules
        moduleAnimationStartTimes.keySet().removeIf(mod -> !modList.contains(mod) || !mod.isToggled());
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

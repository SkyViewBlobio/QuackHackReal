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

public class HUD extends Module {
    private boolean watermark, background, textShadow, active;
    private int margin;
    private int waterMarkMargin, topOffSet, rightOffSet, miniboxWidth;
    private int modColor = 0xFFFFFF;
    private int wmColor = 0xFF4500;
    private String sortMode;
    public List<Module> modList;
    private final Map<Module, Long> activationTimes = new HashMap<>(); // Store activation times
    private final Map<Module, Long> deactivationTimes = new HashMap<>();
    //todo cleanup
    public HUD() {
        super("Bildschirmzeugs", "@Hauptinformationen: " +
                "Zeigt dir den Modnamen, die aktiven Module, laesst dich die Farben von den benannten Dingen aendern", false, false, Category.VISUELLES);

        ArrayList<String> sort = new ArrayList<>();
        sort.add("Lang bis > kurz");
        sort.add("kurz bis > lang");
        sort.add("Alphabetisch");
        sort.add("random");

        Galacticc.instance.settingsManager.rSetting(new Setting("Arraylist sort", this, "lang bis > kurz", sort));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modname", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund", this, true));
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
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Speed", this, 0.38, 0.1, 5.0, false));

    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent event) {
        if (Galacticc.instance.destructed || event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS || !active) {
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
        String waterMarkText = "QuackHack | 1.12.2+Internal-AutoCrystal | v2.0.9b+"; // Static watermark text
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;  // Updated here

        // Draw a border around the watermark (this still remains)
        if (background) {
            Gui.drawRect(
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - waterMarkMargin * 2 - rightOffSet - miniboxWidth,
                    topOffSet,
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - waterMarkMargin * 2 - rightOffSet,
                    topOffSet + waterMarkMargin * 2 + fr.FONT_HEIGHT,
                    0xffff4500
            );
        }

        // Draw the watermark text with or without shadow
        if (textShadow) {
            fr.drawStringWithShadow(
                    waterMarkText,
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - rightOffSet - waterMarkMargin,
                    topOffSet + waterMarkMargin,
                    wmColor
            );
        } else {
            fr.drawString(
                    waterMarkText,
                    sr.getScaledWidth() - fr.getStringWidth(waterMarkText) - rightOffSet - waterMarkMargin,
                    topOffSet + waterMarkMargin,
                    wmColor
            );
        }

        // Adjust the vertical offset for the next element
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

        // Fetch settings for gradient colors
        boolean useGradientColors = Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Colors").getValBoolean();
        int gradRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Rot").getValDouble();
        int gradGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Green").getValDouble();
        int gradBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Blau").getValDouble();
        int gradAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Alpha").getValDouble();

        boolean useAnimatedGradient = Galacticc.instance.settingsManager.getSettingByName(this, "AnimatedGradient").getValBoolean();
        double gradientSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Speed").getValDouble();

        int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;

        long currentTime = System.currentTimeMillis(); // Time for animations
        int slideDuration = 500; // Slide-in and slide-out duration in milliseconds

        int moduleCount = modList.size(); // Total modules for gradient calculation
        int moduleIndex = 0; // Start from bottom

        // Loop through active modules
        for (int i = moduleCount - 1; i >= 0; i--) { // Reverse order for bottom-to-top gradient
            Module mod = modList.get(i);
            boolean isActive = mod.visible && mod.isToggled();

            // Handle activation and deactivation times
            if (isActive) {
                if (!activationTimes.containsKey(mod)) {
                    activationTimes.put(mod, currentTime); // Register activation time if not already added
                }
            } else if (activationTimes.containsKey(mod)) {
                if (!deactivationTimes.containsKey(mod)) {
                    deactivationTimes.put(mod, currentTime); // Register deactivation time if not already added
                }
            }

            long activationTime = activationTimes.getOrDefault(mod, 0L);
            long deactivationTime = deactivationTimes.getOrDefault(mod, 0L);

            // Determine if the module is in slide-in or slide-out phase
            int elapsed = (int) (currentTime - activationTime);
            int elapsedDeactivation = (int) (currentTime - deactivationTime);

            boolean isSlidingOut = !isActive && deactivationTimes.containsKey(mod) && elapsedDeactivation < slideDuration;
            if (!isActive && !isSlidingOut) {
                // Remove from activationTimes and deactivationTimes after sliding out
                activationTimes.remove(mod);
                deactivationTimes.remove(mod);
                continue; // Skip rendering
            }

            String hudInfo = Module.hideHUDInfo ? null : mod.getHUDInfo();
            String displayText = mod.getName() + (hudInfo != null ? " " + hudInfo : "");
            int displayTextWidth = fr.getStringWidth(displayText); // Width of the full text

            // Determine horizontal position for slide-in or slide-out
            int finalPosition = sr.getScaledWidth() - displayTextWidth - rightOffSet - margin;
            int startOffScreen = sr.getScaledWidth() + displayTextWidth;
            int textX;

            if (isSlidingOut) {
                textX = finalPosition + (int) ((startOffScreen - finalPosition) * (elapsedDeactivation / (double) slideDuration));
            } else {
                textX = elapsed < slideDuration
                        ? startOffScreen - (int) ((startOffScreen - finalPosition) * (elapsed / (double) slideDuration))
                        : finalPosition; // Animate for slideDuration, then stop
            }

            // Calculate gradient or static color
            int renderColor = textColor;
            if (useColors) {
                if (useAnimatedGradient) {
                    // Animated gradient with bottom-to-top progression
                    float hue = ((currentTime % (int) (5000 / gradientSpeed)) / (5000.0F / (float) gradientSpeed) + (float) moduleIndex / moduleCount) % 1.0F;
                    int animatedColor = Color.HSBtoRGB(hue, 1.0F, 1.0F);
                    int animatedAlpha = (int) (textAlpha * ((animatedColor >> 24) & 0xFF) / 255.0F); // Preserve alpha
                    renderColor = (animatedAlpha << 24) | (animatedColor & 0xFFFFFF); // Combine alpha and RGB
                } else if (useGradientColors) {
                    // Static gradient logic (bottom-to-top)
                    float progress = (float) moduleIndex / (float) moduleCount;
                    int blendedRed = (int) ((1 - progress) * textRed + progress * gradRed);
                    int blendedGreen = (int) ((1 - progress) * textGreen + progress * gradGreen);
                    int blendedBlue = (int) ((1 - progress) * textBlue + progress * gradBlue);
                    int blendedAlpha = (int) ((1 - progress) * textAlpha + progress * gradAlpha);
                    renderColor = (blendedAlpha << 24) | (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;
                }
            }

            // Render background if enabled
            if (useBackgroundColors && background) {
                Gui.drawRect(
                        textX - margin,
                        topOffSet,
                        textX + displayTextWidth + margin,
                        topOffSet + margin * 2 + fr.FONT_HEIGHT,
                        backgroundColor
                );
            }

            // Render module text
            if (textShadow) {
                fr.drawStringWithShadow(
                        displayText,
                        textX,
                        topOffSet + margin,
                        renderColor
                );
            } else {
                fr.drawString(
                        displayText,
                        textX,
                        topOffSet + margin,
                        renderColor
                );
            }

            topOffSet += fr.FONT_HEIGHT + margin * 2;
            moduleIndex++; // Increment for gradient calculation
        }

        // Clean up activation and deactivation times for inactive modules
        activationTimes.keySet().removeIf(mod -> !modList.contains(mod) || (!mod.isToggled() && !deactivationTimes.containsKey(mod)));
        deactivationTimes.keySet().removeIf(mod -> !modList.contains(mod));
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
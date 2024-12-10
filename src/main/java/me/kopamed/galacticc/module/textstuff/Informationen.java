package me.kopamed.galacticc.module.textstuff;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import com.sun.management.OperatingSystemMXBean;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Informationen extends Module {

    private boolean active;
    private long gameStartTime;
    private Minecraft mc;
    private ScheduledExecutorService executor;
    private double cpuUsage = 0.0;


    public Informationen() {
        super("Informationen", "zeigt dir zeugs", false, false, Category.TEXTSTUFF);

        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige FPS", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Coordinaten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Spielzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Systemzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SpielTag", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige CPU", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Ping", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Systemdatum", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hand Info", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Traenke", this, true));

        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 0, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -500, 500, true));

        // Add color sliders for labels and values
        Galacticc.instance.settingsManager.rSetting(new Setting("Label Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Label Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Label Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Label Alpha", this, 255, 0, 255, true));

        // Add alpha sliders for labels and values
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Alpha", this, 255, 0, 255, true));

        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Dichte", this, 144, 0, 255, true));

        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Colors", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Speed", this, 1.0, 0.1, 5.0, false));

        this.mc = Minecraft.getMinecraft();
        this.gameStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (Galacticc.instance.destructed || !this.active) {
            return;
        }

        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        FontRenderer fr = mc.fontRendererObj;

        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

        int labelRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Rot").getValDouble();
        int labelGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Green").getValDouble();
        int labelBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Blau").getValDouble();
        int valueRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Rot").getValDouble();
        int valueGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Green").getValDouble();
        int valueBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Blau").getValDouble();

        boolean useGradient = Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Colors").getValBoolean();
        int gradRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Rot").getValDouble();
        int gradGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Green").getValDouble();
        int gradBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Blau").getValDouble();
        double gradientSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Speed").getValDouble();

        int bgRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Rot").getValDouble();
        int bgGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Green").getValDouble();
        int bgBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Blau").getValDouble();
        int bgAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Dichte").getValDouble();

        int labelAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Alpha").getValDouble();
        int valueAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Alpha").getValDouble();

        int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;

        ScaledResolution sr = new ScaledResolution(mc);
        int xPos = sr.getScaledWidth() / 2 + xOffset;
        int yPos = sr.getScaledHeight() / 2 - 40 + yOffset;

        boolean showFPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
        boolean showCoords = Galacticc.instance.settingsManager.getSettingByName(this, "Coordinaten").getValBoolean();
        boolean showGameTime = Galacticc.instance.settingsManager.getSettingByName(this, "Spielzeit").getValBoolean();
        boolean showSystemTime = Galacticc.instance.settingsManager.getSettingByName(this, "Systemzeit").getValBoolean();
        boolean showMinecraftDay = Galacticc.instance.settingsManager.getSettingByName(this, "SpielTag").getValBoolean();
        boolean showPing = Galacticc.instance.settingsManager.getSettingByName(this, "Ping").getValBoolean();
        boolean showDate = Galacticc.instance.settingsManager.getSettingByName(this, "Systemdatum").getValBoolean();
        boolean showItemInfo = Galacticc.instance.settingsManager.getSettingByName(this, "Hand Info").getValBoolean();
        boolean showPotions = Galacticc.instance.settingsManager.getSettingByName(this, "Traenke").getValBoolean();
        boolean showCPU = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige CPU").getValBoolean();

        List<String[]> infoList = new ArrayList<>();
        if (showCPU) infoList.add(new String[]{"CPU Usage", String.format("%.1f%%", cpuUsage)});
        if (showFPS) infoList.add(new String[]{"FPS", String.valueOf(Minecraft.getDebugFPS())});
        if (showCoords)
            infoList.add(new String[]{"Coordinates", String.format("X: %.1f, Y: %.1f, Z: %.1f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)});
        if (showGameTime) {
            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            long elapsedSeconds = elapsedMillis / 1000;
            infoList.add(new String[]{"Spielzeit", String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60)});
        }
        if (showSystemTime)
            infoList.add(new String[]{"Systemzeit", new SimpleDateFormat("HH:mm:ss").format(new Date())});
        if (showMinecraftDay)
            infoList.add(new String[]{"SpielTag", String.valueOf(mc.theWorld.getWorldTime() / 24000)});
        if (showPing) {
            EntityPlayerSP player = mc.thePlayer;
            int ping = player != null && mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfo(player.getUniqueID()) != null
                    ? mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getResponseTime() : -1;
            infoList.add(new String[]{"Ping", ping == -1 ? "N/A" : ping + " ms"});
        }
        if (showDate) infoList.add(new String[]{"Datum", new SimpleDateFormat("dd-MM-yyyy").format(new Date())});
        if (showItemInfo && mc.thePlayer.getHeldItem() != null) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            String itemName = heldItem.getDisplayName();
            int durability = heldItem.getMaxDamage() - heldItem.getItemDamage();
            infoList.add(new String[]{"Hand", itemName + " (" + durability + " Durability)"});
        }
        if (showPotions) {
            for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                if (effect != null) {
                    String potionName = EnumChatFormatting.getTextWithoutFormattingCodes(effect.getEffectName());
                    int duration = effect.getDuration() / 20; // Convert ticks to seconds
                    infoList.add(new String[]{"Traenke", potionName + " (" + duration + "s)"});
                }
            }
        }

        int moduleIndex = 0;
        for (String[] entry : infoList) {
            String label = entry[0] + " : ";
            String value = entry[1];

            int labelRenderColor;
            int valueRenderColor;

            if (useGradient) {
                labelRenderColor = calculateGradientColor(moduleIndex, infoList.size(), labelRed, labelGreen, labelBlue, gradRed, gradGreen, gradBlue, gradientSpeed);
                valueRenderColor = labelRenderColor;
            } else {
                labelRenderColor = (labelAlpha << 24) | (labelRed << 16) | (labelGreen << 8) | labelBlue;
                valueRenderColor = (valueAlpha << 24) | (valueRed << 16) | (valueGreen << 8) | valueBlue;
            }
//todo rewrite this alpha value bs, bit messy, only sets back at 5 %-
            renderText(fr, label, xPos, yPos, backgroundColor, labelRenderColor);
            renderText(fr, value, xPos + fr.getStringWidth(label), yPos, 0, valueRenderColor);

            yPos += 12;
            moduleIndex++;
        }
    }

    private void updateCPUUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.cpuUsage = osBean.getSystemCpuLoad() * 100;
    }

    private int calculateGradientColor(int index, int total, int labelRed, int labelGreen, int labelBlue, int gradRed, int gradGreen, int gradBlue, double gradientSpeed) {

        long currentTime = System.currentTimeMillis();
        double animatedProgress = (currentTime / 1000.0) * gradientSpeed;

        double progress = ((double) index / (double) total + animatedProgress) % 1.0;

        double sineProgress = (Math.sin(progress * 2 * Math.PI - Math.PI / 2) + 1) / 2;

        int red = (int) ((1 - sineProgress) * labelRed + sineProgress * gradRed);
        int green = (int) ((1 - sineProgress) * labelGreen + sineProgress * gradGreen);
        int blue = (int) ((1 - sineProgress) * labelBlue + sineProgress * gradBlue);

        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    private void renderText(FontRenderer fr, String text, int x, int y, int backgroundColor, int textColor) {
        if (backgroundColor != 0) {
            Gui.drawRect(x - 2, y - 2, x + fr.getStringWidth(text) + 2, y + fr.FONT_HEIGHT + 2, backgroundColor);
        }
        fr.drawStringWithShadow(text, x, y, textColor);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        this.active = true;
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            updateCPUUsage();
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        this.active = false;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
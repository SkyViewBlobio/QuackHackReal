package me.kopamed.galacticc.module.textstuff;

import com.sun.management.OperatingSystemMXBean;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
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
    private final long gameStartTime;
    private final Minecraft mc;
    private ScheduledExecutorService executor;
    private double cpuUsage = 0.0;
    private double deathX;
    private double deathY;
    private double deathZ;
    private boolean deathCoordinatesSet = false;

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
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige TPS", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Speicher Info", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige Geschwindigkeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige Kristalle", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige XP", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gerenderte Entitaeten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Death Coordinates", this, true));

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

        if (mc == null || mc.world == null || mc.player == null) {
            return;
        }

        FontRenderer fr = mc.fontRenderer;

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
        boolean showTPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige TPS").getValBoolean();
        boolean showMemory = Galacticc.instance.settingsManager.getSettingByName(this, "Speicher Info").getValBoolean();
        boolean showSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige Geschwindigkeit").getValBoolean();
        boolean showCrystals = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige Kristalle").getValBoolean();
        boolean showExperience = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige XP").getValBoolean();
        boolean showEntities = Galacticc.instance.settingsManager.getSettingByName(this, "Gerenderte Entitaeten").getValBoolean();
        boolean showDeathCoordinates = Galacticc.instance.settingsManager.getSettingByName(this, "Death Coordinates").getValBoolean();

        List<String[]> infoList = new ArrayList<>();
        if (showCPU) infoList.add(new String[]{"CPU Usage", String.format("%.1f%%", cpuUsage)});
        if (showFPS) infoList.add(new String[]{"FPS", String.valueOf(Minecraft.getDebugFPS())});
        if (showCoords) infoList.add(new String[]{"Coordinates", String.format("X: %.1f, Y: %.1f, Z: %.1f", mc.player.posX, mc.player.posY, mc.player.posZ)});
        if (showGameTime) {
            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            long elapsedSeconds = elapsedMillis / 1000;
            infoList.add(new String[]{"Spielzeit", String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60)});
        }
        if (showSystemTime) infoList.add(new String[]{"Systemzeit", new SimpleDateFormat("HH:mm:ss").format(new Date())});
        if (showMinecraftDay) infoList.add(new String[]{"SpielTag", String.valueOf(mc.world.getWorldTime() / 24000)});
        if (showPing && mc.getConnection() != null && mc.player != null) {
            int ping = mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime();
            infoList.add(new String[]{"Ping", ping + " ms"});
        }
        if (showDate) infoList.add(new String[]{"Datum", new SimpleDateFormat("dd-MM-yyyy").format(new Date())});
        if (showItemInfo && mc.player != null) {
            ItemStack heldItem = mc.player.getHeldItemMainhand();
            if (!heldItem.isEmpty()) {
                String itemName = heldItem.getDisplayName();
                int durability = heldItem.getMaxDamage() - heldItem.getItemDamage();
                infoList.add(new String[]{"Hand", itemName + " (" + durability + " Durability)"});
            }
        }

        if (showTPS) infoList.add(new String[]{"TPS", getTPS()});
        if (showMemory) {
            long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
            infoList.add(new String[]{"Speicher", usedMemory + " MB / " + maxMemory + " MB"});
        }
        if (showSpeed && mc.player != null) {
            double speed = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ) * 20.0;
            int roundedSpeed = (int) Math.round(speed); // Round to the nearest whole number
            infoList.add(new String[]{"Geschwindigkeit", roundedSpeed + " blocks/s"});
        }
        if (showCrystals) {
            int crystalCount = countItemsInInventory(Items.END_CRYSTAL);
            infoList.add(new String[]{"Kristalle", String.valueOf(crystalCount)});
        }
        if (showExperience) {
            int xpBottleCount = countItemsInInventory(Items.EXPERIENCE_BOTTLE);
            infoList.add(new String[]{"XP", String.valueOf(xpBottleCount)});
        }
        if (showEntities) {
            int renderedEntities = mc.world.loadedEntityList.size();
            infoList.add(new String[]{"Gerenderte Entitaegraten", String.valueOf(renderedEntities)});
        }
        if (showDeathCoordinates) {
            if (mc.currentScreen instanceof GuiGameOver && mc.player != null) {
                deathX = mc.player.posX;
                deathY = mc.player.posY;
                deathZ = mc.player.posZ;
                deathCoordinatesSet = true;
            }

            if (deathCoordinatesSet) {
                infoList.add(new String[]{"Death", String.format("X: %.1f, Y: %.1f, Z: %.1f", deathX, deathY, deathZ)});
            }
        }

        if (!showDeathCoordinates) {
            deathCoordinatesSet = false;
        }
        int adjustedYPos = yPos;
        if (showPotions && mc.player != null) {
            int potionCount = 0;
            for (PotionEffect effect : mc.player.getActivePotionEffects()) {
                String potionName = I18n.format(effect.getPotion().getName());
                potionName = Character.toUpperCase(potionName.charAt(0)) + potionName.substring(1);

                int level = effect.getAmplifier() + 1;
                int duration = effect.getDuration() / 20;

                infoList.add(new String[]{"Traenke", potionName + " | Level " + level + " | (" + duration + "s)"});
                potionCount++;
            }

            int potionOffsetY = potionCount * 12;
            adjustedYPos = yPos - potionOffsetY;
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

            renderText(fr, label, xPos, adjustedYPos, backgroundColor, labelRenderColor);
            renderText(fr, value, xPos + fr.getStringWidth(label), adjustedYPos, 0, valueRenderColor);

            adjustedYPos += 12;
            moduleIndex++;
        }
    }

    private String getTPS() {
        if (mc.isSingleplayer()) {
            // Singleplayer: Use the integrated server
            MinecraftServer server = mc.getIntegratedServer();
            if (server == null) {
                return "N/A";
            }

            long[] tickTimes = server.tickTimeArray;
            if (tickTimes == null || tickTimes.length == 0) {
                return "N/A";
            }

            long totalTickTime = 0;
            for (long time : tickTimes) {
                totalTickTime += time;
            }
            double avgTickTime = totalTickTime / (double) tickTimes.length / 1_000_000.0;

            double tps = avgTickTime > 50.0 ? 1000.0 / avgTickTime : 20.0;

            return String.format("%.2f", tps);
        } else {
            // Multiplayer: Approximate TPS based on ping
            if (mc.getConnection() == null || mc.player == null) {
                return "N/A";
            }

            // Get player info (guaranteed non-null)
            NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());

            // Get ping time to the server
            long ping = playerInfo.getResponseTime();

            // Estimate TPS assuming ideal conditions
            double estimatedTPS = ping > 0 ? Math.min(20.0, 1000.0 / (ping / 2.0)) : 20.0;

            return String.format("%.2f", estimatedTPS);
        }
    }

    private int countItemsInInventory(Item item) {
        int count = 0;
        for (ItemStack stack : mc.player.inventory.mainInventory) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
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
    //todo rewrite this alpha value bs, bit messy, only sets back at 5 %-
    @Override
    public void onEnabled() {
        super.onEnabled();
        this.active = true;
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::updateCPUUsage, 0, 1, TimeUnit.SECONDS);
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
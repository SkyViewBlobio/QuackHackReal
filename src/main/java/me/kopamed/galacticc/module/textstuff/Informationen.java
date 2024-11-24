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
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Informationen extends Module {

    private boolean active;
    private long gameStartTime;
    private Minecraft mc;

    public Informationen() {
        super("Informationen", "zeigt dir zeugs", false, false, Category.TEXTSTUFF);

        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige FPS", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Coordinaten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Spielzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Systemzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SpielTag", this, true));
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
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Value Blau", this, 0, 0, 255, true));

        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund Dichte", this, 144, 0, 255, true));

        this.mc = Minecraft.getMinecraft();
        this.gameStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (Galacticc.instance.destructed || !this.active) {
            return;
        }

        FontRenderer fr = mc.fontRendererObj;

        // Fetch offsets
        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

        // Fetch label and value colors
        int labelRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Rot").getValDouble();
        int labelGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Green").getValDouble();
        int labelBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Label Blau").getValDouble();
        int valueRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Rot").getValDouble();
        int valueGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Green").getValDouble();
        int valueBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Value Blau").getValDouble();

        int labelColor = (0xFF << 24) | (labelRed << 16) | (labelGreen << 8) | labelBlue;
        int valueColor = (0xFF << 24) | (valueRed << 16) | (valueGreen << 8) | valueBlue;

        // Fetch background color
        int bgRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Rot").getValDouble();
        int bgGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Green").getValDouble();
        int bgBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Blau").getValDouble();
        int bgAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Hintergrund Dichte").getValDouble();
        int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;

        // Positioning
        ScaledResolution sr = new ScaledResolution(mc);
        int xPos = sr.getScaledWidth() / 2 + xOffset;
        int yPos = sr.getScaledHeight() / 2 - 40 + yOffset;

        // Fetch settings
        boolean showFPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
        boolean showCoords = Galacticc.instance.settingsManager.getSettingByName(this, "Coordinaten").getValBoolean();
        boolean showGameTime = Galacticc.instance.settingsManager.getSettingByName(this, "Spielzeit").getValBoolean();
        boolean showSystemTime = Galacticc.instance.settingsManager.getSettingByName(this, "Systemzeit").getValBoolean();
        boolean showMinecraftDay = Galacticc.instance.settingsManager.getSettingByName(this, "SpielTag").getValBoolean();
        boolean showPing = Galacticc.instance.settingsManager.getSettingByName(this, "Ping").getValBoolean();
        boolean showDate = Galacticc.instance.settingsManager.getSettingByName(this, "Systemdatum").getValBoolean();
        boolean showItemInfo = Galacticc.instance.settingsManager.getSettingByName(this, "Hand Info").getValBoolean();
        boolean showPotions = Galacticc.instance.settingsManager.getSettingByName(this, "Traenke").getValBoolean();

        // Gather information to display
        List<String[]> infoList = new ArrayList<>();
        if (showFPS) infoList.add(new String[]{"FPS", String.valueOf(Minecraft.getDebugFPS())});
        if (showCoords) infoList.add(new String[]{"Coordinates", String.format("X: %.1f, Y: %.1f, Z: %.1f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)});
        if (showGameTime) {
            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            long elapsedSeconds = elapsedMillis / 1000;
            infoList.add(new String[]{"Spielzeit", String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60)});
        }
        if (showSystemTime) infoList.add(new String[]{"Systemzeit", new SimpleDateFormat("HH:mm:ss").format(new Date())});
        if (showMinecraftDay) infoList.add(new String[]{"SpielTag", String.valueOf(mc.theWorld.getWorldTime() / 24000)});
        if (showPing) {
            EntityPlayerSP player = mc.thePlayer;
            int ping = player != null && mc.getNetHandler() != null ? mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getResponseTime() : -1;
            infoList.add(new String[]{"Ping", ping == -1 ? "N/A" : ping + " ms"});
        }
        if (showDate) infoList.add(new String[]{"Datum", new SimpleDateFormat("dd-MM-yyyy").format(new Date())});
        if (showItemInfo) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null) {
                String itemName = heldItem.getDisplayName();
                int durability = heldItem.getMaxDamage() - heldItem.getItemDamage();
                infoList.add(new String[]{"Hand", itemName + " (" + durability + " Durability)"});
            }
        }
        if (showPotions) {
            for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                String potionName = EnumChatFormatting.getTextWithoutFormattingCodes(effect.getEffectName());
                int duration = effect.getDuration() / 20; // Convert ticks to seconds
                infoList.add(new String[]{"Traenke", potionName + " (" + duration + "s)"});
            }
        }

        // Render each label and value
        for (String[] entry : infoList) {
            String label = entry[0] + " : ";
            String value = entry[1];

            // Render label
            renderText(fr, label, xPos, yPos, backgroundColor, labelColor);

            // Render value immediately after label
            renderText(fr, value, xPos + fr.getStringWidth(label), yPos, 0, valueColor); // No background for value
            yPos += 12; // Move to the next line
        }
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
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        this.active = false;
    }
}


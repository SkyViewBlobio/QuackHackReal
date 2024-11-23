package me.kopamed.galacticc.module.render;

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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Informationen extends Module {

    private boolean active;
    private long gameStartTime;
    private Minecraft mc;

    public Informationen() {
        super("Informationen", "zeigt dir zeugs", false, false, Category.VISUELLES);

        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige FPS", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Coordinaten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Spielzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Systemzeit", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SpielTag", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Black Background", this, true));

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 0, -screenWidth, screenWidth, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -screenHeight, screenHeight, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Flipped Text", this, false));

        this.mc = Minecraft.getMinecraft();
        this.gameStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (Galacticc.instance.destructed || !this.active) {
            return;
        }

        FontRenderer fr = mc.fontRendererObj;

        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

        ScaledResolution sr = new ScaledResolution(mc);
        int xPos = sr.getScaledWidth() / 2 + xOffset;
        int yPos = sr.getScaledHeight() / 2 - 40 + yOffset;

        boolean showFPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
        boolean showCoords = Galacticc.instance.settingsManager.getSettingByName(this, "Coordinaten").getValBoolean();
        boolean showGameTime = Galacticc.instance.settingsManager.getSettingByName(this, "Spielzeit").getValBoolean();
        boolean showSystemTime = Galacticc.instance.settingsManager.getSettingByName(this, "Systemzeit").getValBoolean();
        boolean showMinecraftDay = Galacticc.instance.settingsManager.getSettingByName(this, "SpielTag").getValBoolean();
        boolean blackBackground = Galacticc.instance.settingsManager.getSettingByName(this, "Black Background").getValBoolean();
        boolean flippedText = Galacticc.instance.settingsManager.getSettingByName(this, "Flipped Text").getValBoolean();

        if (!showFPS && !showCoords && !showGameTime && !showSystemTime && !showMinecraftDay) {
            return;
        }

        int namePaddingLength = 12;
        int valuePaddingLength = 20;

        if (showFPS) {
            String fps = formatText("FPS", String.valueOf(Minecraft.getDebugFPS()), flippedText, namePaddingLength, valuePaddingLength);
            renderText(fr, fps, xPos, yPos, blackBackground);
            yPos += 12;
        }

        if (showCoords) {
            String coords = String.format("X: %.1f, Y: %.1f, Z: %.1f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            coords = formatText("Coordinates", coords, flippedText, namePaddingLength, valuePaddingLength);
            renderText(fr, coords, xPos, yPos, blackBackground);
            yPos += 12;
        }

        if (showGameTime) {
            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            long elapsedSeconds = elapsedMillis / 1000;
            String gameTime = String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60);
            gameTime = formatText("Spielzeit", gameTime, flippedText, namePaddingLength, valuePaddingLength);
            renderText(fr, gameTime, xPos, yPos, blackBackground);
            yPos += 12;
        }

        if (showSystemTime) {
            String systemTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
            systemTime = formatText("Systemzeit", systemTime, flippedText, namePaddingLength, valuePaddingLength);
            renderText(fr, systemTime, xPos, yPos, blackBackground);
            yPos += 12;
        }

        if (showMinecraftDay) {
            long worldTime = mc.theWorld.getWorldTime();
            String inGameDays = String.valueOf(worldTime / 24000);
            inGameDays = formatText("SpielTag", inGameDays, flippedText, namePaddingLength, valuePaddingLength);
            renderText(fr, inGameDays, xPos, yPos, blackBackground);
        }
    }

    private String formatText(String name, String value, boolean flipped, int namePaddingLength, int valuePaddingLength) {
        String paddedName = String.format("%-" + namePaddingLength + "s", name);
        String paddedValue = String.format("%-" + valuePaddingLength + "s", value);

        return flipped ? paddedValue + " : " + name : paddedName + " : " + value;
    }

    private void renderText(FontRenderer fr, String text, int x, int y, boolean blackBackground) {
        int textWidth = fr.getStringWidth(text);
        if (blackBackground) {
            Gui.drawRect(x - 2, y - 2, x + textWidth + 2, y + fr.FONT_HEIGHT + 2, 0x90000000);
        }
        fr.drawStringWithShadow(text, x, y, 0xFFFFFF);
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

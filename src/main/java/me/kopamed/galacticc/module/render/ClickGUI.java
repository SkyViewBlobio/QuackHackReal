package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    private int previousFpsCap;

    public ClickGUI() {
        super("Menu", "Allows you to manage modules", false, true, Category.VISUELLES);
        this.setKey(Keyboard.KEY_RSHIFT); // Default keybind

        //  color sliders for header
        Galacticc.instance.settingsManager.rSetting(new Setting("Header Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Header Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Header Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Header Alpha", this, 255, 0, 255, true));

        //button class || the module color itself.
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul Alpha", this, 0, 0, 255, true));

        // the container inside the line render. (make only visible if a module is unfolded, right now its visible at all times.
        Galacticc.instance.settingsManager.rSetting(new Setting("Hinter-Linie Red", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hinter-Linie Green", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hinter-Linie Blue", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hinter-Linie Alpha", this, 150, 0, 255, true));

        // Module hover color
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul-Hover Rot", this, 50, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul-Hover Green", this, 50, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul-Hover Blau", this, 50, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Modul-Hover Alpha", this, 200, 0, 255, true));

        // CLICKGUI opaque BG
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund2 Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund2 Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund2 Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hintergrund2 Alpha", this, 200, 0, 255, true)); // Max transparency

        // if we hover over a boolean it colors it.
        Galacticc.instance.settingsManager.rSetting(new Setting("Option Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Option Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Option Blau", this, 255, 0, 255, true));

        // colors the lines around the modules.
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Rot", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Green", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Blau", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Alpha", this, 150, 0, 255, true));

        // secondary colors for gradients
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Enable Gradient Header", this, false));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        Minecraft mc = Minecraft.getMinecraft();
        previousFpsCap = mc.gameSettings.limitFramerate;
        mc.gameSettings.limitFramerate = 25;

        // Open the GUI
        mc.displayGuiScreen(Galacticc.instance.clickGui);

        // Ensure GUI opens cleanly
        Galacticc.instance.clickGui.frames.forEach(frame -> frame.setOpen(false)); // Close all frames initially
        this.setToggled(false); // Reset toggle state
    }

    public int getPreviousFpsCap() {
        return previousFpsCap;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        Minecraft.getMinecraft().gameSettings.limitFramerate = previousFpsCap;
    }
}

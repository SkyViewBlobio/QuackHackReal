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

        //  alpha slider for transparency
        Galacticc.instance.settingsManager.rSetting(new Setting("Header Alpha", this, 255, 0, 255, true));

        // secondary colors for gradients
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Header Blau", this, 0, 0, 255, true));

        //  toggle for gradient mode
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

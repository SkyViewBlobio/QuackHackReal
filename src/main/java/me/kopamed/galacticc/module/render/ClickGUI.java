package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    private int previousFpsCap; // Store the original FPS cap

    public ClickGUI() {
        super("Menü", "Allows you to manage modules", false, true, Category.VISUELLES);
        this.setKey(Keyboard.KEY_RSHIFT); // Default keybind
    }

    @Override
    public void onEnabled() {
        super.onEnabled();

        // Store the current FPS cap and apply the new cap
        Minecraft mc = Minecraft.getMinecraft();
        previousFpsCap = mc.gameSettings.limitFramerate;
        mc.gameSettings.limitFramerate = 25; // Cap FPS to 25

        // Open the GUI
        mc.displayGuiScreen(Galacticc.instance.clickGui);

        // Disable this module to avoid re-triggering
        this.setToggled(false);
    }
    public int getPreviousFpsCap() {
        return previousFpsCap;
    }
    @Override
    public void onDisabled() {
        super.onDisabled();

        // Restore the original FPS cap when the module is explicitly toggled off
        Minecraft.getMinecraft().gameSettings.limitFramerate = previousFpsCap;
    }
}

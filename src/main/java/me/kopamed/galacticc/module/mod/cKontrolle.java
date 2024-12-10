package me.kopamed.galacticc.module.mod;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import java.util.ArrayList;

public class cKontrolle extends Module {

    public cKontrolle() {
        super("C-Kontrolle", "Lets you customize things about the client globally", false, false, Category.MOD);
        //todo make this update on the client
        // Add mode settings
        ArrayList<String> modeOptions = new ArrayList<>();
        modeOptions.add("Komplex"); // Show all HUD info
        modeOptions.add("Simpel");  // Hide all HUD info
        Galacticc.instance.settingsManager.rSetting(new Setting("Kontrolle Modus", this, "Komplex", modeOptions));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        updateMode();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        Module.hideHUDInfo = false; // Reset to default when disabled
    }

    public void updateMode() {
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Kontrolle Modus").getValString();
        Module.hideHUDInfo = mode.equals("Simpel"); // Set the global flag based on the mode
    }
}

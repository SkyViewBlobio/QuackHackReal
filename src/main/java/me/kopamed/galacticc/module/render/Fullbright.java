package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;

public class Fullbright extends Module {
    private float defGamma;
    public Fullbright() {
        super("Helligkeit", "Hauptinformationen: " +
                "Macht das licht an ;^)", false, false, Category.VISUELLES);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        defGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10F;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        mc.gameSettings.gammaSetting = defGamma;
    }
}

package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;

public class Fullbright extends Module {
    private float defGamma;
    public Fullbright() {
        super("Helligkeit", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Macht das Licht an ;^)",
                false, false, Category.VISUELLES);
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

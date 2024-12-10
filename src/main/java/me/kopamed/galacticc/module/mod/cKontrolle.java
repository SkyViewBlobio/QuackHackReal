package me.kopamed.galacticc.module.mod;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

/**
 * GLOBAL MODULE ALWAYS ACTIVE !
 *
 * this module is one of the future global manager modules. it does not matter if the module is enabled or not.
 *
 */

public class cKontrolle extends Module {

    private String lastMode; // Track the last mode to detect changes
    private long lastCheckTime; // To manage 10-second intervals

    public cKontrolle() {
        super("C-Kontrolle", "((GLOBALES MODUL ES DIESES MODUL KANN NICHT AUSGESCHALTET WERDEN!)) Hauptinformationen: Dieses Modul laesst dich die extra Informationen in den Modulnamen verbergen oder anzeigen. " +
                "Wenn du die extra Informationen im 'Simpel' Modus siehst, warte 3 Minuten denn der Mod stellt deine Optionen automatisch zurueck.", false, true, Category.MOD);

        ArrayList<String> modeOptions = new ArrayList<>();
        modeOptions.add("Komplex"); // Show all HUD info
        modeOptions.add("Simpel");  // Hide all HUD info
        Galacticc.instance.settingsManager.rSetting(new Setting("Kontrolle Modus", this, "Komplex", modeOptions));

        lastMode = Galacticc.instance.settingsManager.getSettingByName(this, "Kontrolle Modus").getValString();
        lastCheckTime = System.currentTimeMillis();
        updateMode();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (System.currentTimeMillis() - lastCheckTime > 280000) {
            lastCheckTime = System.currentTimeMillis();
            updateMode();
        }
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        updateMode();
    }

    @Override
    public void onLoad() {
        updateMode();
    }

    @Override
    public void onDisabled() {
        setToggled(true);
    }

    public void updateMode() {
        String currentMode = Galacticc.instance.settingsManager.getSettingByName(this, "Kontrolle Modus").getValString();
        if (!currentMode.equals(lastMode)) {
            lastMode = currentMode;
            Module.hideHUDInfo = currentMode.equals("Simpel");
            System.out.println("Updated Kontrolle Mode: " + currentMode);
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientChatReceivedEvent event) {
        updateMode();
    }
}

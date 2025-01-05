//still working on it

package me.kopamed.galacticc.module.player;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class FastPlace extends Module {
    private int delay;
    private boolean onlyBlocks;
    private Field delayTimer;

    public FastPlace() {
        super("Schnellplatzieren", "Places blocks faster", false, false, Category.SPIELER);

        Galacticc.instance.settingsManager.rSetting(new Setting("Delay", this, 2, 0, 20, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Only Blocks", this, true));

        try {
            delayTimer = mc.getClass().getDeclaredField("rightClickDelayTimer");
            delayTimer.setAccessible(true);
        } catch (Exception e) {
            delayTimer = null; // If reflection fails, set delayTimer to null
        }
    }

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent e) {
        if (Galacticc.instance.destructed) return;

        delay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Delay").getValDouble();
        onlyBlocks = Galacticc.instance.settingsManager.getSettingByName(this, "Only Blocks").getValBoolean();

        if (e.phase == TickEvent.Phase.END) {
            if (onlyBlocks) {//todo cleanup and try to see if this even works? fix also check console
                ItemStack item = e.player.getHeldItem(EnumHand.MAIN_HAND);
                if (item.isEmpty() || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            try {
                if (delayTimer != null) {
                    if (delay == 0) {
                        delayTimer.set(mc, delay);
                    } else {
                        int currentDelay = delayTimer.getInt(mc);
                        if (currentDelay == 4) {
                            delayTimer.set(mc, delay);
                        }
                    }
                }
            } catch (Exception ex) {
                this.toggle(); // Disable the module on error
            }
        }
    }

    @Override
    public void onEnabled() {
        if (delayTimer == null) {
            this.toggle(); // Disable if delayTimer is unavailable
        }
        super.onEnabled();
    }
}
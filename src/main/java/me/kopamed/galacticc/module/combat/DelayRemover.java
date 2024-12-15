package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class DelayRemover extends Module {
    private boolean leftDisable, rightDisable;
    private Field leftCap = null;
    private Field rightCap = null;

    public DelayRemover() {
        super("-Umgeher", "@Hauptinformation: " +
                "Laesst dich die Verzoegerung von rechts/links Klicks ausschalten.", false, false, Category.ANGRIFF);

        Setting leftDisable = new Setting("Remove LeftClick Delay", this, true);
        Setting rightDisable = new Setting("Remove RightClick Delay", this, true);

        Galacticc.instance.settingsManager.rSetting(leftDisable);
        Galacticc.instance.settingsManager.rSetting(rightDisable);
    }

    @Override
    public void onEnabled() {
        updateValues();
        if (leftDisable) {
            try {
                this.leftCap = mc.getClass().getDeclaredField("field_71429_W");
            } catch (Exception var4) {
                try {
                    this.leftCap = mc.getClass().getDeclaredField("leftClickCounter");
                } catch (Exception var3) {
                }
            }

            if (this.leftCap != null) {
                System.out.println("Made left accessible");
                this.leftCap.setAccessible(true);
            } else {
                this.toggle();
            }
        }

        if (rightDisable) {
            try {
                this.rightCap = mc.getClass().getDeclaredField("rightClickCounter");
            } catch (Exception var4) {
            }


            if (this.rightCap != null) {
                System.out.println("Made right accessible");
                this.rightCap.setAccessible(true);
            } else {
                this.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        updateValues();
        if(leftDisable) {
            if (mc.thePlayer != null && mc.theWorld != null) {
                if (!mc.inGameHasFocus || mc.thePlayer.capabilities.isCreativeMode) {
                    return;
                }

                try {
                    this.leftCap.set(mc, 0);
                } catch (Exception what3) {
                    what3.printStackTrace();
                }
            }
        }

        if(rightDisable) {
            if (mc.thePlayer != null && mc.theWorld != null) {
                if (!mc.inGameHasFocus || mc.thePlayer.capabilities.isCreativeMode) {
                    return;
                }

                try {
                    this.rightCap.set(mc, 0);
                } catch (Exception what4) {
                    what4.printStackTrace();
                }
            }
        }
    }

    public void updateValues() {
        leftDisable = Galacticc.instance.settingsManager.getSettingByName(this, "Remove LeftClick Delay").getValBoolean();
        rightDisable = Galacticc.instance.settingsManager.getSettingByName(this, "Remove RightClick Delay").getValBoolean();

        if (!leftDisable && !rightDisable) {
            this.toggle();
        }
    }
}

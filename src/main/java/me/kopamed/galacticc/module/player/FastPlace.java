package me.kopamed.galacticc.module.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class FastPlace extends Module {
    private Field delayTimerField;

    public FastPlace() {
        super("Schnellplatzieren", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Reduziert die Verzögerung beim Platzieren von Blöcken, wodurch Blockplatzierung schneller erfolgt." + ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Verwenden Sie diese Funktion, um Blöcke schneller zu platzieren, indem Sie die Verzögerung zwischen Platzierungen verringern.",
                false,
                false,
                Category.SPIELER);

        Galacticc.instance.settingsManager.rSetting(new Setting("Delay", this, 0, 0, 20, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Only Blocks", this, true));

        try {
            delayTimerField = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71467_ac");
            delayTimerField.setAccessible(true);
        } catch (Exception e) {
            delayTimerField = null;
        }
    }

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (Galacticc.instance.destructed) return;

        int delay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Delay").getValDouble();
        boolean onlyBlocks = Galacticc.instance.settingsManager.getSettingByName(this, "Only Blocks").getValBoolean();

        if (event.phase == TickEvent.Phase.END) {
            if (onlyBlocks) {
                ItemStack item = event.player.getHeldItem(EnumHand.MAIN_HAND);
                if (item.isEmpty() || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            if (delayTimerField != null) {
                try {
                    int currentDelay = delayTimerField.getInt(mc);
                    if (currentDelay != delay) {
                        delayTimerField.set(mc, delay);
                    }
                } catch (Exception e) {
                    this.toggle();
                }
            } else {
                this.toggle();
            }
        }
    }

    @Override
    public void onEnabled() {
        if (delayTimerField == null) {
            this.toggle();
        }
        super.onEnabled();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (delayTimerField != null) {
            try {
                delayTimerField.set(mc, 4);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
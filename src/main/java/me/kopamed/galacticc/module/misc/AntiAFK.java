package me.kopamed.galacticc.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class AntiAFK extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    private long afkStartTime = 0; // Time when the module was turned on
    private long lastActionTime = 0; // Last time an action was performed

    public AntiAFK() {
        super("AntiAFK", "Prevents you from looking AFK by performing actions", false, false, Category.SONSTIGES);

        //************************Delay Timer Setting**************************
        Galacticc.instance.settingsManager.rSetting(new Setting("Delay Timer", this, 5, 1, 30, true));

        //************************Action Settings**************************
        Galacticc.instance.settingsManager.rSetting(new Setting("Punch", this, true)); // Default: Punch enabled
        Galacticc.instance.settingsManager.rSetting(new Setting("Sneak", this, false)); // Default: Sneak disabled
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        afkStartTime = System.currentTimeMillis(); // Start counting AFK time
        lastActionTime = System.currentTimeMillis(); // Initialize last action time
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        afkStartTime = 0; // Reset AFK start time
        lastActionTime = 0; // Reset last action time
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        //************************Check Delay Timer**************************
        long delay = (long) (Galacticc.instance.settingsManager.getSettingByName(this, "Delay Timer").getValDouble() * 1000); // Convert seconds to milliseconds
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastActionTime >= delay) {
            performAFKAction();
            lastActionTime = currentTime; // Reset last action time
        }
    }

    private void performAFKAction() {
        boolean punchEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "Punch").getValBoolean();
        boolean sneakEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "Sneak").getValBoolean();

        if (punchEnabled) {
            mc.thePlayer.swingItem(); // Perform punching animation
        }

        if (sneakEnabled) {
            // Simulate pressing the sneak key
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true); // Start sneaking
            mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C0BPacketEntityAction(mc.thePlayer, net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING));

            try {
                Thread.sleep(100); // Short delay to simulate sneak press
            } catch (InterruptedException ignored) {
            }

            // Simulate releasing the sneak key
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false); // Stop sneaking
            mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C0BPacketEntityAction(mc.thePlayer, net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public String getHUDInfo() {
        if (afkStartTime == 0) {
            return "Tage: 0, Stunden: 0, Minuten: 0, Sekunden: 0,"; // Default when module is off
        }

        long elapsedTime = System.currentTimeMillis() - afkStartTime;

        // Convert elapsed time to days, hours, minutes, and seconds
        long days = elapsedTime / (1000 * 60 * 60 * 24);
        elapsedTime %= (1000 * 60 * 60 * 24);

        long hours = elapsedTime / (1000 * 60 * 60);
        elapsedTime %= (1000 * 60 * 60);

        long minutes = elapsedTime / (1000 * 60);
        elapsedTime %= (1000 * 60);

        long seconds = elapsedTime / 1000;

        return  ChatFormatting.GRAY +  "Tage: "  + ChatFormatting.GRAY + days + ChatFormatting.GRAY + ", Stunden: " + ChatFormatting.GRAY + hours + ", Minuten: " + ChatFormatting.GRAY + minutes + ", Sekunden: " + ChatFormatting.GRAY + seconds + ",";
    }
}

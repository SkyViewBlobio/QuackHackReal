package me.kopamed.galacticc.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoFish extends Module {

    private final Minecraft mc;
    private boolean isWaitingForFish;
    private long lastCastTime;
    private long reelStartTime;
    private int castCount;

    public AutoFish() {
        super("AutoFisch", "Automatically reels in the fish and casts the fishing rod for you", false, false, Category.SONSTIGES);
        this.mc = Minecraft.getMinecraft();
        this.isWaitingForFish = false;
        this.lastCastTime = 0;
        this.reelStartTime = 0;
        this.castCount = 0;
    }

    //************************Initialization and Toggle State Management**************************//

    @Override
    public void onEnabled() {
        super.onEnabled();
        this.isWaitingForFish = false; // Reset state when enabled
        this.lastCastTime = 0;        // Reset last cast time
        this.reelStartTime = 0;      // Reset reel start time
        this.castCount = 0;         // Reset cast count
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        this.castCount = 0;         // Reset cast count on disable
    }

    //************************Main Logic for Fishing Automation**************************//

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || mc.thePlayer == null || mc.theWorld == null) {
            return; // Exit if the module is disabled or the player/world is null
        }

        // Check if the player is holding a fishing rod
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        boolean holdingFishingRod = heldItem != null && heldItem.getItem() == Items.fishing_rod;

        if (!holdingFishingRod) {
            isWaitingForFish = false; // Reset waiting state if no rod is held
            castCount = 0; // Reset cast count if switching items
            return; // Exit if not holding a fishing rod
        }

        // Cast the rod if not already cast
        if (mc.thePlayer.fishEntity == null) {
            if (!isWaitingForFish && System.currentTimeMillis() - lastCastTime > 1000) {
                performRightClick(); // Cast the rod
                isWaitingForFish = true; // Mark as waiting for fish
                lastCastTime = System.currentTimeMillis(); // Record cast time
                reelStartTime = lastCastTime; // Set reel start time
                castCount++; // Increment cast count
            }
        }
    }

    //************************Reeling in Fish Based on Sound Detection**************************//

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!this.isToggled() || mc.thePlayer == null || mc.theWorld == null) {
            return; // Exit if the module is disabled or the player/world is null
        }

        // Check if the sound is "random.splash" and the player is fishing
        if (event.name.equals("random.splash") && mc.thePlayer.fishEntity != null) {
            performRightClick(); // Reel in the fish
            isWaitingForFish = false; // Reset waiting state
        }
    }

    //************************Simulate Right-Click for Fishing Rod**************************//

    /**
     * Simulates a right-click action using the fishing rod.
     */
    private void performRightClick() {
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, true); // Simulate key press
        KeyBinding.onTick(key);               // Trigger the key action
        KeyBinding.setKeyBindState(key, false); // Release the key
    }

    //************************HUD Info Display**************************//

    @Override
    public String getHUDInfo() {
        // Calculate reel time in seconds
        long reelTimeInSeconds = isWaitingForFish ? (System.currentTimeMillis() - reelStartTime) / 1000 : 0;

        return ChatFormatting.GRAY + "[Wurf-Zahl: " + ChatFormatting.GRAY + castCount + ChatFormatting.GRAY +
                ", Ltz-Fang: " + ChatFormatting.GRAY + reelTimeInSeconds + "s" + ChatFormatting.GRAY + "]";
    }
}

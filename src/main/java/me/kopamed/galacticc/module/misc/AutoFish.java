package me.kopamed.galacticc.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.projectile.EntityFishHook;

public class AutoFish extends Module {

    private final Minecraft mc;
    private boolean isWaitingForFish;
    private boolean isFishing;
    private long lastCastTime;
    private long reelStartTime;
    private long reelTimeInSeconds;
    private int castCount;
//todo check for grounded rod if so recast
    public AutoFish() {
        super("AutoFish", "@HauptInformation: " +
                "Automiert das Angeln.", false, false, Category.SONSTIGES);
        this.mc = Minecraft.getMinecraft();
        this.isWaitingForFish = false;
        this.isFishing = false;
        this.reelTimeInSeconds = 0;
        this.lastCastTime = 0;
        this.reelStartTime = 0;
        this.castCount = 0;
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        this.isWaitingForFish = false;
        this.isFishing = false;
        this.reelTimeInSeconds = 0;
        this.lastCastTime = 0;
        this.reelStartTime = 0;
        this.castCount = 0;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        this.castCount = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) {
            return;
        }

        ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        boolean holdingFishingRod = !heldItem.isEmpty() && heldItem.getItem() == Items.FISHING_ROD;

        if (!holdingFishingRod) {
            isWaitingForFish = false;
            castCount = 0;
            return;
        }

        if (mc.player.fishEntity != null && !(mc.player.fishEntity instanceof EntityFishHook)) {
            isFishing = false;
            isWaitingForFish = false;
            reelStartTime = System.currentTimeMillis();
            reelTimeInSeconds = 0;
        }

        if (mc.player.fishEntity == null) {
            if (!isFishing && System.currentTimeMillis() - lastCastTime > 1000) {
                performRightClick();
                isFishing = true;
                isWaitingForFish = true;
                lastCastTime = System.currentTimeMillis();
                reelStartTime = lastCastTime;
                reelTimeInSeconds = 0;
                castCount++;
            }
        }

        if (mc.player.fishEntity instanceof EntityFishHook) {
            EntityFishHook fishHook = (EntityFishHook) mc.player.fishEntity;

            if (fishHook.isInWater() && isWaitingForFish) {
                if (Math.abs(fishHook.motionY) > 0.15) {
                    if (System.currentTimeMillis() - reelStartTime > 500) {
                        performRightClick();
                        isFishing = false;
                        isWaitingForFish = false;
                        reelStartTime = System.currentTimeMillis();
                    }
                }
            }

            if (fishHook.isInWater()) {
                reelTimeInSeconds = (System.currentTimeMillis() - reelStartTime) / 1000;
            }
        } else {
            reelTimeInSeconds = 0;
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) {
            return;
        }

        if (event.getName().equals("random.splash") && mc.player.fishEntity == null && !isFishing) {
            isFishing = true;
            isWaitingForFish = true;
            lastCastTime = System.currentTimeMillis();
            reelStartTime = lastCastTime;
            reelTimeInSeconds = 0;
        }
    }

    private void performRightClick() {
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        KeyBinding.setKeyBindState(key, false);
    }

    @Override
    public String getHUDInfo() {
        return ChatFormatting.GRAY + "[Wurf-Zahl: " + ChatFormatting.GRAY + castCount + ChatFormatting.GRAY +
                ", Ltz-Fang: " + ChatFormatting.GRAY + reelTimeInSeconds + "s" + ChatFormatting.GRAY + "]";
    }
}
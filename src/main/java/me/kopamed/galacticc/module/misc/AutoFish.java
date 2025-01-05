package me.kopamed.galacticc.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.projectile.EntityFishHook;

public class AutoFish extends Module {

    private final Minecraft mc;
    private boolean isFishing;
    private boolean isWaitingForFish;
    private boolean manualCast;
    private boolean isRecastPending;
    private long lastCastTime;
    private long manualActionTime;
    private int castCount;

    public AutoFish() {
        super("AutoFish", "@HauptInformation: Automiert das Angeln.", false, false, Category.SONSTIGES);
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        resetState();

        // Initial checks for rod state
        if (mc.player != null && mc.player.fishEntity instanceof EntityFishHook) {
            EntityFishHook fishHook = (EntityFishHook) mc.player.fishEntity;

            if (fishHook.isInWater()) {
                isFishing = true;
                isWaitingForFish = true;
                lastCastTime = System.currentTimeMillis();
            } else if (isOnSolidGround(fishHook)) {
                recastRod();
            }
        }
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        resetState();
    }

    private void resetState() {
        isFishing = false;
        isWaitingForFish = false;
        manualCast = false;
        isRecastPending = false;
        lastCastTime = 0;
        manualActionTime = 0;
        castCount = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) return;

        ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        boolean holdingFishingRod = !heldItem.isEmpty() && heldItem.getItem() == Items.FISHING_ROD;

        if (!holdingFishingRod) {
            resetState();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Pause automation if manual actions were detected
        if (manualCast && currentTime - manualActionTime < 3000) return;

        if (mc.player.fishEntity == null) {
            // Rod is not in the water
            if (isFishing && currentTime - lastCastTime > 1000) {
                recastRod();
            }
        } else if (mc.player.fishEntity instanceof EntityFishHook) {
            EntityFishHook fishHook = (EntityFishHook) mc.player.fishEntity;

            // Recast if the hook is on solid ground
            if (!fishHook.isInWater() && isOnSolidGround(fishHook)) {
                if (!isRecastPending) {
                    recastRod();
                    isRecastPending = true;
                }
                return;
            } else {
                isRecastPending = false;
            }

            // Wait for a fish to bite
            if (fishHook.isInWater() && isWaitingForFish) {
                if (Math.abs(fishHook.motionY) > 0.15) {
                    reelFish();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        // Ignore splash sound within 2 seconds of casting
        if (event.getName().equals("random.splash")) {
            if (currentTime - lastCastTime < 2000) return;

            // Register splash sound as a fish bite
            isWaitingForFish = true;
        }
    }

    private void recastRod() {
        performRightClick();
        isFishing = true;
        isWaitingForFish = true;
        lastCastTime = System.currentTimeMillis();
        castCount++;
    }

    private void reelFish() {
        performRightClick();
        isFishing = false;
        isWaitingForFish = false;
        lastCastTime = System.currentTimeMillis();

        // Recast after reeling in
        recastRod();
    }

    private void performRightClick() {
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        KeyBinding.setKeyBindState(key, false);

        // Mark as manual action if not automated
        if (mc.player.fishEntity == null) {
            manualCast = true;
            manualActionTime = System.currentTimeMillis();
        }
    }

    private boolean isOnSolidGround(EntityFishHook fishHook) {
        World world = mc.world;
        BlockPos hookPosition = new BlockPos(fishHook.posX, fishHook.posY - 0.1, fishHook.posZ);
        return !world.isAirBlock(hookPosition);
    }

    @Override
    public String getHUDInfo() {
        return ChatFormatting.GRAY + "[Wurfe: " + castCount + "]";
    }
}

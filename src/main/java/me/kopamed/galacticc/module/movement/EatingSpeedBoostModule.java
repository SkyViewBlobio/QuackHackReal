package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EatingSpeedBoostModule extends Module {

    private static final double BOOSTED_SPEED_MULTIPLIER = 1.2; // Small boost
    private static final double NORMAL_SPEED_MULTIPLIER = 1.0; // Normal speed
    private boolean isBoosting = false;

    public EatingSpeedBoostModule() {
        super("EatingSpeedBoost", "Increases speed slightly while eating", false, false, Category.BEWEGUNG);
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        EntityPlayerSP player = mc.player;

        if (player.isHandActive() && player.getActiveItemStack().getItem() instanceof net.minecraft.item.ItemFood) {
            applyGroundStrafe(player, BOOSTED_SPEED_MULTIPLIER);
            isBoosting = true;
        } else {
            if (isBoosting) {
                resetSpeed(player);
                isBoosting = false;
            }
        }
    }

    private void applyGroundStrafe(EntityPlayerSP player, double speedMultiplier) {
        if (player.moveForward == 0 && player.moveStrafing == 0) {
            return;
        }

        // Get player yaw and movement inputs
        float yaw = player.rotationYaw;
        MovementInput input = player.movementInput;
        double forward = input.moveForward;
        double strafe = input.moveStrafe;

        // Normalize input
        if (forward != 0) {
            if (strafe > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (strafe < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }
        if (strafe != 0) {
            strafe = strafe > 0 ? 1 : -1;
        }

        // Calculate new motion
        double radians = Math.toRadians(yaw + 90);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double motionX = forward * speedMultiplier * cos + strafe * speedMultiplier * sin;
        double motionZ = forward * speedMultiplier * sin - strafe * speedMultiplier * cos;

        // Apply motion boost
        player.motionX += motionX;
        player.motionZ += motionZ;

        // Slight drag for smoother movement
        double dragFactor = 0.98;
        player.motionX *= dragFactor;
        player.motionZ *= dragFactor;
    }

    private void resetSpeed(EntityPlayerSP player) {
        // Reset motion to normal (ground drag)
        double dragFactor = 0.91;
        player.motionX *= dragFactor;
        player.motionZ *= dragFactor;
    }
}

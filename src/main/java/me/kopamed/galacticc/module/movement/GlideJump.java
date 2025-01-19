package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GlideJump extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public GlideJump() {
        super("Speed", "@Hauptinformation: " +
                "Macht dich schneller", true, false, Category.BEWEGUNG);

        // Add slider for strafe speed (0.1 to 2.0)
        Galacticc.instance.settingsManager.rSetting(new Setting("Schnelligkeit", this, 0.5, 0.1, 0.13, false));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Retrieve the strafe speed from the slider (can be adjusted to a slower value)
        double strafeSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Schnelligkeit").getValDouble();

        // Adjust movement only when in the air (if on ground, no air strafe effect)
        if (!mc.player.onGround) {
            applyAirStrafe(mc.player, strafeSpeed);
        }
    }

    private void applyAirStrafe(EntityPlayerSP player, double speed) {
        // If there's no movement input, return early to avoid unnecessary calculations
        if (player.moveForward == 0 && player.moveStrafing == 0) {
            return;
        }

        // Retrieve movement input
        float yaw = player.rotationYaw;
        MovementInput input = player.movementInput;

        // Get forward and strafe inputs (convert forward to -1 or 1 for cleaner control)
        double forward = input.moveForward;
        double strafe = input.moveStrafe;

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

        // Convert yaw to radians for trigonometric calculations
        double radians = Math.toRadians(yaw + 90);

        // Calculate movement velocities
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double motionX = forward * speed * cos + strafe * speed * sin;
        double motionZ = forward * speed * sin - strafe * speed * cos;

        // Check if the player is on the ground
        boolean onGround = player.onGround;

        // Apply air strafe with smoother control
        double airControlFactor = 0.3; // Adjust this to control the intensity of air movement
        player.motionX += motionX * airControlFactor;
        player.motionZ += motionZ * airControlFactor;

        // If the player is on the ground, apply some small ground speed boost
        if (onGround) {
            double groundSpeedBoost = 0.05; // Small speed boost on ground
            player.motionX += motionX * groundSpeedBoost;
            player.motionZ += motionZ * groundSpeedBoost;
        }

        // Optional: Apply a slight drag effect to make the movement feel less sharp
        double dragFactor = 0.95; // Smoothing factor for air control
        player.motionX *= dragFactor;
        player.motionZ *= dragFactor;
    }
}
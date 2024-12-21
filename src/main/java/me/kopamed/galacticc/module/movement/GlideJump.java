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
        super("SprungGleiter", "@Hauptinformation: " +
                "Macht dich schneller und laesst dich beim springen gleiten, fast wie ein Supersprung.", true, false, Category.BEWEGUNG);

        // Add slider for strafe speed (0.1 to 2.0)
        Galacticc.instance.settingsManager.rSetting(new Setting("Schnelligkeit", this, 0.5, 0.1, 2.0, false));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Retrieve the strafe speed from the slider
        double strafeSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Schnelligkeit").getValDouble();

        // Adjust movement on ground or in air
        applyStrafe(mc.player, strafeSpeed);
    }

    private void applyStrafe(EntityPlayerSP player, double speed) {
        if (player.moveForward == 0 && player.moveStrafing == 0) {
            return; // No input, no movement adjustment needed
        }

        // Check if the player is on the ground or in the air
        boolean onGround = player.onGround;

        // Retrieve movement input
        float yaw = player.rotationYaw;
        MovementInput input = player.movementInput;

        // Calculate movement direction
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

        if (onGround) {
            // Ground movement: Directly set motionX and motionZ
            player.motionX = motionX;
            player.motionZ = motionZ;
        } else {
            // Air movement: Add motionX and motionZ for smoother control
            player.motionX += motionX * 0.2; // Reduced effect in the air
            player.motionZ += motionZ * 0.2;
        }
    }
}

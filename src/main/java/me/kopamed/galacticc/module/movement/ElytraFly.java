package me.kopamed.galacticc.module.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ElytraFly extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean nearGround = false;
    private boolean forceFall = false;
    private long groundProximityStartTime = 0;

    public ElytraFly() {
        super("ElytraFly", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Kontrolliere die Elytra mit Bewegung in alle Richtungen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Horizontal_Speed: " + ChatFormatting.WHITE +
                        "Passt die horizontale Geschwindigkeit an." + ChatFormatting.RED +
                        "- Vertical_Speed:" + ChatFormatting.WHITE +
                        " Ermoeglicht schnelles Aufsteigen." + ChatFormatting.RED +
                        "- Descend_Speed:" + ChatFormatting.WHITE +
                        " Reguliert die Abstiegsgeschwindigkeit." + ChatFormatting.RED +
                        "- Automatische_Landung:" + ChatFormatting.WHITE +
                        " Verhindert Fallschaden bei Bodennaehe." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Bevor du Bodenkontakt machst,| lass das Modul landen sonst wirst du Fallschaden bekommen.",
                false, false, Category.BEWEGUNG);

        Galacticc.instance.settingsManager.rSetting(new Setting("Horizontal Speed", this, 1.0, 0.1, 5.0, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Vertical Speed", this, 0.5, 0.1, 3.0, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Descend Speed", this, 0.2, 0.1, 1.0, false));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || !mc.player.isElytraFlying() || event.phase != TickEvent.Phase.START) {
            return;
        }

        double horizontalSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Horizontal Speed").getValDouble();
        double verticalSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Vertical Speed").getValDouble();
        double descendSpeed = Galacticc.instance.settingsManager.getSettingByName(this, "Descend Speed").getValDouble();

        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        boolean up = mc.gameSettings.keyBindJump.isKeyDown();
        boolean down = mc.gameSettings.keyBindSneak.isKeyDown();

        double yaw = Math.toRadians(mc.player.rotationYaw + 90);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);

        mc.player.motionX = (forward * horizontalSpeed * cos) + (strafe * horizontalSpeed * sin);
        mc.player.motionZ = (forward * horizontalSpeed * sin) - (strafe * horizontalSpeed * cos);

        double groundHeight = mc.world.getHeight((int) mc.player.posX, (int) mc.player.posZ);
        double playerHeightAboveGround = mc.player.posY - groundHeight;

        if (playerHeightAboveGround <= 3.0) {
            if (!nearGround) {
                nearGround = true;
                groundProximityStartTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - groundProximityStartTime >= 3000) {
                forceFall = true;
            }
        } else {
            nearGround = false;
            groundProximityStartTime = 0;
            forceFall = false;
        }

        if (up) {
            mc.player.motionY = verticalSpeed;
        } else if (down) {
            mc.player.motionY = -descendSpeed;
        } else if (forceFall) {
            mc.player.motionY = -0.5;
        } else {
            mc.player.motionY = -0.05;
        }

        mc.player.fallDistance = 0.0f;

        if (playerHeightAboveGround <= 1.5) {
            mc.player.connection.sendPacket(new CPacketPlayer(true));
            mc.player.onGround = true;
        }
    }

    @SubscribeEvent
    public void onFallDamage(LivingFallEvent event) {
        if (event.getEntityLiving() == mc.player) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (mc.player.fallDistance > 2.0f) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
        }
    }
}

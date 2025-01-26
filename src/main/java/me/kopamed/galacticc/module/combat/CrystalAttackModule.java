package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@Mod.EventBusSubscriber
public class CrystalAttackModule extends Module {
    public CrystalAttackModule  () {
        super ("AutoCrystal", "attacks endcrystals in 1.12.2 minecraft", false, false, Category.ANGRIFF);
    }

    private long lastAttackTime = 0;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        // Find end crystals within a 6-block radius
        List<EntityEnderCrystal> crystalsInRange = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class,
                mc.player.getEntityBoundingBox().grow(6.0));

        if (crystalsInRange.isEmpty()) {
            return;
        }

        // Attack the closest end crystal
        EntityEnderCrystal closestCrystal = findClosestCrystal(crystalsInRange);
        if (closestCrystal != null && canAttack()) {
            performAttack(closestCrystal);
        }
    }

    /**
     * Finds the closest end crystal to the player.
     *
     * @param crystals List of end crystals in range.
     * @return The closest end crystal or null if no crystals are provided.
     */
    private EntityEnderCrystal findClosestCrystal(List<EntityEnderCrystal> crystals) {
        EntityEnderCrystal closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (EntityEnderCrystal crystal : crystals) {
            if (crystal != null && crystal.isEntityAlive()) {
                double distance = mc.player.getDistance(crystal);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = crystal;
                }
            }
        }
        return closest;
    }

    /**
     * Determines if the player can attack based on cooldown.
     *
     * @return True if the attack cooldown has elapsed, false otherwise.
     */
    private boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= 700
                && mc.player.getCooledAttackStrength(0.0f) >= 0.848f;
    }

    /**
     * Performs the attack on the target end crystal.
     *
     * @param target The end crystal to attack.
     */
    private void performAttack(EntityEnderCrystal target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        // Calculate rotation to face the target
        float[] rotation = calculateAngles(target.getPositionVector());
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation[0], rotation[1], mc.player.onGround));

        // Send attack packet to the target
        mc.player.connection.sendPacket(new CPacketUseEntity(target));
        mc.player.swingArm(EnumHand.MAIN_HAND);

        lastAttackTime = System.currentTimeMillis();
    }

    /**
     * Calculates the rotation angles required to face a specific position.
     *
     * @param to The target position to face.
     * @return An array containing yaw and pitch angles.
     */
    private float[] calculateAngles(Vec3d to) {
        Vec3d eyesPosition = mc.player.getPositionEyes(1.0F);
        double deltaX = to.x - eyesPosition.x;
        double deltaY = to.y - eyesPosition.y;
        double deltaZ = to.z - eyesPosition.z;

        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float distanceXZ = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, distanceXZ));

        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        return new float[]{yaw, pitch};
    }
}

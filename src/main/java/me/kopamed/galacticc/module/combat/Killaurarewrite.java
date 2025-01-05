package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class Killaurarewrite extends Module {

    private final Minecraft mc;
    private long lastHitTime;

    public Killaurarewrite() {
        super("Aura", "@Hauptinformation: " +
                "Greift Gegner automatisch an und beschutzt dich somit. " +
                "@Optionen: " +
                "- Attack Mode bietet drei Modi: " +
                "- Einzel greift nur ein Ziel an. " +
                "- Multi greift alle Ziele in  einer 4.5-Block Reichweite an. " +
                "- Closest priorisiert das naeheste Ziel. " +
                "- Kategorien zum Angriff: " +
                "- Attack Monsters greift feindliche Kreaturen an. " +
                "- Attack Animals greift Tiere an. " +
                "- Attack Neutral greift neutrale Kreaturen an. " +
                "- Attack Players greift andere Spieler an. " +
                "- Die Reichweite des Moduls betraegt 4.5 Bloecke. " +
                "- Unterstuetzt Sweeping Edge und fuegt mehreren Zielen in einem Schwung Schaden zu.",
                true, false, Category.ANGRIFF);

        this.mc = Minecraft.getMinecraft();
        this.lastHitTime = 0;

        ArrayList<String> attackModes = new ArrayList<>();
        attackModes.add("Einzel");
        attackModes.add("Multi");
        attackModes.add("Closest");
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Mode", this, "Closest", attackModes));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Monsters", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Animals", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Neutral", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Players", this, false));

    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        lastHitTime = 0;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        lastHitTime = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Mode").getValString();

        boolean attackMonsters = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Monsters").getValBoolean();
        boolean attackAnimals = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Animals").getValBoolean();
        boolean attackNeutral = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Neutral").getValBoolean();
        boolean attackPlayers = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Players").getValBoolean();

        // Get all entities in range (6 blocks now)
        List<Entity> entitiesInRange = mc.world.getEntitiesWithinAABB(Entity.class, mc.player.getEntityBoundingBox().grow(6.0));

        List<Entity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getEntitiesWithinAABB(EntityLivingBase.class, mc.player.getEntityBoundingBox().grow(6.0))) {
            if (entity == mc.player || !entity.isEntityAlive()) {
                continue;
            }

            if (attackMonsters && entity instanceof EntityMob) {
                targets.add(entity);
            } else if (attackAnimals && entity instanceof EntityAnimal) {
                targets.add(entity);
            } else if (attackNeutral && isNeutralEntity(entity)) {
                targets.add(entity);
            } else if (attackPlayers && entity instanceof EntityPlayer && entity != mc.player) {
                targets.add(entity);
            }
        }

        if (targets.isEmpty()) {
            return;
        }

        switch (mode) {
            case "Einzel":
                attackSingleEntity(targets);
                break;
            case "Multi":
                attackMultipleEntities(targets);
                break;
            case "Closest":
                attackClosestEntity(targets);
                break;
        }
    }

    private boolean isNeutralEntity(Entity entity) {
        if (entity instanceof EntityPigZombie) {
            return !((EntityPigZombie) entity).isAngry();
        }
        if (entity instanceof EntityIronGolem) {
            return !((EntityIronGolem) entity).isPlayerCreated();
        }
        return entity instanceof EntityCreature && !(entity instanceof EntityMob || entity instanceof EntityAnimal);
    }

    private void attackSingleEntity(List<Entity> entitiesInRange) {
        for (Entity entity : entitiesInRange) {
            if (entity != null && canHit()) {
                if (mc.player.getCooledAttackStrength(0.0f) >= 0.848) {
                    performSweepAttack(entity);
                }
                lastHitTime = System.currentTimeMillis();
                break;
            }
        }
    }

    private void attackMultipleEntities(List<Entity> entitiesInRange) {
        for (Entity entity : entitiesInRange) {
            if (entity != null && canHit()) {
                if (mc.player.getCooledAttackStrength(0.0f) >= 0.848) {
                    performSweepAttack(entity);
                }
                lastHitTime = System.currentTimeMillis();
            }
        }
    }

    private void attackClosestEntity(List<Entity> entitiesInRange) {
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entitiesInRange) {
            if (entity != null) {
                double distance = mc.player.getDistance(entity);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null && canHit()) {
            if (mc.player.getCooledAttackStrength(0.0f) >= 0.848) {
                performSweepAttack(closestEntity);
            }
            lastHitTime = System.currentTimeMillis();
        }
    }

    private void performSweepAttack(Entity target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        float[] rotation = calculateAngles(target.getPositionVector());

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation[0], rotation[1], mc.player.onGround));

        mc.player.connection.sendPacket(new CPacketUseEntity(target));
        mc.player.swingArm(EnumHand.MAIN_HAND);

        if (mc.player.onGround && !mc.player.isSprinting() && mc.player.getCooledAttackStrength
                (0.5f) >= 0.848f) {
            List<Entity> nearbyEntities = mc.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    target.getEntityBoundingBox().grow(
                            1.0,
                            0.25,
                            1.0));
            for (Entity entity : nearbyEntities) {
                if (entity == mc.player || entity == target || !entity.isEntityAlive()) {
                    continue; // Skip the player, the main target, and dead entities
                }

                if (!Galacticc.instance.settingsManager.getSettingByName
                        (this, "Attack Players").getValBoolean() && entity instanceof EntityPlayer) {
                    continue;
                }

                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            }
        }
    }

    @Override
    public String getHUDInfo() {
        String attackMode = Galacticc.instance.settingsManager.getSettingByName
                (this, "Attack Mode").getValString();
        int targetCount = (int) mc.world.getEntitiesWithinAABB(EntityLivingBase.class, mc.player.getEntityBoundingBox().grow(
                6.0))
                .stream()
                .filter(entity -> entity != mc.player && entity.isEntityAlive())
                .count();
        return ChatFormatting.GRAY + "[" + ChatFormatting.GRAY + attackMode + ChatFormatting.GRAY + ", "
                + ChatFormatting.GRAY + "Targets: " + targetCount + ChatFormatting.GRAY + "]";
    }

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

        private boolean canHit() {
            return System.currentTimeMillis() - lastHitTime >= 700;
        }
    }


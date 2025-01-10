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
import java.util.Collections;
import java.util.List;

public class Killaurarewrite extends Module {

    private final Minecraft mc;
    private long lastHitTime;

    public Killaurarewrite() {
        super("Aura", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Greift Gegner automatisch an und bietet| Schutz durch automatische Angriffe.|" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Attack Mode: " + ChatFormatting.WHITE +
                        "Waehle aus drei Modi, um Ziele| strategisch anzugreifen:|" + ChatFormatting.RED +
                        "- Einzel: " + ChatFormatting.WHITE +
                        "Greift ein einzelnes Ziel an.|" + ChatFormatting.RED +
                        "- Multi: " + ChatFormatting.WHITE +
                        "Greift alle Ziele innerhalb von| 6 Bloecken an.|" + ChatFormatting.RED +
                        "- Closest: " + ChatFormatting.WHITE +
                        "Priorisiert das naeheste Ziel.|" + ChatFormatting.RED +
                        "- Kategorien zum Angriff: " + ChatFormatting.WHITE +
                        "Passe an, welche Entitaeten angegriffen| werden sollen:|" + ChatFormatting.RED +
                        "- Attack Monsters: " + ChatFormatting.WHITE +
                        "Greift feindliche Kreaturen an.|" + ChatFormatting.RED +
                        "- Attack Animals: " + ChatFormatting.WHITE +
                        "Greift Tiere an.|" + ChatFormatting.RED +
                        "- Attack Neutral: " + ChatFormatting.WHITE +
                        "Greift neutrale Kreaturen an.|" + ChatFormatting.RED +
                        "- Attack Players: " + ChatFormatting.WHITE +
                        "Greift andere Spieler an.|" + ChatFormatting.RED +
                        "- Reichweite: " + ChatFormatting.WHITE +
                        "Laesst dich die Angriffsreichweite bestimmen." + ChatFormatting.RED +
                        "- Sweeping Edge Unterstuetzung: " + ChatFormatting.WHITE +
                        "Fuegt mehreren Zielen in einem| Schwung Schaden zu.|" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Passe die Angriffskategorien und den| Modus im Modulmenue an, um optimalen| Schutz und Angriffseffizienz zu erreichen.",
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

        // Copy entities into a local list to avoid concurrent modification issues
        List<EntityLivingBase> entitiesInRange = new ArrayList<>(mc.world.getEntitiesWithinAABB(EntityLivingBase.class, mc.player.getEntityBoundingBox().grow(6.0)));

        List<Entity> targets = new ArrayList<>();
        for (Entity entity : entitiesInRange) {
            if (entity == null || entity == mc.player || !entity.isEntityAlive()) {
                continue;
            }

            if (attackMonsters && (entity instanceof EntityMob || isCustomHostileEntity(entity))) {
                targets.add(entity);
            } else if (attackAnimals && entity instanceof EntityAnimal) {
                targets.add(entity);
            } else if (attackNeutral && (isNeutralEntity(entity) || isCustomNeutralEntity(entity))) {
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

    private boolean isCustomHostileEntity(Entity entity) {
        List<String> customHostileClassNames = new ArrayList<>();
        Collections.addAll(customHostileClassNames,
                "EntityCQRBoarman",
                "EntityCQRMandril",
                "EntityCQRIllager",
                "EntityCQRGremlin",
                "EntityCQRGoblin",
                "EntityCQREnderman",
                "EntityCQRMinotaur",
                "EntityCQRMummy",
                "EntityCQRPirate",
                "EntityCQRSkeleton",
                "EntityCQRSpectre",
                "EntityCQRWalker",
                "EntityCQRZombie"
        );

        String className = entity.getClass().getSimpleName();
        return customHostileClassNames.contains(className);
    }

    private boolean isCustomNeutralEntity(Entity entity) {
        List<String> customNeutralClassNames = new ArrayList<>();
        Collections.addAll(customNeutralClassNames,
                "EntityCQRDummy",
                "EntityCQRDwarf",
                "EntityCQRHuman",
                "EntityCQRGolem",
                "EntityCQRTriton",
                "EntityCQRNPC",
                "EntityCQROgre",
                "EntityCQROrc"
        );

        String className = entity.getClass().getSimpleName();
        return customNeutralClassNames.contains(className);
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


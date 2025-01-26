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
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketHeldItemChange;
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
    private String attackMode;
    private boolean attackMonsters;
    private boolean attackAnimals;
    private boolean attackNeutral;
    private boolean attackPlayers;
    private boolean silentSwitchEnabled;
    private boolean obsidianSwitchEnabled;
    private long lastObsidianSwitchTime = 0;
    private static final long WEAKNESS_DURATION_MS = 11000;

    public Killaurarewrite() {
        super("Aura", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Greift Gegner automatisch an und bietet| Schutz durch automatische Angriffe.|" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Attack Mode: " + ChatFormatting.WHITE +
                        "Waehle aus drei Modi, um Ziele| strategisch anzugreifen:|" + ChatFormatting.RED +
                        "- Einzel: " + ChatFormatting.WHITE +
                        "Greift ein einzelnes Ziel an.|" + ChatFormatting.RED +
                        "- Silent-Switch: " +ChatFormatting.WHITE +
                        "Wechselt zum Schwert, erlaubt dir waehrenddessen zu essen.| Du musst kein Schwert halten|, mit dieser Funktion." + ChatFormatting.RED +
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
                        "Fuegt mehreren Zielen in einem| Schwung Schaden zu.|" + ChatFormatting.RED +
                        "- ObsidianSword-Switch: " + ChatFormatting.WHITE +
                        "Schaltet zum Obsidian-Schwert, um das Ziel| mit Schwaeche II fuer 11 Sekunden zu belegen.| Wechselt danach zu einem anderen Schwert, um| effizient Schaden zuzufugen, bevor nach 11| Sekunden erneut zum Obsidian-Schwert gewechselt wird.|" +
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
        Galacticc.instance.settingsManager.rSetting(new Setting("ObsidianSword-Switch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Silent-Switch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Monsters", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Animals", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Neutral", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Attack Players", this, false));
        cacheSettings();

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

    /**
     * Caches the settings to minimize repeated lookup.
     */
    private void cacheSettings() {
        this.attackMode = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Mode").getValString();
        this.attackMonsters = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Monsters").getValBoolean();
        this.attackAnimals = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Animals").getValBoolean();
        this.attackNeutral = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Neutral").getValBoolean();
        this.attackPlayers = Galacticc.instance.settingsManager.getSettingByName(this, "Attack Players").getValBoolean();
        this.silentSwitchEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "Silent-Switch").getValBoolean();
        this.obsidianSwitchEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "ObsidianSword-Switch").getValBoolean(); // New cache
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        // Update settings cache only when necessary
        if (settingsHaveChanged()) {
            cacheSettings();
        }

        // Find entities within range
        List<EntityLivingBase> entitiesInRange = mc.world.getEntitiesWithinAABB(EntityLivingBase.class,
                mc.player.getEntityBoundingBox().grow(6.0));

        // Filter entities based on attack categories
        List<Entity> targets = filterTargets(entitiesInRange);

        if (targets.isEmpty()) {
            return;
        }

        // Attack based on the selected mode
        switch (attackMode) {
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

    /**
     * Checks if any settings have changed.
     *
     * @return True if any setting has changed, false otherwise.
     */
    private boolean settingsHaveChanged() {
        return !attackMode.equals(Galacticc.instance.settingsManager.getSettingByName(this, "Attack Mode").getValString())
                || attackMonsters != Galacticc.instance.settingsManager.getSettingByName(this, "Attack Monsters").getValBoolean()
                || attackAnimals != Galacticc.instance.settingsManager.getSettingByName(this, "Attack Animals").getValBoolean()
                || attackNeutral != Galacticc.instance.settingsManager.getSettingByName(this, "Attack Neutral").getValBoolean()
                || attackPlayers != Galacticc.instance.settingsManager.getSettingByName(this, "Attack Players").getValBoolean()
                || silentSwitchEnabled != Galacticc.instance.settingsManager.getSettingByName(this, "Silent-Switch").getValBoolean()
                || obsidianSwitchEnabled != Galacticc.instance.settingsManager.getSettingByName(this, "ObsidianSword-Switch").getValBoolean(); // Check for change
    }

    /**
     * Filters entities based on the cached attack settings.
     *
     * @param entities The list of entities to filter.
     * @return A list of entities to attack.
     */
    private List<Entity> filterTargets(List<EntityLivingBase> entities) {
        List<Entity> targets = new ArrayList<>();
        for (Entity entity : entities) {
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
        return targets;
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
            if (entity != null && System.currentTimeMillis() - lastHitTime >= 700) {
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
            if (entity != null && System.currentTimeMillis() - lastHitTime >= 700) {
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

        if (closestEntity != null && System.currentTimeMillis() - lastHitTime >= 700) {
            if (mc.player.getCooledAttackStrength(0.0f) >= 0.848) {
                performSweepAttack(closestEntity);
            }
            lastHitTime = System.currentTimeMillis();
        }
    }

    /**
     * Performs a sweep attack on the given target entity, attacking nearby entities as well.
     * <p>
     * This method performs the following actions:
     * <ul>
     *     <li>Checks if the target entity is valid and alive.</li>
     *     <li>Calculates the required rotation for the player to face the target entity.</li>
     *     <li>Sends a rotation packet to adjust the player's facing direction.</li>
     *     <li>Executes an attack on the target entity using a {@link CPacketUseEntity} packet.</li>
     *     <li>If the player is grounded, not sprinting, and the attack cool-down is satisfied, checks for nearby entities and attacks them as well,
     *         based on configurable settings.</li>
     *     <li>Optionally performs a silent switch to the sword slot if enabled, and reverts back to the original slot after the attack.</li>
     * </ul>
     * </p>
     *
     * @param target The target entity to attack. It should not be null and must be alive.
     */
    private void performSweepAttack(Entity target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        boolean obsidianSwitchEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "ObsidianSword-Switch").getValBoolean();
        boolean silentSwitchEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "Silent-Switch").getValBoolean();

        int obsidianSwordSlot = findObsidianSwordInHotbar();
        int otherSwordSlot = findSwordInHotbar();
        int originalSlot = mc.player.inventory.currentItem;

        long currentTime = System.currentTimeMillis();

        Runnable attackAction = () -> {
            float[] rotation = calculateAngles(target.getPositionVector());
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation[0], rotation[1], mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketUseEntity(target));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            if (mc.player.onGround && !mc.player.isSprinting() && mc.player.getCooledAttackStrength(0.5f) >= 0.848f) {
                List<Entity> nearbyEntities = mc.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        target.getEntityBoundingBox().grow(1.0, 0.25, 1.0));

                for (Entity entity : nearbyEntities) {
                    if (entity == mc.player || entity == target || !entity.isEntityAlive()) {
                        continue; // Skip the player, the target, and dead entities
                    }

                    if (!Galacticc.instance.settingsManager.getSettingByName(this, "Attack Players").getValBoolean()
                            && entity instanceof EntityPlayer) {
                        continue;
                    }

                    // Send attack packet to the nearby entity
                    mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                }
            }
        };

        if (obsidianSwitchEnabled && obsidianSwordSlot != -1 && (currentTime - lastObsidianSwitchTime) >= WEAKNESS_DURATION_MS) {
            if (silentSwitchEnabled) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(obsidianSwordSlot));
            } else {
                mc.player.inventory.currentItem = obsidianSwordSlot;
            }

            attackAction.run();
            lastObsidianSwitchTime = currentTime;

            if (silentSwitchEnabled) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
            } else {
                mc.player.inventory.currentItem = originalSlot;
            }
        } else if (silentSwitchEnabled && otherSwordSlot != -1) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(otherSwordSlot));
            attackAction.run();
            mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
        } else {
            attackAction.run();
        }
    }


    /**
     * Finds the first sword in the player's hot-bar.
     *
     * @return The hot-bar slot index of the sword, or -1 if no sword is found.
     */
    private int findSwordInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof ItemSword) {
                return i;
            }
        }
        return -1;
    }

    private int findObsidianSwordInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof ItemSword && "Obsidian Sword".equals(itemStack.getDisplayName())) {
                return i;
            }
        }
        return -1;
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
}


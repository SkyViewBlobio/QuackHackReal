package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttackParticle extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<EntityZombie, Float> zombieHealthMap = new HashMap<>();

    public AttackParticle() {
        super("Angriff Partikel", "Replaces critical hit particles with custom particles on zombies", false, false, Category.VISUELLES);

        //************************Particle Mode Settings**************************
        ArrayList<String> modes = new ArrayList<>();
        modes.add("C-Schlag");
        modes.add("N-Schlag");
        Galacticc.instance.settingsManager.rSetting(new Setting("Partikel Modus", this, "C-Schlag", modes));

        //************************Particle Type Settings**************************
        Galacticc.instance.settingsManager.rSetting(new Setting("Herz", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Flamme", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rauch-1", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rauch-2", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Feuerwerk", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Happy", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Sauer", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Fluch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("WasserBlase", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Explosion-3", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Explosion-2", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Explosion-1", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("SchneeFlocke", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Verzauberung", this, false));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Handle particles differently for singleplayer and multiplayer
        if (mc.isIntegratedServerRunning()) {
            // Singleplayer logic uses LivingHurtEvent for precise hit detection
        } else {
            handleMultiplayerParticles();
        }
    }

    //************************Handle Multiplayer Particles**************************
    private void handleMultiplayerParticles() {
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Partikel Modus").getValString();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityZombie)) {
                continue;
            }

            EntityZombie zombie = (EntityZombie) entity;
            float currentHealth = zombie.getHealth();

            if (zombieHealthMap.containsKey(zombie)) {
                float previousHealth = zombieHealthMap.get(zombie);

                // Detect a hit by checking health reduction
                if (currentHealth < previousHealth) {
                    if ("C-Schlag".equals(mode) && isCriticalHit()) {
                        spawnParticlesAroundEntity(zombie);
                    } else if ("N-Schlag".equals(mode)) {
                        spawnParticlesAroundEntity(zombie);
                    }
                }
            }

            zombieHealthMap.put(zombie, currentHealth); // Update health tracking
        }

        // Remove dead or unloaded zombies from the map
        zombieHealthMap.keySet().removeIf(zombie -> zombie.isDead || !mc.theWorld.loadedEntityList.contains(zombie));
    }

    //************************Handle Singleplayer Hits**************************
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!mc.isIntegratedServerRunning() || mc.theWorld == null || mc.thePlayer == null) {
            return; // Only handle singleplayer hits
        }

        if (!(event.entity instanceof EntityZombie)) {
            return; // Only handle zombies
        }

        EntityZombie zombie = (EntityZombie) event.entity;
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Partikel Modus").getValString();

        if ("C-Schlag".equals(mode) && isCriticalHit()) {
            spawnParticlesAroundEntity(zombie);
        } else if ("N-Schlag".equals(mode)) {
            spawnParticlesAroundEntity(zombie);
        }
    }

    private boolean isCriticalHit() {
        return mc.thePlayer.fallDistance > 0.0F && !mc.thePlayer.onGround &&
                !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() &&
                !mc.thePlayer.isPotionActive(8) && mc.thePlayer.ridingEntity == null;
    }

    private void spawnParticlesAroundEntity(Entity entity) {
        double x = entity.posX;
        double y = entity.posY + entity.height / 2.0;
        double z = entity.posZ;

        double offsetX = (mc.thePlayer.getRNG().nextDouble() - 0.5) * 2.0;
        double offsetY = mc.thePlayer.getRNG().nextDouble();
        double offsetZ = (mc.thePlayer.getRNG().nextDouble() - 0.5) * 2.0;

        if (isParticleEnabled("Herz")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.HEART, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Flamme")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.FLAME, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Rauch-1")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Rauch-2")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Feuerwerk")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Happy")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Sauer")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Fluch")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.SPELL_WITCH, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("WasserBlase")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Explosion-3")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Explosion-2")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Explosion-1")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("SchneeFlocke")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
        if (isParticleEnabled("Verzauberung")) {
            mc.theWorld.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.1, 0.0);
        }
    }

    private boolean isParticleEnabled(String settingName) {
        Setting setting = Galacticc.instance.settingsManager.getSettingByName(this, settingName);
        return setting != null && setting.getValBoolean();
    }

    /**
     * Data class to store particle spawn information.
     */
    private static class ParticleSpawnData {
        public final Entity entity;
        public final long spawnTime;

        public ParticleSpawnData(Entity entity, long spawnTime) {
            this.entity = entity;
            this.spawnTime = spawnTime;
        }
    }
}
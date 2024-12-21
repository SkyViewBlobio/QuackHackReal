package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SelfParticle extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private long lastParticleTime;

    public SelfParticle() {
        super("Selbst Partikel", "@Hauptinformationen: " +
                "Zeigt Partikel um deinen Charakter an und laesst dich diese aendern. " +
                "@Optionen: " +
                "- Auszeit gibt vor, wie lange es dauert bis der gewuenschte Partikeleffekt erscheint. Warnung: kure Partikelauszeit = mehr Pc Arbeit. || " +
                "- Partikelanzahl gibt vor, wie viele Partikel angezeigt werden. Warnung: mehr Partikel = mehr Pc Arbeit.", false, false, Category.VISUELLES);

        // Delay slider for particle generation
        Galacticc.instance.settingsManager.rSetting(new Setting("Auszeit", this, 3, 1, 10, false)); // Delay in seconds

        // Particle count slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Partikel anzahl", this, 10, 1, 30, false));

        // Particle toggles for all available particles in Minecraft 1.8.9
        Galacticc.instance.settingsManager.rSetting(new Setting("Herz", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Verzauberung", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Note", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Flamme", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Schneeflocke", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Crit", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rauch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Portal", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Lava", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Explosion", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Feuerwerk", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Wasserblase", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Fluch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Happy", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Sauer", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hexe", this, false));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) {
            return; // Exit if the module is disabled or the player/world is null
        }

        //************************Particle Delay Logic**************************//
        double delayInSeconds = Galacticc.instance.settingsManager.getSettingByName(this, "Auszeit").getValDouble();
        long delayInMillis = (long) (delayInSeconds * 1000);

        if (System.currentTimeMillis() - lastParticleTime < delayInMillis) {
            return; // Exit if the delay hasn't passed yet
        }

        //************************Spawn Particles**************************//
        spawnParticlesAroundPlayer();
        lastParticleTime = System.currentTimeMillis(); // Update the last particle spawn time
    }

    private void spawnParticlesAroundPlayer() {
        int count = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Partikel anzahl").getValDouble();

        for (int i = 0; i < count; i++) {
            double x = mc.player.posX + (mc.player.getRNG().nextDouble() - 0.5D) * 2.0D;
            double y = mc.player.posY + mc.player.getRNG().nextDouble();
            double z = mc.player.posZ + (mc.player.getRNG().nextDouble() - 0.5D) * 2.0D;

            double motionX = mc.player.getRNG().nextGaussian() * 0.02D;
            double motionY = mc.player.getRNG().nextGaussian() * 0.02D;
            double motionZ = mc.player.getRNG().nextGaussian() * 0.02D;

            // Spawn specific particles based on toggles
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Herz").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.HEART, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Verzauberung").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Note").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.NOTE, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Flamme").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Schneeflocke").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Crit").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.CRIT, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Rauch").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Portal").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Lava").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Explosion").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Feuerwerk").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Wasserblase").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Fluch").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.SPELL, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Happy").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Sauer").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, x, y, z, motionX, motionY, motionZ);
            }
            if (Galacticc.instance.settingsManager.getSettingByName(this, "Hexe").getValBoolean()) {
                mc.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, x, y, z, motionX, motionY, motionZ);
            }
        }
    }
}
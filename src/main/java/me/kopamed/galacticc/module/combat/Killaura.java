package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import me.kopamed.galacticc.utils.debian;
import me.kopamed.galacticc.utils.mint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class Killaura extends Module {
    private long cps, reach;
    private boolean autoBlock, attackAnimals, attackMonsters, attackPassives, attackPlayers;

    //*******************Information Calculation********************
    private EntityLivingBase lastHitEntity = null;
    private long lastHitTime = 0;
    private double damageDealt = 0;
    private int swingCount = 0;
    private double lastDamageDealt = 0;
    private long lastDamageTime = 0;
    //*************************************************************

    public Killaura() {
        super("Beschutzer", "Blatantly attacks enemies", true, false, Category.ANGRIFF);

        // Settings initialization
        Setting cps = new Setting("KlicksProSek", this, 10, 0.1, 30, false);
        Setting reach = new Setting("Reichweite", this, 6, 1, 6, false);
        Setting autoBlock = new Setting("AutoBlockierung", this, false);
        Setting animals = new Setting("Angriffe Tiere", this, true);
        Setting monsters = new Setting("Angriffe Monster", this, true);
        Setting passives = new Setting("Angriffe Passiv", this, false);
        Setting players = new Setting("Angriffe Spieler", this, true);

        Galacticc.instance.settingsManager.rSetting(cps);
        Galacticc.instance.settingsManager.rSetting(reach);
        Galacticc.instance.settingsManager.rSetting(autoBlock);
        Galacticc.instance.settingsManager.rSetting(animals);
        Galacticc.instance.settingsManager.rSetting(monsters);
        Galacticc.instance.settingsManager.rSetting(passives);
        Galacticc.instance.settingsManager.rSetting(players);
    }

    @SubscribeEvent
    public void onMotion(TickEvent.PlayerTickEvent e) {
        updateVals();
        if (e.phase != TickEvent.Phase.START || mc.thePlayer.isSpectator()) {
            return;
        }

        // Gather and filter targets
        List<EntityLivingBase> targets = debian.getTargets(reach);
        List<EntityLivingBase> filteredTargets = new ArrayList<>();

        for (EntityLivingBase entity : targets) {
            if (shouldAttack(entity)) {
                filteredTargets.add(entity);
            }
        }

        filteredTargets = debian.sortByRange(filteredTargets);

        if (filteredTargets.isEmpty()) {
            return;
        }

        EntityLivingBase target = filteredTargets.get(0);

        // Update player rotation
        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.getEntityBoundingBox().minY,
                mc.thePlayer.posZ,
                debian.getRotations(target)[0],
                debian.getRotations(target)[1],
                mc.thePlayer.onGround
        ));

        // Perform attack logic
        if (mint.hasTimeElapsed(1000 / cps, true) && !mc.thePlayer.isBlocking()) {
            mc.thePlayer.swingItem();
            if (autoBlock && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                mc.thePlayer.getHeldItem().useItemRightClick(mc.theWorld, mc.thePlayer);
            }
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

            // Check for different mob types and reset if necessary
            if (lastHitEntity == null || !lastHitEntity.getClass().equals(target.getClass())) {
                resetHitInfo();
            }

            // Update hit details
            lastHitEntity = target;
            lastHitTime = System.currentTimeMillis();
            lastDamageDealt = target.getMaxHealth() - target.getHealth(); // Approximation of damage
            lastDamageTime = System.currentTimeMillis(); // Track when the damage was calculated
            swingCount++;
        }
    }

    private void resetHitInfo() {
        damageDealt = 0;
        swingCount = 0;
        lastHitEntity = null;
        lastHitTime = 0;
        // Note: lastDamageDealt is not reset here as it handles its own timing.
    }

    private boolean shouldAttack(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && attackPlayers) {
            return true;
        }
        if (attackAnimals && (entity instanceof EntityCow ||
                entity instanceof EntitySheep ||
                entity instanceof EntityWolf ||
                entity instanceof EntityChicken ||
                entity instanceof EntityVillager ||
                entity instanceof EntityPig ||
                entity instanceof EntitySquid ||
                entity instanceof EntityRabbit ||
                entity instanceof EntityOcelot ||
                entity instanceof EntityIronGolem ||
                entity instanceof EntityGolem ||
                entity instanceof EntityHorse)) {
            return true;
        }
        if (attackMonsters && (entity instanceof EntityZombie ||
                entity instanceof EntityCreeper ||
                entity instanceof EntitySkeleton ||
                entity instanceof EntityWitch ||
                entity instanceof EntityWither ||
                entity instanceof EntityDragon ||
                entity instanceof EntityGhast ||
                entity instanceof EntitySlime ||
                entity instanceof EntityMagmaCube ||
                entity instanceof EntityBlaze ||
                entity instanceof EntitySpider ||
                entity instanceof EntityCaveSpider ||
                entity instanceof EntityEndermite ||
                entity instanceof EntityGuardian ||
                entity instanceof EntitySilverfish)) {
            return true;
        }
        if (attackPassives && (entity instanceof EntityEnderman ||
                entity instanceof EntityPigZombie)) {
            return true;
        }
        return false;
    }

    public void updateVals() {
        this.cps = (long) Galacticc.instance.settingsManager.getSettingByName
                (this, "KlicksProSek").getValDouble();
        this.reach = (long) Galacticc.instance.settingsManager.getSettingByName
                (this, "Reichweite").getValDouble();
        this.autoBlock = Galacticc.instance.settingsManager.getSettingByName
                (this, "AutoBlockierung").getValBoolean();
        this.attackAnimals = Galacticc.instance.settingsManager.getSettingByName
                (this, "Angriffe Tiere").getValBoolean();
        this.attackMonsters = Galacticc.instance.settingsManager.getSettingByName
                (this, "Angriffe Monster").getValBoolean();
        this.attackPassives = Galacticc.instance.settingsManager.getSettingByName
                (this, "Angriffe Passiv").getValBoolean();
        this.attackPlayers = Galacticc.instance.settingsManager.getSettingByName
                (this, "Angriffe Spieler").getValBoolean();
    }

    //*******************HUD INFO********************
    @Override
    public String getHUDInfo() {
        // Reset swing and entity info if inactive for more than 3 seconds
        if (System.currentTimeMillis() - lastHitTime > 3000) {
            resetHitInfo();
        }

        // Reset damage per hit info if inactive for more than 1 second
        if (System.currentTimeMillis() - lastDamageTime > 1000) {
            lastDamageDealt = 0;
        }

        String entityName = lastHitEntity != null ? lastHitEntity.getName() : "N/A";
        String damageFormatted = String.format("%.1f", lastDamageDealt); // Show damage for the last hit

        return ChatFormatting.GRAY + "[L-T " +
                entityName + ChatFormatting.GRAY +
                ",  " + damageFormatted + ChatFormatting.GRAY +
                ",  " + swingCount + ChatFormatting.GRAY + "]";
    }
    //*************************************************************
}

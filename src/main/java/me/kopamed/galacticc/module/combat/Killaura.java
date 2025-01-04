package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import me.kopamed.galacticc.utils.debian;
import me.kopamed.galacticc.utils.mint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

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
    // Map to store entities with fading hitboxes
    private final Map<EntityLivingBase, FadingHitbox> fadingHitboxes = new HashMap<>();
    //*************************************************************
    //todo reset gethudinfo every 3s and add neutral mobs. also ADD HIT DELAY FOR 1.12.2

    public Killaura() {
        super("Beschutzer", "@Hauptinformation: " +
                "Schlaegt ausgewaehlte Tiere/Monster/Spieler in einen festgelegten Umkreis." +
                "@Optionen: " +
                "- KlicksProSek gibt an wie viele Klicks pro Sekunde der Beschutzer schlaegt. || " +
                "- Reichweite gibt die Reichweite in Bloecken an. || " +
                "- Autoblockierung blockt eintreffenden Schaden Automatisch. || " +
                "- Hitbox anzeigen zeigt markiert was du gerade geschlagen hast. || " +
                "- Fade-out zeigt einen schoenen Effekt wenn du aufhoerst mit schlagen." , true, false, Category.ANGRIFF);

        // Settings initialization
        Setting cps = new Setting("KlicksProSek", this, 10, 0.1, 30, false);
        Setting reach = new Setting("Reichweite", this, 6, 1, 6, false);
        Setting autoBlock = new Setting("AutoBlockierung", this, false);
        Setting animals = new Setting("Angriffe Tiere", this, true);
        Setting monsters = new Setting("Angriffe Monster", this, true);
        Setting passives = new Setting("Angriffe Passiv", this, false);
        Setting players = new Setting("Angriffe Spieler", this, true);
        // Hitbox settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Hitbox anzeigen", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Fade-Out Time", this, 20000, 500, 5000, true)); // Fade-out time in milliseconds
        Galacticc.instance.settingsManager.rSetting(new Setting("Hitbox Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hitbox Grün", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hitbox Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Hitbox Alpha", this, 100, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Outline Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Outline Grün", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Outline Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Outline Alpha", this, 200, 0, 255, true));

        Galacticc.instance.settingsManager.rSetting(cps);
        Galacticc.instance.settingsManager.rSetting(reach);
        Galacticc.instance.settingsManager.rSetting(autoBlock);
        Galacticc.instance.settingsManager.rSetting(animals);
        Galacticc.instance.settingsManager.rSetting(monsters);
        Galacticc.instance.settingsManager.rSetting(passives);
        Galacticc.instance.settingsManager.rSetting(players);
    }

    @SubscribeEvent
    public void onMotion(TickEvent.PlayerTickEvent event) {
        updateVals();
        // Check if mc.player is not null before proceeding
        if (mc.player == null || event.phase != TickEvent.Phase.START || mc.player.isSpectator()) {
            return;
        }

        // Filter and sort targets
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

// Update rotation and perform attack
        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(
                mc.player.posX,
                mc.player.getEntityBoundingBox().minY,
                mc.player.posZ,
                debian.getRotations(target)[0],
                debian.getRotations(target)[1],
                mc.player.onGround
        ));

        if (mint.hasTimeElapsed(1000 / cps, true)) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketUseEntity(target, EnumHand.MAIN_HAND));
            lastHitEntity = target;
            lastHitTime = System.currentTimeMillis();
            lastDamageDealt = target.getMaxHealth() - target.getHealth();
            lastDamageTime = System.currentTimeMillis();
            swingCount++;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        boolean showHitbox = Galacticc.instance.settingsManager.getSettingByName(this, "Hitbox anzeigen").getValBoolean();
        if (!showHitbox) return;

        Color baseFillColor = getColor("Hitbox Rot", "Hitbox Grün", "Hitbox Blau", "Hitbox Alpha");
        Color baseOutlineColor = getColor("Outline Rot", "Outline Grün", "Outline Blau", "Outline Alpha");

        double fadeDuration = Galacticc.instance.settingsManager.getSettingByName(this, "Fade-Out Time").getValDouble();
        long currentTime = System.currentTimeMillis();

        // Render fading hitboxes
        Iterator<Map.Entry<EntityLivingBase, FadingHitbox>> iterator = fadingHitboxes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityLivingBase, FadingHitbox> entry = iterator.next();
            FadingHitbox fadingHitbox = entry.getValue();
            long elapsedTime = currentTime - fadingHitbox.getFadeStartTime();

            // Calculate fade-out progress
            double fadeProgress = Math.min(1.0, elapsedTime / fadeDuration);
            if (fadeProgress >= 1.0) {
                iterator.remove(); // Remove fully faded entities
                continue;
            }

            // Reduce alpha over time
            int fadingAlpha = (int) (baseFillColor.getAlpha() * (1.0 - fadeProgress));
            Color fadingFillColor = new Color(baseFillColor.getRed(), baseFillColor.getGreen(), baseFillColor.getBlue(), fadingAlpha);
            Color fadingOutlineColor = new Color(baseOutlineColor.getRed(), baseOutlineColor.getGreen(), baseOutlineColor.getBlue(), fadingAlpha);

            renderEntityHitbox(fadingHitbox.getEntity(), fadingFillColor, fadingOutlineColor);
        }

        // Render the current target's hitbox
        if (lastHitEntity != null) {
            renderEntityHitbox(lastHitEntity, baseFillColor, baseOutlineColor);
        }
    }

    private void updateFadingHitboxes(EntityLivingBase currentTarget) {
        long currentTime = System.currentTimeMillis();

        // Add the last hit entity to fading hitboxes if switching targets
        if (lastHitEntity != null && lastHitEntity != currentTarget) {
            fadingHitboxes.putIfAbsent(lastHitEntity, new FadingHitbox(lastHitEntity, currentTime));
        }

        // Remove the current target from fading hitboxes
        if (currentTarget != null) {
            fadingHitboxes.remove(currentTarget);
        }

        lastHitEntity = currentTarget;
    }

    private void renderEntityHitbox(EntityLivingBase entity, Color fillColor, Color outlineColor) {
        double partialTicks = getPartialTicks();

        // Interpolate the position of the bounding box corners
        double interpolatedX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double interpolatedY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double interpolatedZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        // Viewer position (camera offset)
        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;

        // Get the bounding box and adjust for the interpolated position
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox()
                .offset(interpolatedX - entity.posX, interpolatedY - entity.posY, interpolatedZ - entity.posZ)
                .offset(-viewerX, -viewerY, -viewerZ);

        // Render the bounding box with the specified colors
        renderFilledBoundingBox(boundingBox, fillColor);
        renderBoundingBoxOutline(boundingBox, outlineColor);
    }

    private static class FadingHitbox {
        private final EntityLivingBase entity;
        private final long fadeStartTime;

        public FadingHitbox(EntityLivingBase entity, long fadeStartTime) {
            this.entity = entity;
            this.fadeStartTime = fadeStartTime;
        }

        public EntityLivingBase getEntity() {
            return entity;
        }

        public long getFadeStartTime() {
            return fadeStartTime;
        }
    }

    private Color getColor(String redKey, String greenKey, String blueKey, String alphaKey) {
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, redKey).getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, greenKey).getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, blueKey).getValDouble();
        int alpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, alphaKey).getValDouble();

        return new Color(red, green, blue, alpha);
    }

    private void renderFilledBoundingBox(AxisAlignedBB box, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth(); // Disable depth to render correctly
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull(); // Ensure all faces are rendered
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GL11.glBegin(GL11.GL_QUADS);

        // Render all faces explicitly
        // Bottom face
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        // Top face
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        // North face
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // South face
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        // West face
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // East face
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableCull(); // Re-enable face culling
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderBoundingBoxOutline(AxisAlignedBB box, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GL11.glLineWidth(2.0F); // Set line thickness
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GL11.glBegin(GL11.GL_LINES);

        // Bottom face edges
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);

        // Top face edges
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // Vertical edges connecting top and bottom faces
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private float getPartialTicks() {
        try {
            Field timerField = Minecraft.class.getDeclaredField("timer"); // Verify this field name
            timerField.setAccessible(true);
            Object timer = timerField.get(mc);

            Field renderPartialTicksField = timer.getClass().getDeclaredField("renderPartialTicks"); // Verify this field name
            renderPartialTicksField.setAccessible(true);

            return renderPartialTicksField.getFloat(timer);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0F; // Fallback to 0 if reflection fails
        }
    }

    private void resetHitInfo() {
        damageDealt = 0;
        swingCount = 0;
        lastHitTime = 0;
        // Do not reset lastHitEntity here
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

    private double normalize(long current, double start, double end) {
        return (current - start) / (end - start);
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
        String entityName = lastHitEntity != null ? lastHitEntity.getName() : "N/A";
        String damageFormatted = String.format("%.1f", lastDamageDealt); // Show damage for the last hit

        return ChatFormatting.GRAY + "[L-T " +
                entityName + ChatFormatting.GRAY +
                ",  " + damageFormatted + ChatFormatting.GRAY +
                ",  " + swingCount + ChatFormatting.GRAY + "]";
    }

    //*************************************************************
}

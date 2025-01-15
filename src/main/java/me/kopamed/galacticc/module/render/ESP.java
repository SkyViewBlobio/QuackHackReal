package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float lastRed = -1, lastGreen = -1, lastBlue = -1;
    private float lastRange = -1;
    private float lastNoRenderRange = 3.0f;
    private boolean cachedShowMonsters;
    private boolean cachedShowAnimals;
    private boolean cachedShowEndCrystals;
    private float cachedRange;
    private float cachedNoRenderRange;
//todo add player
    public ESP() {
        super("ESP", "Draws outlines or boxes around entities", false, false, Category.VISUELLES);

        Galacticc.instance.settingsManager.rSetting(new Setting("Monsters", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Animals", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EndCrystals", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rot", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Blau", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("NoRenderRange", this, 3.0, 3.0, 10.0, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 10.0F, 3.0F, 50.0F, false));
        updateCachedSettings();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null || mc.getRenderViewEntity() == null) return;

        updateCachedSettings();  // Update cached settings

        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blau").getValDouble();

        double partialTicks = mc.getRenderPartialTicks();
        RenderManager renderManager = mc.getRenderManager();

        // Update color only if it has changed
        if (red != lastRed || green != lastGreen || blue != lastBlue) {
            lastRed = red;
            lastGreen = green;
            lastBlue = blue;
        }

        // Update range and NoRenderRange if changed
        if (cachedRange != lastRange || cachedNoRenderRange != lastNoRenderRange) {
            lastRange = cachedRange;
            lastNoRenderRange = cachedNoRenderRange;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f);

        // Process entities
        for (Entity entity : mc.world.loadedEntityList) {
            double distanceSq = mc.player.getDistanceSq(entity);

            // Skip early if the entity is far beyond the maximum range
            if (distanceSq > cachedRange * cachedRange) continue;

            // Perform expensive checks only after filtering by distance
            if ((cachedShowMonsters && entity instanceof IMob) ||
                    (cachedShowAnimals && entity instanceof EntityAnimal) ||
                    (cachedShowEndCrystals && entity instanceof EntityEnderCrystal)) {

                // Skip entities inside NoRenderRange
                if (mc.player.getDistance(entity) <= cachedNoRenderRange && !isObstructed(entity)) continue;
                if (!isEntityInFrustum(entity)) continue;
                if (!isEntityInFOV(entity)) continue;

                drawEntityOutline(entity, partialTicks, renderManager);
            }
        }

        // Reset OpenGL settings
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void updateCachedSettings() {
        cachedShowMonsters = Galacticc.instance.settingsManager.getSettingByName(this, "Monsters").getValBoolean();
        cachedShowAnimals = Galacticc.instance.settingsManager.getSettingByName(this, "Animals").getValBoolean();
        cachedShowEndCrystals = Galacticc.instance.settingsManager.getSettingByName(this, "EndCrystals").getValBoolean();
        cachedRange = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();
        cachedNoRenderRange = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NoRenderRange").getValDouble();
    }

    private boolean isEntityInFrustum(Entity entity) {
        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) return false;

        Frustum frustum = new Frustum();
        frustum.setPosition(viewEntity.posX, viewEntity.posY, viewEntity.posZ);
        return frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox());
    }

    private void drawEntityOutline(Entity entity, double partialTicks, RenderManager renderManager) {
        Render<Entity> entityRenderer = renderManager.getEntityRenderObject(entity);

        if (entityRenderer != null) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderManager.viewerPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - renderManager.viewerPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderManager.viewerPosZ;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GL11.glLineWidth(2.0f);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            // Render the entity outline
            entityRenderer.doRender(entity, 0.0f, 0.0f, 0.0f, entity.rotationYaw, (float) partialTicks);

            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GlStateManager.popMatrix();
        }
    }

    private boolean isEntityInFOV(Entity entity) {
        Vec3d playerLookVec = mc.player.getLookVec();
        Vec3d entityVec = new Vec3d(entity.posX - mc.player.posX, entity.posY - mc.player.posY, entity.posZ - mc.player.posZ);
        double dotProduct = playerLookVec.dotProduct(entityVec);
        double magnitude = playerLookVec.lengthVector() * entityVec.lengthVector();

        double angle = Math.acos(dotProduct / magnitude);
        double fov = Math.toRadians(mc.gameSettings.fovSetting);
        return angle <= fov;
    }

    /**
     * Checks if the line of sight to the entity is obstructed by a block.
     */
    private boolean isObstructed(Entity entity) {
        Vec3d playerPos = mc.player.getPositionEyes(1.0f);
        Vec3d entityPos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);

        RayTraceResult result = mc.world.rayTraceBlocks(playerPos, entityPos, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK;
    }
}

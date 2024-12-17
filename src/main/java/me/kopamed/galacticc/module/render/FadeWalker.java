package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//todo add Box/top face mode.
public class FadeWalker extends Module {
    public FadeWalker() {
        super("FadeWalker", "@Hauptinformation: " +
                "Zeigt Farben auf Bloecken auf den sich das ausgewaehlte Tier/Monster befindet an. " + "Optionen: " +
                        "- Duration gibt die Verblassungszeit an. || " +
                        "- Alpha gibt die Staerke der Farbe an. || " +
                        "- Range gibt die Reichweite an indem Tier/Monster angezeigt werden."
                , false, false, Category.VISUELLES);

        // Adding sliders for Fade Speed, RGBA values, and Range
        Galacticc.instance.settingsManager.rSetting(new Setting("Duration", this, 5.0F, 1.0F, 40.0F, false)); // Duration slider (seconds)
        Galacticc.instance.settingsManager.rSetting(new Setting("Rot", this, 0, 0, 255, true)); // Red slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Green", this, 255, 0, 255, true)); // Green slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Blau", this, 0, 0, 255, true)); // Blue slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Alpha", this, 0.3F, 0.0F, 1.0F, false)); // Alpha slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 10.0F, 3.0F, 15.0F, false)); // Range slider

        // Adding settings for each mob type
        Galacticc.instance.settingsManager.rSetting(new Setting("Zombie", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Creeper", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Enderman", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Skeleton", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Witch", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Slime", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("MagmaCube", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Spider", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Endermite", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Guardian", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("ZombiePigmen", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Silverfish", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("CaveSpider", this, false));

        // Animals
        Galacticc.instance.settingsManager.rSetting(new Setting("Pig", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Cow", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Squid", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Villager", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Chicken", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Rabbit", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("IronGolem", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Horse", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Wolf", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Sheep", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Ocelot", this, false));

    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<BlockPos, Float> blockFadeMap = new HashMap<>();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // Retrieve slider values
        float duration = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Duration").getValDouble();
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blau").getValDouble();
        float alpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Alpha").getValDouble();
        float range = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();

        float fadeDecrement = 1.0F / (duration * 40.0F);
        Vec3 playerPos = mc.thePlayer.getPositionVector();

        // Calculate the dynamic FOV threshold using the player's current FOV setting
        float playerFOV = mc.gameSettings.fovSetting; // Current FOV (default ~70-90)
        double threshold = Math.cos(Math.toRadians(playerFOV / 2)); // Convert FOV to a cosine value

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (shouldConsiderEntity(entity)) {
                Vec3 playerLookVec = mc.thePlayer.getLookVec().normalize();
                Vec3 entityVec = new Vec3(entity.posX - mc.thePlayer.posX,
                        entity.posY - mc.thePlayer.posY,
                        entity.posZ - mc.thePlayer.posZ).normalize();

                double dotProduct = playerLookVec.dotProduct(entityVec);
                if (dotProduct < threshold) continue; // Skip entities outside FOV

                BlockPos entityPos = new BlockPos(entity.posX, entity.posY - 1, entity.posZ);
                if (playerPos.distanceTo(new Vec3(entityPos.getX() + 0.5,
                        entityPos.getY() + 0.5,
                        entityPos.getZ() + 0.5)) <= range) {
                    blockFadeMap.put(entityPos, 1.0f);
                }
            }
        }

        // Render and fade logic for blocks
        Iterator<Map.Entry<BlockPos, Float>> iterator = blockFadeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Float> entry = iterator.next();
            BlockPos blockPos = entry.getKey();
            float currentAlpha = entry.getValue();

            renderBlockFade(blockPos, currentAlpha, red, green, blue, alpha);

            currentAlpha -= fadeDecrement;
            if (currentAlpha <= 0) {
                iterator.remove();
            } else {
                entry.setValue(currentAlpha);
            }
        }
    }

    private boolean shouldConsiderEntity(Entity entity) {
      if (entity == null) return false;

      if (entity instanceof EntityZombie && Galacticc.instance.settingsManager.getSettingByName(this, "Zombie").getValBoolean()) return true;
      if (entity instanceof EntityCreeper && Galacticc.instance.settingsManager.getSettingByName(this, "Creeper").getValBoolean()) return true;
      if (entity instanceof EntityEnderman && Galacticc.instance.settingsManager.getSettingByName(this, "Enderman").getValBoolean()) return true;
      if (entity instanceof EntitySkeleton && Galacticc.instance.settingsManager.getSettingByName(this, "Skeleton").getValBoolean()) return true;
      if (entity instanceof EntityWitch && Galacticc.instance.settingsManager.getSettingByName(this, "Witch").getValBoolean()) return true;
      if (entity instanceof EntitySlime && Galacticc.instance.settingsManager.getSettingByName(this, "Slime").getValBoolean()) return true;
      if (entity instanceof EntityMagmaCube && Galacticc.instance.settingsManager.getSettingByName(this, "MagmaCube").getValBoolean()) return true;
      if (entity instanceof EntitySpider && Galacticc.instance.settingsManager.getSettingByName(this, "Spider").getValBoolean()) return true;
      if (entity instanceof EntityCaveSpider && Galacticc.instance.settingsManager.getSettingByName(this, "CaveSpider").getValBoolean()) return true;
      if (entity instanceof EntityEndermite && Galacticc.instance.settingsManager.getSettingByName(this, "Endermite").getValBoolean()) return true;
      if (entity instanceof EntityGuardian && Galacticc.instance.settingsManager.getSettingByName(this, "Guardian").getValBoolean()) return true;
      if (entity instanceof EntityPigZombie && Galacticc.instance.settingsManager.getSettingByName(this, "ZombiePigman").getValBoolean()) return true;
      if (entity instanceof EntitySilverfish && Galacticc.instance.settingsManager.getSettingByName(this, "Silverfish").getValBoolean()) return true;

      if (entity instanceof EntityPig && Galacticc.instance.settingsManager.getSettingByName(this, "Pig").getValBoolean()) return true;
      if (entity instanceof EntityCow && Galacticc.instance.settingsManager.getSettingByName(this, "Cow").getValBoolean()) return true;
      if (entity instanceof EntitySheep && Galacticc.instance.settingsManager.getSettingByName(this, "Sheep").getValBoolean()) return true;
      if (entity instanceof EntityChicken && Galacticc.instance.settingsManager.getSettingByName(this, "Chicken").getValBoolean()) return true;
      if (entity instanceof EntitySquid && Galacticc.instance.settingsManager.getSettingByName(this, "Squid").getValBoolean()) return true;
      if (entity instanceof EntityVillager && Galacticc.instance.settingsManager.getSettingByName(this, "Villager").getValBoolean()) return true;
      if (entity instanceof EntityRabbit && Galacticc.instance.settingsManager.getSettingByName(this, "Rabbit").getValBoolean()) return true;
      if (entity instanceof EntityHorse && Galacticc.instance.settingsManager.getSettingByName(this, "Horse").getValBoolean()) return true;
      if (entity instanceof EntityWolf && Galacticc.instance.settingsManager.getSettingByName(this, "Wolf").getValBoolean()) return true;
      if (entity instanceof EntityOcelot && Galacticc.instance.settingsManager.getSettingByName(this, "Ocelot").getValBoolean()) return true;
      if (entity instanceof EntityIronGolem && Galacticc.instance.settingsManager.getSettingByName(this, "IronGolem").getValBoolean()) return true;

 return false;
}

    private void renderBlockFade(BlockPos blockPos, float fadeAlpha, int red, int green, int blue, float alphaMultiplier) {
        AxisAlignedBB boundingBox = mc.theWorld.getBlockState(blockPos).getBlock()
                .getSelectedBoundingBox(mc.theWorld, blockPos)
                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, fadeAlpha * alphaMultiplier);
        drawFilledBox(boundingBox);

        drawOutline(boundingBox, red / 255.0f, green / 255.0f, blue / 255.0f, fadeAlpha * alphaMultiplier);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawOutline(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);

        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glEnd();
    }

    private void drawFilledBox(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glEnd();
    }
}
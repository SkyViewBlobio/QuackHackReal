package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ItemESP extends Module {

    private int lastRed = -1;
    private int lastGreen = -1;
    private int lastBlue = -1;
    private float lastAlpha = -1;

    public ItemESP() {
        super("ItemESP", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Markiert fallengelassene Gegenstaende mit| einem sichtbaren Umriss und hebt sie hervor.|" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Range: " + ChatFormatting.WHITE +
                        "Stellt die Reichweite des Markierens ein.|" + ChatFormatting.RED +
                        "- NotRender: " + ChatFormatting.WHITE +
                        "Legt fest, ab welcher Distanz ein| Gegenstand nicht mehr markiert wird. Gegenstaende in dieser Reichweite,| hinter Bloecken, werden trotzdem gerzeigt." + ChatFormatting.RED +
                        "- Linie Rot, Gruen, Blau, Alpha: " + ChatFormatting.WHITE +
                        "Erlaubt die Einstellung der Farben| und Transparenz der Markierungen.",
                false, false, Category.VISUELLES);

        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 10.0F, 3.0F, 90.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("NotRender", this, 3.0F, 1.0F, 6.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Alpha", this, 1.0F, 0.0F, 1.0F, false));
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        float range = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();
        float notRenderRange = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NotRender").getValDouble();
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Blau").getValDouble();
        float alpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Alpha").getValDouble();

        if (red != lastRed || green != lastGreen || blue != lastBlue || alpha != lastAlpha) {
            lastRed = red;
            lastGreen = green;
            lastBlue = blue;
            lastAlpha = alpha;
        }

        Vec3d renderPos = getRenderPos();
        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) {
            return;
        }

        Vec3d cameraPos = viewEntity.getPositionEyes(event.getPartialTicks());
        Vec3d viewVector = viewEntity.getLook(event.getPartialTicks());

        float fov = mc.gameSettings.fovSetting;
        double halfFov = fov / 2.0;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5F);

        for (EntityItem item : mc.world.getEntitiesWithinAABB(EntityItem.class, mc.player.getEntityBoundingBox().grow(range))) {
            Vec3d itemPos = new Vec3d(item.posX, item.posY, item.posZ);

            if (cameraPos.distanceTo(itemPos) <= notRenderRange && !isObstructed(cameraPos, itemPos)) {
                continue;
            }

            Vec3d toItem = itemPos.subtract(cameraPos).normalize();
            double dotProduct = viewVector.dotProduct(toItem);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle > halfFov || dotProduct < 0) {
                continue;
            }

            AxisAlignedBB itemBox = item.getEntityBoundingBox().offset(-renderPos.x, -renderPos.y, -renderPos.z);
            float effectiveAlpha = Math.max(0.1F, lastAlpha);
            RenderGlobal.drawSelectionBoundingBox(itemBox, lastRed / 255.0F, lastGreen / 255.0F, lastBlue / 255.0F, effectiveAlpha);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    private boolean isObstructed(Vec3d start, Vec3d end) {
        RayTraceResult result = mc.world.rayTraceBlocks(start, end, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    private Vec3d getRenderPos() {
        return new Vec3d(
                mc.getRenderManager().viewerPosX,
                mc.getRenderManager().viewerPosY,
                mc.getRenderManager().viewerPosZ
        );
    }
}
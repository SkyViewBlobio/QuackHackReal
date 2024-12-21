package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
//todo make a gradient mode for the block lines. and do cleanup
//************************Module Initialization**************************//

public class BlockHighlight extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public BlockHighlight() {
        super("BlockInfo", "@Hauptinformation: " +
                        "Laesst dich Bloecke hervorheben und zeigt dir nuetzliche Informationen ueber den Block den du anschausst. " +
                        "@Optionen: " +
                        "- Block-Typ zeigt dir den Blocknamen. || " +
                        "- Blockcoordinaten zeigt dir die Coordinaten, lol. || " +
                        "- Zeige Farben leasst dich rot, gruen, blau, dichte verandern. Ist diese Option nicht an, kannst du keine farben sehen. || " +
                        "- Show gradient Colors mixt die Farben.",
                false, false, Category.VISUELLES);

        // Initialize settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Block-Typ", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Block Coordinaten", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Zeige Farben", this, true)); // Overlay colors
        Galacticc.instance.settingsManager.rSetting(new Setting("Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Alpha", this, 0.3F, 0.0F, 1.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Linie Alpha", this, 1.0F, 0.0F, 1.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Show Gradient Colors", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Green", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Blau", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Gradient Alpha", this, 0.5F, 0.0F, 1.0F, false));
    }

    @Override
    public String getHUDInfo() {
        boolean showBlockType = Galacticc.instance.settingsManager.getSettingByName(this, "Block-Typ").getValBoolean();
        boolean showBlockCoordinates = Galacticc.instance.settingsManager.getSettingByName(this, "Block Coordinaten").getValBoolean();

        RayTraceResult rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return ChatFormatting.GRAY + "[N/B]";
        }

        BlockPos blockPos = rayTraceResult.getBlockPos();
        Block block = mc.world.getBlockState(blockPos).getBlock();

        String blockName = block.getRegistryName() != null ? block.getRegistryName().toString() : "Unknown";

        StringBuilder hudInfo = new StringBuilder(ChatFormatting.GRAY + "[B/I: ");
        if (showBlockType) {
            hudInfo.append("T: ").append(ChatFormatting.GRAY).append(blockName).append(ChatFormatting.GRAY);
        }
        if (showBlockCoordinates) {
            if (showBlockType) {
                hudInfo.append(", ");
            }
            hudInfo.append("C: ").append(ChatFormatting.GRAY)
                    .append(blockPos.getX()).append(", ")
                    .append(blockPos.getY()).append(", ")
                    .append(blockPos.getZ()).append(ChatFormatting.GRAY);
        }
        hudInfo.append("]");
        return hudInfo.toString();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        RayTraceResult rayTraceResult = mc.objectMouseOver;
        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos blockPos = rayTraceResult.getBlockPos();
        if (blockPos == null) {
            return;
        }

        boolean showColors = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige Farben").getValBoolean();
        boolean showGradientColors = Galacticc.instance.settingsManager.getSettingByName(this, "Show Gradient Colors").getValBoolean();

        if (!showColors && !showGradientColors) {
            return;
        }

        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blau").getValDouble();
        float alpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Alpha").getValDouble();

        int gradRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Rot").getValDouble();
        int gradGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Green").getValDouble();
        int gradBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Blau").getValDouble();
        float gradAlpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Gradient Alpha").getValDouble();

        int outlineRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Rot").getValDouble();
        int outlineGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Green").getValDouble();
        int outlineBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Blau").getValDouble();
        float outlineAlpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Linie Alpha").getValDouble();

        AxisAlignedBB boundingBox = mc.world.getBlockState(blockPos).getBoundingBox(mc.world, blockPos)
                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (showGradientColors) {
            int startColor = new Color(red, green, blue, (int) (alpha * 255)).getRGB();
            int endColor = new Color(gradRed, gradGreen, gradBlue, (int) (gradAlpha * 255)).getRGB();
            drawGradientBox(boundingBox, startColor, endColor);
        } else if (showColors) {
            GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha);
            drawFilledBox(boundingBox);
        }

        drawOutline(boundingBox, outlineRed / 255.0F, outlineGreen / 255.0F, outlineBlue / 255.0F, outlineAlpha);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    //************************Helper Functions**************************//

    private void drawOutline(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        GL11.glLineWidth(2.0F); // Set outline width
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

    private float interpolate(float start, float end, double t) {
        return (float) (start + t * (end - start));
    }

    private void drawGradientBox(AxisAlignedBB box, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        double gradientStart = box.minY;
        double gradientEnd = box.maxY;

        double tBottom = (box.minY - gradientStart) / (gradientEnd - gradientStart);
        double tTop = (box.maxY - gradientStart) / (gradientEnd - gradientStart);

        // Bottom face (Y = min)
        GL11.glColor4f(interpolate(startRed, endRed, tBottom), interpolate(startGreen, endGreen, tBottom), interpolate(startBlue, endBlue, tBottom), interpolate(startAlpha, endAlpha, tBottom));
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        // Top face (Y = max)
        GL11.glColor4f(interpolate(startRed, endRed, tTop), interpolate(startGreen, endGreen, tTop), interpolate(startBlue, endBlue, tTop), interpolate(startAlpha, endAlpha, tTop));
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        // North face (Z = min) - Alternative vertex ordering
        GL11.glColor4f(interpolate(startRed, endRed, tBottom), interpolate(startGreen, endGreen, tBottom), interpolate(startBlue, endBlue, tBottom), interpolate(startAlpha, endAlpha, tBottom));
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glColor4f(interpolate(startRed, endRed, tTop), interpolate(startGreen, endGreen, tTop), interpolate(startBlue, endBlue, tTop), interpolate(startAlpha, endAlpha, tTop));
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        // South face (Z = max)
        GL11.glColor4f(interpolate(startRed, endRed, tBottom), interpolate(startGreen, endGreen, tBottom), interpolate(startBlue, endBlue, tBottom), interpolate(startAlpha, endAlpha, tBottom));
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glColor4f(interpolate(startRed, endRed, tTop), interpolate(startGreen, endGreen, tTop), interpolate(startBlue, endBlue, tTop), interpolate(startAlpha, endAlpha, tTop));
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        // West face (X = min)
        GL11.glColor4f(interpolate(startRed, endRed, tBottom), interpolate(startGreen, endGreen, tBottom), interpolate(startBlue, endBlue, tBottom), interpolate(startAlpha, endAlpha, tBottom));
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glColor4f(interpolate(startRed, endRed, tTop), interpolate(startGreen, endGreen, tTop), interpolate(startBlue, endBlue, tTop), interpolate(startAlpha, endAlpha, tTop));
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // East face (X = max)
        GL11.glColor4f(interpolate(startRed, endRed, tBottom), interpolate(startGreen, endGreen, tBottom), interpolate(startBlue, endBlue, tBottom), interpolate(startAlpha, endAlpha, tBottom));
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glColor4f(interpolate(startRed, endRed, tTop), interpolate(startGreen, endGreen, tTop), interpolate(startBlue, endBlue, tTop), interpolate(startAlpha, endAlpha, tTop));
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
    }
}
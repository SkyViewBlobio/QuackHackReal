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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;
//todo make a gradient mode for the block lines. and do cleanup
//************************Module Initialization**************************//

public class BlockHighlight extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public BlockHighlight() {
        super("BlockInfo", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Ermoeglicht das Hervorheben von Bloecken und zeigt| dir nuetzliche Informationen ueber| den Block, den du anschaust." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Block-Typ: " + ChatFormatting.WHITE +
                        "Zeigt dir den| Namen des Blocks an." + ChatFormatting.RED +
                        "- Block Coordinaten: " + ChatFormatting.WHITE +
                        "Zeigt dir die| Koordinaten des Blocks an." + ChatFormatting.RED +
                        "- Zeige Farben: " + ChatFormatting.WHITE +
                        "Ermoeglicht dir, die Farben (Rot, Gruen, Blau)| und die Dichte einzustellen. Diese Option muss aktiviert| sein, um Farben anzuzeigen." + ChatFormatting.RED +
                        "- Show Gradient Colors: " + ChatFormatting.WHITE +
                        "Mischt die eingestellten| Farben, um einen Farbverlauf zu erzeugen.",
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

//************************HUD Information**************************//

    @Override
    public String getHUDInfo() {
        boolean showBlockType = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block-Typ").getValBoolean();
        boolean showBlockCoordinates = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block Coordinaten").getValBoolean();

        RayTraceResult rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return TextFormatting.GRAY + "[N/B]";
        }

        BlockPos blockPos = rayTraceResult.getBlockPos();
        Block block = mc.world.getBlockState(blockPos).getBlock();

        String fullBlockName = (ForgeRegistries.BLOCKS.getKey(block) != null)
                ? Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString()
                : "unknown";

        String blockName = fullBlockName.contains(":") ? fullBlockName.split(":")[1] : fullBlockName;

        StringBuilder hudInfo = new StringBuilder(TextFormatting.GRAY + "[B/I: ");

        if (showBlockType) {
            hudInfo.append("T: ").append(TextFormatting.GRAY).append(blockName).append(TextFormatting.GRAY);
        }

        if (showBlockCoordinates) {
            if (showBlockType) {
                hudInfo.append(", ");
            }
            hudInfo.append("C: ").append(TextFormatting.GRAY)
                    .append(blockPos.getX()).append(", ")
                    .append(blockPos.getY()).append(", ")
                    .append(blockPos.getZ()).append(TextFormatting.GRAY);
        }

        hudInfo.append("]");
        return hudInfo.toString();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        // Ensure RayTraceResult is valid and is hitting a block
        RayTraceResult rayTraceResult = mc.objectMouseOver;
        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        // Get the BlockPos of the block we are looking at
        BlockPos blockPos = rayTraceResult.getBlockPos();
        if (!mc.world.isBlockLoaded(blockPos)) {
            return;
        }

        // Fetch settings for rendering
        boolean showColors = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige Farben").getValBoolean();
        boolean showGradientColors = Galacticc.instance.settingsManager.getSettingByName(this, "Show Gradient Colors").getValBoolean();
        if (!showColors && !showGradientColors) {
            return;
        }

        // Fetch color settings
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

        // Correctly calculate the bounding box for the block
        AxisAlignedBB boundingBox = mc.world.getBlockState(blockPos).getBoundingBox(mc.world, blockPos)
                .offset(blockPos.getX() - mc.getRenderManager().viewerPosX,
                        blockPos.getY() - mc.getRenderManager().viewerPosY,
                        blockPos.getZ() - mc.getRenderManager().viewerPosZ);

        // Start rendering
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Render gradient or solid color based on settings
        if (showGradientColors) {
            int startColor = new Color(red, green, blue, (int) (alpha * 255)).getRGB();
            int endColor = new Color(gradRed, gradGreen, gradBlue, (int) (gradAlpha * 255)).getRGB();
            drawGradientBox(boundingBox, startColor, endColor);
        }

        if (showColors) {
            GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha);
            drawFilledBox(boundingBox);
        }

        // Draw the outline of the block
        drawOutline(boundingBox, outlineRed / 255.0F, outlineGreen / 255.0F, outlineBlue / 255.0F, outlineAlpha);

        // Reset OpenGL state
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
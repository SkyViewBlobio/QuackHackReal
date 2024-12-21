package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HoleESP extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public HoleESP() {
        super("HoleESP", "Highlights safe and unsafe holes within a specified radius.",
                false, false, Category.VISUELLES);

        // Safe hole RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeRed", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeGreen", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeBlue", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeAlpha", this, 0.5F, 0.0F, 1.0F, false));

        // Unsafe hole RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("Unsa" +
                "feRed", this, 255.0F, 0.0F, 255.0F, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("UnsafeGreen", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("UnsafeBlue", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("UnsafeAlpha", this, 0.5F, 0.0F, 1.0F, false));

        // Range slider
        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 10.0F, 3.0F, 20.0F, false));

        // Mode settings
        ArrayList<String> modes = new ArrayList<>();
        modes.add("BoxRender");
        modes.add("BottomFace");
        Galacticc.instance.settingsManager.rSetting(new Setting("Mode", this, "BoxRender", modes));
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // Retrieve slider values
        int safeRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "SafeRed").getValDouble();
        int safeGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "SafeGreen").getValDouble();
        int safeBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "SafeBlue").getValDouble();
        float safeAlpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "SafeAlpha").getValDouble();

        int unsafeRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "UnsafeRed").getValDouble();
        int unsafeGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "UnsafeGreen").getValDouble();
        int unsafeBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "UnsafeBlue").getValDouble();
        float unsafeAlpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "UnsafeAlpha").getValDouble();

        float range = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();

        // Get current mode
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();

        // Find and render holes
        Set<BlockPos> checkedPositions = new HashSet<>();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-range, -5, -range), playerPos.add(range, 5, range))) {
            if (checkedPositions.contains(pos)) continue;
            checkedPositions.add(pos);

            if (isSafeHole(pos)) {
                renderBlock(pos, safeRed, safeGreen, safeBlue, safeAlpha, mode);
            } else if (isUnsafeHole(pos)) {
                renderBlock(pos, unsafeRed, unsafeGreen, unsafeBlue, unsafeAlpha, mode);
            }
        }
    }

    private boolean isSafeHole(BlockPos pos) {
        if (isInvalidEntryPoint(pos)) return false;

        return mc.theWorld.getBlockState(pos.down()).getBlock() == Blocks.bedrock &&
                mc.theWorld.getBlockState(pos.north()).getBlock() == Blocks.bedrock &&
                mc.theWorld.getBlockState(pos.south()).getBlock() == Blocks.bedrock &&
                mc.theWorld.getBlockState(pos.east()).getBlock() == Blocks.bedrock &&
                mc.theWorld.getBlockState(pos.west()).getBlock() == Blocks.bedrock;
    }

    private boolean isUnsafeHole(BlockPos pos) {
        if (isInvalidEntryPoint(pos)) return false;

        boolean hasBedrock = false;
        boolean hasObsidian = false;

        Block[] surroundingBlocks = {
                mc.theWorld.getBlockState(pos.down()).getBlock(),
                mc.theWorld.getBlockState(pos.north()).getBlock(),
                mc.theWorld.getBlockState(pos.south()).getBlock(),
                mc.theWorld.getBlockState(pos.east()).getBlock(),
                mc.theWorld.getBlockState(pos.west()).getBlock()
        };

        for (Block block : surroundingBlocks) {
            if (block == Blocks.bedrock) {
                hasBedrock = true;
            } else if (block == Blocks.obsidian) {
                hasObsidian = true;
            } else {
                return false; // Not a valid hole
            }
        }

        return hasObsidian;
    }

    private boolean isInvalidEntryPoint(BlockPos pos) {
        // First air block
        if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.air) {
            return true;
        }

        // Second air block (above the first)
        BlockPos secondBlockPos = pos.up();
        if (mc.theWorld.getBlockState(secondBlockPos).getBlock() != Blocks.air) {
            return true;
        }

        // Third air block (above the second)
        BlockPos thirdBlockPos = secondBlockPos.up();
        if (mc.theWorld.getBlockState(thirdBlockPos).getBlock() != Blocks.air) {
            return true;
        }

        // Ensure the third block is not fully enclosed
        Block[] surroundingThirdBlocks = {
                mc.theWorld.getBlockState(thirdBlockPos.north()).getBlock(),
                mc.theWorld.getBlockState(thirdBlockPos.south()).getBlock(),
                mc.theWorld.getBlockState(thirdBlockPos.east()).getBlock(),
                mc.theWorld.getBlockState(thirdBlockPos.west()).getBlock(),
                mc.theWorld.getBlockState(thirdBlockPos.down()).getBlock()
        };

        for (Block block : surroundingThirdBlocks) {
            if (block == Blocks.air) {
                return false; // Not fully enclosed
            }
        }

        return true; // Fully enclosed or invalid
    }

    private void renderBlock(BlockPos blockPos, int red, int green, int blue, float alpha, String mode) {
        AxisAlignedBB boundingBox = new AxisAlignedBB(
                blockPos.getX() - mc.getRenderManager().viewerPosX,
                blockPos.getY() - mc.getRenderManager().viewerPosY,
                blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ
        );

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Disable depth testing for x-ray effect
        GlStateManager.disableDepth();

        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha);

        if (mode.equals("BoxRender")) {
            drawFilledBox(boundingBox); // Ensure proper outer face rendering
            drawOutline(boundingBox, red / 255.0f, green / 255.0f, blue / 255.0f, alpha);
        } else if (mode.equals("BottomFace")) {
            drawBottomFace(boundingBox);
        }

        // Re-enable depth testing for other rendering operations
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    private void drawBottomFace(AxisAlignedBB box) {
        // Move the face slightly upward to avoid z-fighting
        double offset = 0.01;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth(); // Disable depth testing to ensure visibility

        // Draw the inverted bottom face
        GL11.glBegin(GL11.GL_QUADS);

        // Render the face oriented upwards
        GL11.glVertex3d(box.minX, box.minY + offset, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY + offset, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY + offset, box.minZ);
        GL11.glVertex3d(box.minX, box.minY + offset, box.minZ);

        GL11.glEnd();

        GlStateManager.enableDepth(); // Re-enable depth testing
        GlStateManager.enableTexture2D();
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

        // Bottom face (Y negative)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        // Top face (Y positive)
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        // North face (Z negative)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        // South face (Z positive)
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        // West face (X negative)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // East face (X positive)
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glEnd();
    }

}

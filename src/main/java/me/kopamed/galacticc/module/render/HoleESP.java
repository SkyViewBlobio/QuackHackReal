package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
//todo cleanup
public class HoleESP extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ICamera camera = new Frustum();

    public HoleESP() {
        super("HoleESP", "Highlights safe and unsafe holes within a specified radius.",
                false, false, Category.VISUELLES);

        // Safe hole RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeRed", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeGreen", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeBlue", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SafeAlpha", this, 0.5F, 0.0F, 1.0F, false));

        // Unsafe hole RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("UnsafeRed", this, 255.0F, 0.0F, 255.0F, true));
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
        if (mc.world == null || mc.player == null) return;

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
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();

        // Pre-render setup
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        Set<BlockPos> checkedPositions = new HashSet<>();
        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        // Cache for the last color state
        int lastRed = -1, lastGreen = -1, lastBlue = -1;
        float lastAlpha = -1;

        for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-range, -5, -range), playerPos.add(range, 5, range))) {
            AxisAlignedBB blockAABB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!camera.isBoundingBoxInFrustum(blockAABB)) {
                continue; // Skip rendering if outside the view
            }
            if (checkedPositions.contains(pos)) continue;
            checkedPositions.add(pos);

            int red, green, blue;
            float alpha;

            if (isSafeHole(pos)) {
                red = safeRed;
                green = safeGreen;
                blue = safeBlue;
                alpha = safeAlpha;
            } else if (isUnsafeHole(pos)) {
                red = unsafeRed;
                green = unsafeGreen;
                blue = unsafeBlue;
                alpha = unsafeAlpha;
            } else {
                continue;
            }

            // Only update the color if it has changed
            if (red != lastRed || green != lastGreen || blue != lastBlue || alpha != lastAlpha) {
                GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha);
                lastRed = red;
                lastGreen = green;
                lastBlue = blue;
                lastAlpha = alpha;
            }

            renderBlockDirect(pos, red, green, blue, alpha, mode);
        }

        // Post-render cleanup
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderBlockDirect(BlockPos blockPos, int red, int green, int blue, float alpha, String mode) {
        AxisAlignedBB boundingBox = new AxisAlignedBB(
                blockPos.getX() - mc.getRenderManager().viewerPosX,
                blockPos.getY() - mc.getRenderManager().viewerPosY,
                blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ
        );

        // Set the color once before rendering
        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha);

        // Batch rendering for mode
        if (mode.equals("BoxRender")) {
            drawFilledBox(boundingBox);
            drawOutline(boundingBox, red / 255.0f, green / 255.0f, blue / 255.0f, alpha);
        } else if (mode.equals("BottomFace")) {
            drawBottomFace(boundingBox);
        }
    }

    private boolean isSafeHole(BlockPos pos) {
        if (isInvalidEntryPoint(pos)) return false;

        // Fetch all relevant block states at once
        IBlockState[] surroundingStates = {
                mc.world.getBlockState(pos.down()),
                mc.world.getBlockState(pos.north()),
                mc.world.getBlockState(pos.south()),
                mc.world.getBlockState(pos.east()),
                mc.world.getBlockState(pos.west())
        };

        // Check if all blocks are bedrock
        for (IBlockState state : surroundingStates) {
            if (state.getBlock() != Blocks.BEDROCK) {
                return false;
            }
        }

        return true;
    }

    private boolean isUnsafeHole(BlockPos pos) {
        if (isInvalidEntryPoint(pos)) return false;

        // Fetch all relevant block states at once
        IBlockState[] surroundingStates = {
                mc.world.getBlockState(pos.down()),
                mc.world.getBlockState(pos.north()),
                mc.world.getBlockState(pos.south()),
                mc.world.getBlockState(pos.east()),
                mc.world.getBlockState(pos.west())
        };

        boolean hasObsidian = false;

        // Check for obsidian and invalid blocks
        for (IBlockState state : surroundingStates) {
            Block block = state.getBlock();
            if (block == Blocks.OBSIDIAN) {
                hasObsidian = true;
            } else if (block != Blocks.BEDROCK) {
                return false; // Not a valid hole
            }
        }

        return hasObsidian;
    }

    private boolean isInvalidEntryPoint(BlockPos pos) {
        // First air block
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
            return true;
        }

        // Second air block (above the first)
        BlockPos secondBlockPos = pos.up();
        if (mc.world.getBlockState(secondBlockPos).getBlock() != Blocks.AIR) {
            return true;
        }

        // Third air block (above the second)
        BlockPos thirdBlockPos = secondBlockPos.up();
        if (mc.world.getBlockState(thirdBlockPos).getBlock() != Blocks.AIR) {
            return true;
        }

        // Ensure the third block is not fully enclosed
        Block[] surroundingThirdBlocks = {
                mc.world.getBlockState(thirdBlockPos.north()).getBlock(),
                mc.world.getBlockState(thirdBlockPos.south()).getBlock(),
                mc.world.getBlockState(thirdBlockPos.east()).getBlock(),
                mc.world.getBlockState(thirdBlockPos.west()).getBlock(),
                mc.world.getBlockState(thirdBlockPos.down()).getBlock()
        };

        for (Block block : surroundingThirdBlocks) {
            if (block == Blocks.AIR) {
                return false; // Not fully enclosed
            }
        }

        return true; // Fully enclosed or invalid
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

        // Start drawing lines only once
        GL11.glBegin(GL11.GL_LINES);

        // Use passed colors
        GL11.glColor4f(red, green, blue, alpha);

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

        GL11.glEnd(); // End drawing lines once
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
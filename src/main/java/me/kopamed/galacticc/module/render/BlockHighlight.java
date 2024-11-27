package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class BlockHighlight extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public BlockHighlight() {
        super("BlockInfo", "Shows valid information of the block you're looking at", false, false, Category.VISUELLES);

        // Register settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Block-Typ", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Block Coordinaten", this, true));

        // RGB sliders for highlighting color
        Galacticc.instance.settingsManager.rSetting(new Setting("Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Blue", this, 255, 0, 255, true));
    }

    @Override
    public String getHUDInfo() {
        // Fetch settings
        boolean showBlockType = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block-Typ").getValBoolean();
        boolean showBlockCoordinates = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block Coordinaten").getValBoolean();

        // Perform a ray trace to determine the block being hovered over
        MovingObjectPosition rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return ChatFormatting.GRAY + "[N/B]";
        }

        // Get block position and block type
        BlockPos blockPos = rayTraceResult.getBlockPos();
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        // Extract block name and strip the "minecraft:" prefix if present
        String fullBlockName = Block.blockRegistry.getNameForObject(block).toString();
        String blockName = fullBlockName.contains(":") ? fullBlockName.split(":")[1] : fullBlockName;

        // Build HUD info based on settings
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
        // Perform a ray trace to determine the block being hovered over
        MovingObjectPosition rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        // Get block position
        BlockPos blockPos = rayTraceResult.getBlockPos();

        // Fetch RGB values from settings
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blue").getValDouble();

        // Calculate highlight box dimensions
        AxisAlignedBB boundingBox = mc.theWorld.getBlockState(blockPos).getBlock().getSelectedBoundingBox(mc.theWorld, blockPos)
                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        // Render the filled box
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.3F); // Use RGB and add transparency

        drawFilledBox(boundingBox);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawFilledBox(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_QUADS);

        // Bottom face (Y = min)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        // Top face (Y = max)
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        // North face (Z = min)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        // South face (Z = max)
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        // West face (X = min)
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        // East face (X = max)
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glEnd();
    }
}


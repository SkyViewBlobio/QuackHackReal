package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class CompassMap extends Module {

    private final Minecraft mc;

    public CompassMap() {
        super("CompassKarte", "IN ARBEIT; NICHT FUNKTIONSTUECHTIG!", false, false, Category.VISUELLES);
        //todo bunch of optimisations and fixes especially of how the light/darkness is applied. at the end this should be like a small journeymap less detailed.

        this.mc = Minecraft.getMinecraft();

        // Add settings for adjustable coordinates
        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 0, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Radius", this, 75, 50, 200, true));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (!this.isToggled()) {
            return;
        }

        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();
        int radius = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Radius").getValDouble();

        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2 + xOffset;
        int centerY = sr.getScaledHeight() / 2 + yOffset;

        renderCircularMap(centerX, centerY, radius);
    }

    private void renderCircularMap(int centerX, int centerY, int radius) {
        World world = mc.theWorld;

        if (world == null) {
            return;
        }

        // Enable stencil buffer for circular rendering
        GlStateManager.pushMatrix();
        enableCircularStencil(centerX, centerY, radius);

        // Render the map contents
        renderMapContents(world, centerX, centerY, radius);

        // Disable stencil
        disableStencil();
        GlStateManager.popMatrix();
    }

    private void enableCircularStencil(int centerX, int centerY, int radius) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        // Draw the circular stencil
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(centerX, centerY);
        for (int i = 0; i <= 360; i++) {
            double angle = Math.toRadians(i);
            double x = centerX + Math.sin(angle) * radius;
            double y = centerY + Math.cos(angle) * radius;
            GL11.glVertex2d(x, y);
        }
        GL11.glEnd();

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    private void disableStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private void renderMapContents(World world, int centerX, int centerY, int radius) {
        EntityPlayer player = mc.thePlayer;

        if (player == null || world == null) {
            return;
        }

        int playerX = (int) player.posX;
        int playerZ = (int) player.posZ;

        RenderHelper.enableGUIStandardItemLighting();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue; // Skip blocks outside the circular map
                }

                int worldX = playerX + dx;
                int worldZ = playerZ + dz;

                BlockPos topBlock = new BlockPos(worldX, world.getTopSolidOrLiquidBlock(new BlockPos(worldX, 0, worldZ)).getY(), worldZ);
                IBlockState state = world.getBlockState(topBlock);

                // Get base color
                int baseColor = getBlockColorFromState(world, topBlock);

                // Apply darkness shading
                int colorWithDarkness = applyDarknessShade(world, topBlock, baseColor);

                // Draw the pixel on the minimap
                drawPixel(centerX + dx, centerY + dz, colorWithDarkness);
            }
        }

        RenderHelper.disableStandardItemLighting();
    }

    private int getBlockColorFromState(World world, BlockPos pos) {
        // Get the block state at the given position
        IBlockState state = world.getBlockState(pos);

        // Determine color based on block properties
        Block block = state.getBlock();

        // Use block properties for efficient rendering
        if (block == Blocks.grass) {
            return getGrassColor(world, pos); // Grass color depends on biome
        } else if (block == Blocks.water) {
            return getWaterColor(world, pos, state);
        } else if (block == Blocks.lava) {
            return new Color(255, 69, 0).getRGB(); // Bright orange for lava
        } else if (block == Blocks.bedrock) {
            return Color.DARK_GRAY.getRGB(); // Dark gray for bedrock
        } else if (block == Blocks.stone) {
            return Color.GRAY.getRGB(); // Gray for stone
        } else if (block == Blocks.sand) {
            return new Color(237, 201, 175).getRGB(); // Sandy color
        } else if (block == Blocks.leaves || block == Blocks.leaves2) {
            return new Color(34, 139, 34, 150).getRGB(); // Transparent green for leaves
        } else if (block == Blocks.torch) {
            return new Color(255, 255, 100).getRGB(); // Yellow for torches
        } else if (block == Blocks.planks) {
            return new Color(139, 69, 19).getRGB(); // Brown for wood
        } else if (block == Blocks.cobblestone) {
            return new Color(112, 112, 112).getRGB(); // Light gray for cobblestone
        }

        // Default to a neutral gray for unrecognized blocks
        return Color.LIGHT_GRAY.getRGB();
    }

    private int getGrassColor(World world, BlockPos pos) {
        // Get the biome at the given position
        BiomeGenBase biome = world.getBiomeGenForCoords(pos);

        // Calculate the grass color using BiomeGenBase's method
        return biome.getGrassColorAtPos(pos);
    }

    private int applyDarknessShade(World world, BlockPos pos, int baseColor) {
        // Separate sky and block light levels
        int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos); // Light from torches, lava, etc.
        int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos);     // Light from the sky
        boolean isDay = world.isDaytime();

        // Adjust sky light for nighttime
        if (!isDay) {
            skyLight *= 0.1; // Significantly dim the sky light at night
        }

        // Calculate combined light level
        int totalLight = Math.max(blockLight, skyLight);

        // Darkness factor based on total light level
        float darknessFactor = 1.0f - (totalLight / 15.0f);

        // Apply darkness to the base color
        Color original = new Color(baseColor);
        int r = (int) (original.getRed() * (1.0f - 0.6f * darknessFactor));
        int g = (int) (original.getGreen() * (1.0f - 0.6f * darknessFactor));
        int b = (int) (original.getBlue() * (1.0f - 0.6f * darknessFactor));

        return new Color(r, g, b).getRGB();
    }

    private int getWaterColor(World world, BlockPos pos, IBlockState state) {
        // Water can have transparency and biome-specific colors
        boolean isFlowing = state.getBlock() == Blocks.flowing_water;
        int baseColor = new Color(64, 164, 223).getRGB(); // Default water color

        if (isFlowing) {
            baseColor = darkenColor(baseColor, 0.9f); // Flowing water is slightly darker
        }

        // Optionally, adjust based on biome
        return baseColor;
    }

    private BlockPos getTopBlockUsingChunks(World world, int chunkX, int chunkZ, int blockX, int blockZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int highestY = chunk.getTopFilledSegment() + 15; // Get top Y coordinate in the chunk

        for (int y = highestY; y >= 0; y--) {
            BlockPos pos = new BlockPos(blockX, y, blockZ);
            IBlockState state = chunk.getBlockState(pos);

            if (!state.getBlock().isAir(world, pos)) {
                return pos;
            }
        }
        return null;
    }

    private int adjustColorForLight(int baseColor, int lightLevel) {
        // Light levels range from 0 (dark) to 15 (fully lit)
        if (lightLevel == 0) {
            // Apply a subtle dark overlay to shadows
            return darkenColor(baseColor, 0.7f); // 70% brightness
        } else {
            // Gradually reduce the dark overlay as light level increases
            float brightnessFactor = 0.7f + (lightLevel / 15.0f) * 0.3f; // From 70% to 100%
            return darkenColor(baseColor, brightnessFactor);
        }
    }

    private int darkenColor(int color, float factor) {
        // Decompose the color into RGB components
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Apply the brightness factor
        r = (int) (r * factor);
        g = (int) (g * factor);
        b = (int) (b * factor);

        // Recompose the color
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawPixelWithLightTransition(int x, int y, int baseColor, int lightLevel) {
        // If it's a light source, add a glowing yellow overlay
        if (lightLevel > 12) { // Consider levels 13-15 as light sources
            int glowColor = new Color(255, 255, 100).getRGB(); // Soft yellow
            drawGradientPixel(x, y, baseColor, glowColor, lightLevel);
        } else {
            // Otherwise, darken the base color based on light level
            int adjustedColor = adjustColorForLight(baseColor, lightLevel);
            drawPixel(x, y, adjustedColor);
        }
    }

    private void drawGradientPixel(int x, int y, int baseColor, int glowColor, int lightLevel) {
        float glowFactor = (lightLevel - 12) / 3.0f; // Transition from 0 to 1 as light level goes 13 to 15
        int blendedColor = blendColors(baseColor, glowColor, glowFactor);
        drawPixel(x, y, blendedColor);
    }

    private int blendColors(int color1, int color2, float blendFactor) {
        // Decompose colors
        int r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;

        // Blend each component
        int r = (int) (r1 * (1 - blendFactor) + r2 * blendFactor);
        int g = (int) (g1 * (1 - blendFactor) + g2 * blendFactor);
        int b = (int) (b1 * (1 - blendFactor) + b2 * blendFactor);

        // Recompose the color
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawPixel(int x, int y, int color) {
        Gui.drawRect(x, y, x + 1, y + 1, color);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
    }
}


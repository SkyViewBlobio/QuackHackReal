package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
//todo cleanup and fix also make the close up minimizer adjustable via slider bc ente cant see shit
public class NameTagHighlight extends Module {

    public NameTagHighlight() {
        super("NameTagHighlight", "Highlights player nametags with custom colors and size", false, false, Category.VISUELLES);

        // Keep your existing settings structure
        Galacticc.instance.settingsManager.rSetting(new Setting("NameTag Red", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("NameTag Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("NameTag Blue", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("NameTag Alpha", this, 1.0F, 0.0F, 1.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("NameTag Size", this, 1.0F, 0.5F, 3.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("ItemInfo", this, false));
    }

    @SubscribeEvent
    public void onRender(RenderLivingEvent.Specials.Pre<EntityPlayer> event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();

        if (player == mc.player) return; // Skip the local player

        // Use your settings for customization
        float red = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NameTag Red").getValDouble();
        float green = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NameTag Green").getValDouble();
        float blue = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NameTag Blue").getValDouble();
        float alpha = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NameTag Alpha").getValDouble();
        float baseSize = (float) Galacticc.instance.settingsManager.getSettingByName(this, "NameTag Size").getValDouble();
        boolean itemInfoEnabled = Galacticc.instance.settingsManager.getSettingByName(this, "ItemInfo").getValBoolean();

        RenderManager renderManager = mc.getRenderManager();
        double x = player.posX - renderManager.viewerPosX;
        double y = player.posY - renderManager.viewerPosY + player.height + 0.5;
        double z = player.posZ - renderManager.viewerPosZ;

        // Adjust size based on distance
        float distance = mc.player.getDistance(player);
        float size = calculateDynamicSize(baseSize, distance);

        // Check if the text is still visible, shrink further if too close
        if (distance < 5) {
            size = adjustSizeForVisibility(size, x, y, z);
        }

        // Render nametag and item info sections in the correct order
        renderNameTagWithItems(player, x, y, z, red, green, blue, alpha, size, itemInfoEnabled);

        event.setCanceled(true); // Prevent default rendering
    }

    private void renderNameTagWithItems(EntityPlayer player, double x, double y, double z, float red, float green, float blue, float alpha, float size, boolean itemInfoEnabled) {
        RenderManager renderManager = mc.getRenderManager();
        float scale = 0.01666667F * size;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        // Disable depth for proper rendering
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Adjust initial baseY for more room for text
        int baseY = -10; // Start position for Y, adjusted to be higher
        int baseX = 0; // Start position for X (centered)

        // Render armor health percentages first
        renderArmorHealthPercentages(player, baseX, baseY);

        // Render armor piece numbers second
        baseY -= 25; // Adjust offset (move upwards)
        renderArmorNumbers(player, baseX, baseY);

        // Render item names (Mainhand and Offhand) third, if enabled
        if (itemInfoEnabled) {
            baseY -= 25; // Adjust offset again (move upwards)
            renderItemNames(player, baseX, baseY);
        }

        // Render the name tag last
        baseY -= 25; // Adjust for the player name tag (move upwards)
        renderNameTag(player, baseX, baseY, red, green, blue, alpha);

        // Restore OpenGL settings
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderArmorHealthPercentages(EntityPlayer player, int baseX, int baseY) {
        int spacing = 20;  // Reduced spacing between text
        int currentX = baseX;

        // Render the health percentages above armor pieces
        for (int i = 0; i < 4; i++) {
            ItemStack armorPiece = player.inventory.armorInventory.get(i);
            if (!armorPiece.isEmpty()) {
                int healthPercentage = getArmorHealthPercentage(armorPiece);
                String healthText = healthPercentage + "%";
                int width = mc.fontRenderer.getStringWidth(healthText);
                // Render the background for readability
                renderTextBackground(currentX - width / 2, baseY, width, 20);
                mc.fontRenderer.drawString(healthText, currentX - width / 2, baseY, 0xFFFFFF);
                currentX += spacing;
            }
        }
    }

    private int getArmorHealthPercentage(ItemStack armorPiece) {
        // Logic to get the health percentage of the armor
        if (armorPiece.getItem() instanceof ItemArmor) {
            int maxDurability = armorPiece.getMaxDamage();
            int currentDurability = maxDurability - armorPiece.getItemDamage();
            return (int) ((currentDurability / (float) maxDurability) * 100);
        }
        return 100; // Default to 100% if no health information available
    }

    private void renderArmorNumbers(EntityPlayer player, int baseX, int baseY) {
        int spacing = 20;  // Reduced spacing between text
        int currentX = baseX;

        // Render armor piece numbers (slot numbers)
        for (int i = 0; i < 4; i++) {
            ItemStack armorPiece = player.inventory.armorInventory.get(i);
            if (!armorPiece.isEmpty()) {
                String armorText = String.valueOf(i + 1); // Slot numbers: 1, 2, 3, 4
                int color = getArmorColor(armorPiece); // Get color for the armor
                int width = mc.fontRenderer.getStringWidth(armorText);
                // Render the background for readability
                renderTextBackground(currentX - width / 2, baseY, width, 20);
                mc.fontRenderer.drawString(armorText, currentX - width / 2, baseY, color);
                currentX += spacing;
            }
        }
    }

    private void renderItemNames(EntityPlayer player, int baseX, int baseY) {
        int spacing = 20;  // Reduced spacing between text
        int currentX = baseX;

        // Render offhand item name
        if (!player.getHeldItemOffhand().isEmpty()) {
            String offhandItemName = player.getHeldItemOffhand().getDisplayName();
            int offhandWidth = mc.fontRenderer.getStringWidth(offhandItemName);
            // Render the background for readability
            renderTextBackground(currentX - offhandWidth / 2, baseY, offhandWidth, 20);
            mc.fontRenderer.drawString(offhandItemName, currentX - offhandWidth / 2, baseY, 0xFFFFFF);
            currentX += offhandWidth + spacing;
        }

        // Render mainhand item name
        if (!player.getHeldItemMainhand().isEmpty()) {
            String mainhandItemName = player.getHeldItemMainhand().getDisplayName();
            int mainhandWidth = mc.fontRenderer.getStringWidth(mainhandItemName);
            // Render the background for readability
            renderTextBackground(currentX - mainhandWidth / 2, baseY, mainhandWidth, 20);
            mc.fontRenderer.drawString(mainhandItemName, currentX - mainhandWidth / 2, baseY, 0xFFFFFF);
            currentX += mainhandWidth + spacing;
        }
    }

    private void renderTextBackground(int x, int y, int width, int height) {
        // Background for text (black rectangle with semi-transparent alpha)
        GlStateManager.color(0f, 0f, 0f, 0.7f); // Semi-transparent black background
        drawRect(x, y, x + width, y + height);
    }

    private void renderNameTag(EntityPlayer player, int baseX, int baseY, float red, float green, float blue, float alpha) {
        String name = player.getDisplayName().getFormattedText();
        int nameWidth = mc.fontRenderer.getStringWidth(name);

        // Background for name tag
        int backgroundWidth = nameWidth + 10; // Add padding
        int backgroundHeight = 20;
        renderTextBackground(baseX - backgroundWidth / 2, baseY - backgroundHeight / 2, backgroundWidth, backgroundHeight);

        // Render name with specified color
        int color = ((int) (alpha * 255) << 24) | ((int) red << 16) | ((int) green << 8) | (int) blue;
        mc.fontRenderer.drawString(name, baseX - nameWidth / 2.0F, baseY, color, false);
    }

    private void drawRect(int left, int top, int right, int bottom) {
        // Drawing the rectangle (black background)
        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(left, bottom, 0).color(0, 0, 0, 0.7f).endVertex(); // Bottom-left
        buffer.pos(right, bottom, 0).color(0, 0, 0, 0.7f).endVertex(); // Bottom-right
        buffer.pos(right, top, 0).color(0, 0, 0, 0.7f).endVertex(); // Top-right
        buffer.pos(left, top, 0).color(0, 0, 0, 0.7f).endVertex(); // Top-left
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private int getArmorColor(ItemStack armorPiece) {
        if (armorPiece.isEmpty()) return 0xFFFFFF; // Default to white if empty

        if (armorPiece.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) armorPiece.getItem();
            switch (armor.getArmorMaterial()) {
                case DIAMOND:
                    return 0xADD8E6; // Light blue for Diamond Armor
                case IRON:
                    return 0x808080; // Gray for Iron Armor
                case GOLD:
                    return 0xFFD700; // Yellow for Gold Armor
                case CHAIN:
                    return 0x4B4B4B; // Dark gray for Chainmail Armor
                case LEATHER:
                    return 0x8B4513; // Brown for Leather Armor
                default:
                    return 0xFFFFFF; // Default color for unknown armor
            }
        }
        return 0xFFFFFF; // Default color for non-armor items
    }

    private float adjustSizeForVisibility(float size, double x, double y, double z) {
        // Get the screen width and height
        float screenWidth = mc.displayWidth;
        float screenHeight = mc.displayHeight;

        // Calculate the 2D screen coordinates of the text (project the 3D coordinates to 2D)
        RenderManager renderManager = mc.getRenderManager();
        double cameraX = renderManager.viewerPosX;
        double cameraY = renderManager.viewerPosY;
        double cameraZ = renderManager.viewerPosZ;

        double dx = x - cameraX;
        double dy = y - cameraY;
        double dz = z - cameraZ;

        // Project the 3D world coordinates to 2D screen space using the player's camera position
        float screenX = (float) (mc.displayWidth / 2 - (dx * size) / (dz + 1)); // Calculate 2D X
        float screenY = (float) (mc.displayHeight / 2 - (dy * size) / (dz + 1)); // Calculate 2D Y

        // Check if the text is off the screen
        boolean isOffScreenX = screenX < 0 || screenX > screenWidth;
        boolean isOffScreenY = screenY < 0 || screenY > screenHeight;

        // If the text is off the screen, reduce the size further
        if (isOffScreenX || isOffScreenY) {
            size *= 0.8F; // Reduce size by 20% if it's off the screen
        }

        // Make the text extremely tiny if the player is closer than 2 blocks
        if (dz < 2) {
            size *= 0.05F; // Reduce size to 5% of the original size if closer than 2 blocks
        }

        // Make the text even tinier if the player is extremely close (less than 1 block)
        if (dz < 1) {
            size *= 0.02F; // Reduce size to 2% of the original size if closer than 1 block
        }

        // For extreme proximity (less than 0.5 blocks), reduce size even further
        if (dz < 0.5) {
            size *= 0.005F; // Reduce size to 0.5% of the original size
        }

        // Ensure that the size doesn't get too small
        return Math.max(size, 0.1F); // Prevent the size from getting smaller than 0.1% of the original size
    }

    private float calculateDynamicSize(float baseSize, float distance) {
        // Dynamically adjust text size based on player distance
        if (distance < 0.5) {
            return baseSize * 0.005F; // Really, really tiny when extremely close (less than 0.5 block)
        } else if (distance < 1) {
            return baseSize * 0.02F; // Extremely tiny when very close (less than 1 block)
        } else if (distance < 2) {
            return baseSize * 0.05F; // Tiny when close (less than 2 blocks)
        } else if (distance < 5) {
            return baseSize * 0.6F; // Slightly reduced size when close (less than 5 blocks)
        } else if (distance < 10) {
            return baseSize * 0.8F; // Slightly reduced size when still somewhat close (less than 10 blocks)
        }
        return baseSize; // No size change for further distances
    }
}
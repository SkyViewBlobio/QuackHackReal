package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class ArmorDisplay extends Module {

    public ArmorDisplay() {
        super("Rustung Zeiger",
      "@Hauptinformation: " +
                "Zeigt dir deine Ruestung in verschiedenen layouts und laesst dich wichtige Informationen wissen. || " +
                "@Optionen:" +
                "- Verticales layout: Zeigt dir die Ruestung in vertikaler Reihenfolge. || " +
                "- Balken und Nummern modes lassen dich die anzeige der rest-Haltbarkeit in nummer-form oder in Balkenformat praesentieren.", false, false, Category.VISUELLES);

        // Add position settings
        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 0, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -500, 500, true));

        // Add layout setting (horizontal/vertical)
        Galacticc.instance.settingsManager.rSetting(new Setting("Verticales Layout", this, false));

        // Add mode settings
        ArrayList<String> durabilityModes = new ArrayList<>();
        durabilityModes.add("Nummern");
        durabilityModes.add("Balken");
        Galacticc.instance.settingsManager.rSetting(new Setting("Durability Mode", this, "Nummern", durabilityModes));

        // Add RGB settings for durability text (Nummern mode)
        Galacticc.instance.settingsManager.rSetting(new Setting("Text Rot", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Text Green", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Text Blau", this, 255, 0, 255, true));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Fetch settings
        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();
        boolean vertical = Galacticc.instance.settingsManager.getSettingByName(this, "Verticales Layout").getValBoolean();
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Durability Mode").getValString();

        // RGB for text (Nummern mode)
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Blau").getValDouble();
        int textColor = (0xFF << 24) | (red << 16) | (green << 8) | blue;

        // Get armor inventory in correct order
        NonNullList<ItemStack> armorInventory = mc.player.inventory.armorInventory;
        ItemStack[] orderedArmor = new ItemStack[]{armorInventory.get(3), armorInventory.get(2), armorInventory.get(1), armorInventory.get(0)};

        // Calculate positions
        int xStart = (screenWidth / 2) + xOffset;
        int yStart = screenHeight - 50 + yOffset;

        for (int i = 0; i < orderedArmor.length; i++) {
            ItemStack armorPiece = orderedArmor[i];
            if (armorPiece == null) continue;

            // Adjust positions for vertical or horizontal layout
            int x = vertical ? xStart : xStart + (i * 20);
            int y = vertical ? yStart + (i * 20) : yStart;

            // Draw armor piece
            drawItemIcon(armorPiece, x, y);

            // Display durability based on mode
            int durability = armorPiece.getMaxDamage() - armorPiece.getItemDamage();

            if (mode.equals("Nummern")) {
                // Scale down the font size further and adjust position
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.7, 0.7, 1.0); // Scale text down to 60% size

                // Adjust x and y for proper alignment after scaling
                int adjustedX = (int) ((x + 1) / 0.7); // Shift further left, adjust for scaling
                int adjustedY = (int) ((y - 5) / 0.7); // Slightly closer to the armor piece

                // Draw the durability number
                mc.fontRenderer.drawStringWithShadow(String.valueOf(durability), adjustedX, adjustedY, textColor);

                GlStateManager.popMatrix();
            } else if (mode.equals("Balken")) {
                // Display durability as a bar
                drawDurabilityBar(armorPiece, x, y + 16);
            }
        }
    }

    private void drawItemIcon(ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableBlend();
        mc.getRenderItem().renderItemIntoGUI(stack, x, y);
        GlStateManager.disableBlend();
    }

    private void drawDurabilityBar(ItemStack stack, int x, int y) {
        int maxDurability = stack.getMaxDamage();
        int currentDurability = maxDurability - stack.getItemDamage();
        float durabilityPercent = (float) currentDurability / maxDurability;

        // Calculate bar width
        int barWidth = (int) (13 * durabilityPercent);

        // Determine color based on durability percentage
        int barColor;
        if (durabilityPercent > 0.5f) {
            barColor = 0xFF00FF00; // Green
        } else if (durabilityPercent > 0.2f) {
            barColor = 0xFFFFFF00; // Yellow
        } else {
            barColor = 0xFFFF0000; // Red
        }

        // Adjusted x-coordinate for horizontal alignment
        int adjustedX = x + 2; // Move the bar 2 pixels to the right

        // Draw background bar (gray)
        Gui.drawRect(adjustedX, y, adjustedX + 13, y + 2, 0xFF808080);

        // Draw durability bar
        Gui.drawRect(adjustedX, y, adjustedX + barWidth, y + 2, barColor);
    }
}
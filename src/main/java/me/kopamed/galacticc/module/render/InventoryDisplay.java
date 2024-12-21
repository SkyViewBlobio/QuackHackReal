package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InventoryDisplay extends Module {
//todo test if works??
    private final Minecraft mc;

    public InventoryDisplay() {
        super("InventarZeiger", "@Hauptinformation: " +
                "Zeigt dir das Inventar auf deinem Bildschirm an.", false, false, Category.VISUELLES);

        this.mc = Minecraft.getMinecraft();

        // Add settings for adjustable coordinates
        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 0, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -500, 500, true));

        // Add RGBA sliders for background color
        Galacticc.instance.settingsManager.rSetting(new Setting("Background Red", this, 40, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Background Green", this, 30, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Background Blue", this, 130, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Background Alpha", this, 150, 0, 255, true));

    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (!this.isToggled()) {
            return;
        }

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableLighting();
        net.minecraft.client.renderer.GlStateManager.enableAlpha();

        try {
            int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
            int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();

            int bgRed = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Background Red").getValDouble();
            int bgGreen = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Background Green").getValDouble();
            int bgBlue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Background Blue").getValDouble();
            int bgAlpha = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Background Alpha").getValDouble();

            int backgroundColor = (bgAlpha << 24) | (bgRed << 16) | (bgGreen << 8) | bgBlue;

            ScaledResolution sr = new ScaledResolution(mc);
            int xPos = sr.getScaledWidth() / 2 + xOffset;
            int yPos = sr.getScaledHeight() / 2 + yOffset;

            // Render background for the inventory display
            int inventoryWidth = 162;
            int inventoryHeight = 72;

            Gui.drawRect(xPos, yPos, xPos + inventoryWidth, yPos + inventoryHeight, backgroundColor);

            // Render inventory items
            for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
                ItemStack itemStack = mc.player.inventory.mainInventory.get(i);
                if (!itemStack.isEmpty()) {
                    int slotX = xPos + (i % 9) * 18;
                    int slotY = yPos + (i / 9) * 18;

                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, slotX, slotY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, slotX, slotY, null);
                }
            }
        } finally {
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            net.minecraft.client.renderer.GlStateManager.enableTexture2D();
            net.minecraft.client.renderer.GlStateManager.enableBlend();
            net.minecraft.client.renderer.GlStateManager.disableAlpha();
            net.minecraft.client.renderer.GlStateManager.disableLighting();
            net.minecraft.client.renderer.GlStateManager.disableDepth();
        }
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

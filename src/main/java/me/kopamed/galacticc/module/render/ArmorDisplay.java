package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
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
        super("Ruestung Zeiger", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Zeigt dir deine Ruestung an und liefert| wichtige Informationen wie Haltbarkeit." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Vertikales Layout: " + ChatFormatting.WHITE + "Zeigt die Ruestung in vertikaler Reihenfolge an." + ChatFormatting.RED +
                        "- Haltbarkeitsanzeige: " + ChatFormatting.WHITE + "Wechselt zwischen Nummern| und Balken zur Darstellung der Haltbarkeit." + ChatFormatting.RED +
                        "- Nummern: " + ChatFormatting.WHITE + "Zeigt die Haltbarkeit in Nummer-form." + ChatFormatting.RED +
                        "- Balken: " + ChatFormatting.WHITE + "Zeigt dir die Haltbarkeit in Balken an sollte| deine Ruestung unter 99% Haltbarkeit liegen."
                        ,
                false, false, Category.VISUELLES);

        Galacticc.instance.settingsManager.rSetting(new Setting("X Offset", this, 11, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, -11, -500, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Verticales Layout", this, false));

        ArrayList<String> durabilityModes = new ArrayList<>();
        durabilityModes.add("Nummern");
        durabilityModes.add("Balken");
        Galacticc.instance.settingsManager.rSetting(new Setting("Durability Mode", this, "Nummern", durabilityModes));
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

        int xOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "X Offset").getValDouble();
        int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();
        boolean vertical = Galacticc.instance.settingsManager.getSettingByName(this, "Verticales Layout").getValBoolean();
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Durability Mode").getValString();

        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Rot").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Green").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Text Blau").getValDouble();
        int textColor = (0xFF << 24) | (red << 16) | (green << 8) | blue;

        NonNullList<ItemStack> armorInventory = mc.player.inventory.armorInventory;
        ItemStack[] orderedArmor = new ItemStack[]{armorInventory.get(3), armorInventory.get(2), armorInventory.get(1), armorInventory.get(0)};

        int xStart = (screenWidth / 2) + xOffset;
        int yStart = screenHeight - 50 + yOffset;

        for (int i = 0; i < orderedArmor.length; i++) {
            ItemStack armorPiece = orderedArmor[i];
            if (armorPiece.isEmpty()) continue;

            int x = vertical ? xStart : xStart + (i * 20);
            int y = vertical ? yStart + (i * 20) : yStart;

            drawItemIcon(armorPiece, x, y);

            int durability = armorPiece.getMaxDamage() - armorPiece.getItemDamage();

            if (mode.equals("Nummern")) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.7, 0.7, 1.0);

                int adjustedX = (int) ((x + 1) / 0.7);
                int adjustedY = (int) ((y - 5) / 0.7);

                mc.fontRenderer.drawStringWithShadow(String.valueOf(durability), adjustedX, adjustedY, textColor);

                GlStateManager.popMatrix();
            } else if (mode.equals("Balken")) {
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

        if (currentDurability == maxDurability) {
            return;
        }

        float durabilityPercent = (float) currentDurability / maxDurability;

        int barWidth = (int) (13 * durabilityPercent);

        int barColor;
        if (durabilityPercent > 0.5f) {
            barColor = 0xFF00FF00;
        } else if (durabilityPercent > 0.2f) {
            barColor = 0xFFFFFF00;
        } else {
            barColor = 0xFFFF0000;
        }

        int adjustedX = x + 2;

        Gui.drawRect(adjustedX, y, adjustedX + 13, y + 2, 0xFF808080);

        Gui.drawRect(adjustedX, y, adjustedX + barWidth, y + 2, barColor);
    }
}
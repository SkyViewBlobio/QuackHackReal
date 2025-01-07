package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class ShulkerPreview extends Module {
    public ShulkerPreview() {
        super("ShulkerPreview", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Zeigt dir den Inhalt einer Shulker-Box an,| ohne sie zu oeffnen, indem du mit der| Maus darueber schwebst.",
                false, false, Category.VISUELLES);
    }

    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemShulkerBox)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BlockEntityTag")) {
            return;
        }

        NBTTagCompound blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        NBTTagList items = blockEntityTag.getTagList("Items", 10);

        int adjustedX = event.getX() + 120;
        int adjustedY = event.getY();

        renderShulkerPreview(adjustedX, adjustedY, items);
    }

    private void renderShulkerPreview(int x, int y, NBTTagList items) {
        int rows = 3;
        int columns = 9;
        int slotSize = 18;
        int width = columns * slotSize;
        int height = rows * slotSize;

        Gui.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 150).getRGB());

        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            ItemStack itemStack = new ItemStack(itemTag);

            int slotX = x + (i % columns) * slotSize;
            int slotY = y + (i / columns) * slotSize;

            renderItemStack(itemStack, slotX, slotY);
        }
    }

        private void renderItemStack (ItemStack stack,int x, int y){
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();

            itemRender.renderItemAndEffectIntoGUI(stack, x, y);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, x, y, null);

            GlStateManager.disableBlend();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }
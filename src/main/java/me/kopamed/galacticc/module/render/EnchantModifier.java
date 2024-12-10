package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class EnchantModifier extends Module {

    private RenderItem renderItem;

    public EnchantModifier() {
        super("Enchant Color", "Customize enchantment glint color", false, false, Category.VISUELLES);

        // Add RGB settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Red Value", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Green Value", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Blue Value", this, 255, 0, 255, true));
    }

    @Override
    public void onEnabled() {
        super.onEnabled();

        // Initialize RenderItem with ModelManager
        Minecraft mc = Minecraft.getMinecraft();
        ModelManager modelManager = mc.getRenderItem().getItemModelMesher().getModelManager();
        this.renderItem = new RenderItem(mc.renderEngine, modelManager);

        // Register the event handler
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();

        // Unregister the event handler
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        // Get current RGB values from settings
        int red = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Red Value").getValDouble();
        int green = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Green Value").getValDouble();
        int blue = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Blue Value").getValDouble();

        Minecraft mc = Minecraft.getMinecraft();

        // Render enchantments with custom glint color
        for (ItemStack itemStack : mc.thePlayer.inventory.mainInventory) {
            if (itemStack != null && itemStack.isItemEnchanted()) {
                applyEnchantColor(itemStack, red, green, blue);
            }
        }
    }

    // Apply custom enchant glint color
    private void applyEnchantColor(ItemStack stack, int red, int green, int blue) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, 1.0F); // Apply custom RGB
        this.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0); // Render item with effects
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }
}

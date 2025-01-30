package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber
public class ZombiePortalShader extends Module {

    public ZombiePortalShader() {
        super("ZombiePortalShader", "Applies the End Portal shader effect to zombies", false, false, Category.VISUELLES);
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        // Check if the entity being rendered is a Zombie
        if (event.getEntity() instanceof EntityZombie) {
            GlStateManager.pushMatrix();

            // Translate to the zombie's position
            double x = event.getX();
            double y = event.getY();
            double z = event.getZ();
            GlStateManager.translate(x, y, z);

            // Rotate to match the zombie's orientation
            float yaw = interpolateRotation(event.getEntity().prevRotationYaw, event.getEntity().rotationYaw, event.getPartialRenderTick());
            GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F); // Negative yaw ensures correct facing direction

            // Correct the height alignment (start at the feet)
            GlStateManager.translate(0.0F, 0.0F, 0.0F);

            // Set up OpenGL states for rendering
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();

            // Bind the End Portal texture
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/entity/end_portal.png"));

            // Render the overlay on the zombie
            renderOverlay(event);

            // Restore OpenGL states
            GlStateManager.enableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();

            GlStateManager.popMatrix();
        }
    }

    private void renderOverlay(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        // Get the renderer for the entity
        Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(event.getEntity());

        // Ensure the renderer is a RenderLivingBase instance
        if (renderer instanceof RenderLivingBase<?>) {
            RenderLivingBase<?> livingRenderer = (RenderLivingBase<?>) renderer;

            // Render the zombie model with the End Portal texture
            GlStateManager.pushMatrix();

            // Scale down slightly to fit the overlay better
            GlStateManager.scale(-0.95F, -0.95F, -0.95F); // Reduce size by 5%

            // Properly flip the texture upright
            GlStateManager.scale(1.0F, -1.0F, 1.0F); // Correct Y-axis orientation
            GlStateManager.translate(0.0F, -event.getEntity().height, 0.0F); // Start at the feet

            // Set transparency for the overlay
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F); // Semi-transparent

            // Render the zombie's model using the overlay texture
            livingRenderer.getMainModel().render(
                    event.getEntity(),
                    event.getEntity().limbSwing,
                    event.getEntity().limbSwingAmount,
                    event.getEntity().ticksExisted + event.getPartialRenderTick(),
                    event.getEntity().rotationYawHead,
                    event.getEntity().rotationPitch,
                    0.0625F // Scale factor
            );

            GlStateManager.popMatrix();
        }
    }


    /**
     * Interpolates between two rotation angles to ensure smooth transitions.
     */
    private float interpolateRotation(float prevYaw, float yaw, float partialTicks) {
        float delta = yaw - prevYaw;
        while (delta < -180.0F) delta += 360.0F;
        while (delta >= 180.0F) delta -= 360.0F;
        return prevYaw + partialTicks * delta;
    }


}
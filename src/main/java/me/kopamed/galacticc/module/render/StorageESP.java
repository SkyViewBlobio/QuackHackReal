package me.kopamed.galacticc.module.render;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//todo add gradient aaaaaaaaaaaa and maybe find other way than fov
public class StorageESP extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<BlockPos, Float> blockFadeMap = new HashMap<>();
    private final Map<Entity, MinecartFadeState> minecartFadeMap = new HashMap<>();

    public StorageESP() {
        super("StorageESP", "@HauptInformation: " +
                "Zeigt dir Truhen und andere Container an. " +
                "@Optionen: " +
                "- NormalChest (Normale Truhe). || " +
                "- EnderChest (Eine Endertruhe wo du dinge rein tun kannst und nur du kannst diese dinge beim oeffnen der Truhe sehen. || " +
                "- TrappedChest (StromKiste: StromKisten (Rotsteintruhen) - sind Truhen, die oft als Falle genutzt werden. Meist aktivieren diese Truhen eine Explosionsfalle. || " +
                "- Furnace (Ofen). || " +
                "- Dispenser (Minecraft Spender: Eine art Container in der du dinge aufbewaren oder sogar automatische Schiessanlagen erstellen kannst. Sie würden aber auch wenn mit Strom verlinkt Dinge ausspucken wenn sie befuellt sind. || " +
                "- Minecraft(Chest/Furnace) [Truhen oder Oefen: sind einfach in einer Gondel platziert und koennen somit also Dinge transportieren. || " +
                "- Duration (die Dauer des Verblassungseffekts). || " +
                "- Range (Reichweite in Bloecken, wie weit du etwas angezeigt haben willst).", false, false, Category.VISUELLES);

        // Toggles for each storage unit
        Galacticc.instance.settingsManager.rSetting(new Setting("NormalChest", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EnderChest", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("TrappedChest", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Furnace", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Dispenser", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("MinecartChest", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("MinecartFurnace", this, true));

        // Normal Chest RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("ChestRed", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("ChestGreen", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("ChestBlue", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("ChestAlpha", this, 0.3F, 0.0F, 1.0F, false));

        // Ender Chest RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("EnderRed", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EnderGreen", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EnderBlue", this, 255, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EnderAlpha", this, 0.3F, 0.0F, 1.0F, false));

        // Trapped Chest RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("TrappedRed", this, 255, 255, 0, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("TrappedGreen", this, 255, 255, 0, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("TrappedBlue", this, 0, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("TrappedAlpha", this, 0.3F, 0.0F, 1.0F, false));

        // Furnace RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("FurnaceRed", this, 128, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("FurnaceGreen", this, 128, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("FurnaceBlue", this, 128, 0, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("FurnaceAlpha", this, 0.3F, 0.0F, 1.0F, false));

        // Dispenser RGBA sliders
        Galacticc.instance.settingsManager.rSetting(new Setting("DispenserRed", this, 255, 0, 128, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("DispenserGreen", this, 128, 255, 128, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("DispenserBlue", this, 128, 128, 255, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("DispenserAlpha", this, 0.3F, 0.0F, 1.0F, false));

        // Adding sliders for Fade Speed, RGBA values, and Range
        Galacticc.instance.settingsManager.rSetting(new Setting("Duration", this, 5.0F, 1.0F, 40.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 10.0F, 3.0F, 15.0F, false));
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        float duration = getSettingValue("Duration", 1.0f);
        float range = getSettingValue("Range", 10.0f);
        float fadeDecrement = 1.0F / (duration * 40.0F);

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        Vec3 playerLookVec = mc.thePlayer.getLookVec().normalize();

        // Compute threshold for FOV cone dynamically
        double fov = mc.gameSettings.fovSetting;
        double halfFovRadians = Math.toRadians(fov / 2.0);
        double threshold = Math.cos(halfFovRadians);

        for (BlockPos blockPos : BlockPos.getAllInBox(
                new BlockPos(playerPos.xCoord - range, playerPos.yCoord - range, playerPos.zCoord - range),
                new BlockPos(playerPos.xCoord + range, playerPos.yCoord + range, playerPos.zCoord + range))) {

            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            if (blockState == null) continue;

            Block block = blockState.getBlock();
            if (block == null || !isStorageBlock(block, blockPos)) continue;

            // Check if block is within the player's view
            Vec3 blockVec = new Vec3(blockPos.getX() - mc.thePlayer.posX,
                    blockPos.getY() - mc.thePlayer.posY,
                    blockPos.getZ() - mc.thePlayer.posZ).normalize();

            if (playerLookVec.dotProduct(blockVec) < threshold) continue; // Skip blocks outside FOV

            if (playerPos.distanceTo(new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5)) <= range) {
                blockFadeMap.put(blockPos, 1.0f);
            }
        }

        // Fade logic for blocks
        Iterator<Map.Entry<BlockPos, Float>> blockIterator = blockFadeMap.entrySet().iterator();
        while (blockIterator.hasNext()) {
            Map.Entry<BlockPos, Float> entry = blockIterator.next();
            BlockPos blockPos = entry.getKey();
            float currentAlpha = entry.getValue();

            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            if (blockState == null) {
                blockIterator.remove();
                continue;
            }

            Block block = blockState.getBlock();
            if (block == null) {
                blockIterator.remove();
                continue;
            }

            if (block instanceof BlockChest) {
                boolean isTrapped = block == Blocks.trapped_chest;
                if (isTrapped && getSettingBoolean("TrappedChest")) {
                    renderBlockFade(blockPos, currentAlpha,
                            getColorValue("TrappedRed"),
                            getColorValue("TrappedGreen"),
                            getColorValue("TrappedBlue"),
                            getAlphaValue("TrappedAlpha"));
                } else if (!isTrapped && getSettingBoolean("NormalChest")) {
                    renderBlockFade(blockPos, currentAlpha,
                            getColorValue("ChestRed"),
                            getColorValue("ChestGreen"),
                            getColorValue("ChestBlue"),
                            getAlphaValue("ChestAlpha"));
                }
            } else if (block instanceof BlockEnderChest) {
                renderBlockFade(blockPos, currentAlpha,
                        getColorValue("EnderRed"),
                        getColorValue("EnderGreen"),
                        getColorValue("EnderBlue"),
                        getAlphaValue("EnderAlpha"));
            } else if (block instanceof BlockFurnace) {
                renderBlockFade(blockPos, currentAlpha,
                        getColorValue("FurnaceRed"),
                        getColorValue("FurnaceGreen"),
                        getColorValue("FurnaceBlue"),
                        getAlphaValue("FurnaceAlpha"));
            } else if (block instanceof BlockDispenser) {
                renderBlockFade(blockPos, currentAlpha,
                        getColorValue("DispenserRed"),
                        getColorValue("DispenserGreen"),
                        getColorValue("DispenserBlue"),
                        getAlphaValue("DispenserAlpha"));
            }

            currentAlpha -= fadeDecrement;
            if (currentAlpha <= 0) {
                blockIterator.remove();
            } else {
                entry.setValue(currentAlpha);
            }
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityMinecartChest || entity instanceof EntityMinecartFurnace)) continue;

            Vec3 entityPos = new Vec3(entity.posX, entity.posY + entity.height / 2.0, entity.posZ);
            Vec3 toEntityVec = entityPos.subtract(playerPos).normalize();

            // FOV check: dot product to determine if entity is in the player's view
            double dotProduct = playerLookVec.dotProduct(toEntityVec);
            if (dotProduct < threshold) continue;

            if (playerPos.distanceTo(entityPos) > range) continue;

            if (!minecartFadeMap.containsKey(entity)) {
                minecartFadeMap.put(entity, new MinecartFadeState(1.0f, false));
            } else {
                MinecartFadeState state = minecartFadeMap.get(entity);
                state.alpha = 1.0f;
                state.isFading = false;
            }
        }

        Iterator<Map.Entry<Entity, MinecartFadeState>> minecartIterator = minecartFadeMap.entrySet().iterator();
        while (minecartIterator.hasNext()) {
            Map.Entry<Entity, MinecartFadeState> entry = minecartIterator.next();
            Entity entity = entry.getKey();
            MinecartFadeState state = entry.getValue();

            if (!entity.isEntityAlive()) {
                minecartIterator.remove();
                continue;
            }

            Vec3 entityPos = new Vec3(entity.posX, entity.posY + entity.height / 2.0, entity.posZ);
            Vec3 toEntityVec = entityPos.subtract(playerPos).normalize();
            double dotProduct = playerLookVec.dotProduct(toEntityVec);

            if (playerPos.distanceTo(entityPos) > range || dotProduct < threshold) {
                state.isFading = true;
            } else {
                state.isFading = false;
                state.alpha = 1.0f;
            }

            if (state.isFading) {
                state.alpha -= fadeDecrement;
                if (state.alpha <= 0) {
                    state.alpha = 0;
                    minecartIterator.remove();
                    continue;
                }
            }

            if (entity instanceof EntityMinecartChest && getSettingBoolean("MinecartChest")) {
                renderMinecartStorage(entity,
                        getColorValue("ChestRed"),
                        getColorValue("ChestGreen"),
                        getColorValue("ChestBlue"),
                        getAlphaValue("ChestAlpha") * state.alpha);
            } else if (entity instanceof EntityMinecartFurnace && getSettingBoolean("MinecartFurnace")) {
                renderMinecartStorage(entity,
                        getColorValue("FurnaceRed"),
                        getColorValue("FurnaceGreen"),
                        getColorValue("FurnaceBlue"),
                        getAlphaValue("FurnaceAlpha") * state.alpha);
            }
        }
    }

    private static class MinecartFadeState {
        float alpha;
        boolean isFading;

        public MinecartFadeState(float alpha, boolean isFading) {
            this.alpha = alpha;
            this.isFading = isFading;
        }
    }

    private boolean isStorageBlock(Block block, BlockPos pos) {
        if (block instanceof BlockChest) {
            return Galacticc.instance.settingsManager.getSettingByName
                    (this, "NormalChest").getValBoolean()
                    || Galacticc.instance.settingsManager.getSettingByName
                    (this, "TrappedChest").getValBoolean();
        } else if (block instanceof BlockEnderChest) {
            return Galacticc.instance.settingsManager.getSettingByName
                    (this, "EnderChest").getValBoolean();
        } else if (block instanceof BlockFurnace) {
            return Galacticc.instance.settingsManager.getSettingByName
                    (this, "Furnace").getValBoolean();
        } else if (block instanceof BlockDispenser) {
            return Galacticc.instance.settingsManager.getSettingByName
                    (this, "Dispenser").getValBoolean();
        }
        return false;
    }

    private float getSettingValue(String name, float defaultValue) {
        try {
            Setting setting = Galacticc.instance.settingsManager.getSettingByName(this, name);
            return (setting != null) ? (float) setting.getValDouble() : defaultValue;
        } catch (Exception e) {
            System.out.println("Error fetching setting: " + name);
            return defaultValue;
        }
    }

    private boolean getSettingBoolean(String name) {
        try {
            Setting setting = Galacticc.instance.settingsManager.getSettingByName(this, name);
            return (setting != null) && setting.getValBoolean();
        } catch (Exception e) {
            System.out.println("Error fetching boolean setting: " + name);
            return false;
        }
    }

    private int getColorValue(String name) {
        return (int) Galacticc.instance.settingsManager.getSettingByName(this, name).getValDouble();
    }

    private float getAlphaValue(String name) {
        return (float) Galacticc.instance.settingsManager.getSettingByName(this, name).getValDouble();
    }

    private void renderMinecartStorage
            (Entity entity, int red, int green, int blue, float alpha) {
        AxisAlignedBB box = entity.getEntityBoundingBox()
                .offset(-mc.getRenderManager().viewerPosX,
                        -mc.getRenderManager().viewerPosY,
                        -mc.getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha);
        drawFilledBox(box);
        drawOutline(box, red / 255.0f, green / 255.0f, blue / 255.0f, alpha);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderBlockFade
            (BlockPos blockPos, float fadeAlpha, int red, int green, int blue, float alphaMultiplier) {
        AxisAlignedBB boundingBox = mc.theWorld.getBlockState(blockPos).getBlock()
                .getSelectedBoundingBox(mc.theWorld, blockPos)
                .offset(-mc.getRenderManager().viewerPosX,
                        -mc.getRenderManager().viewerPosY,
                        -mc.getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(red
                / 255.0f, green
                / 255.0f, blue
                / 255.0f, fadeAlpha * alphaMultiplier);
        drawFilledBox(boundingBox);

        drawOutline(boundingBox, red
                / 255.0f, green
                / 255.0f, blue
                / 255.0f, fadeAlpha * alphaMultiplier);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawOutline(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_LINES);

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

        GL11.glEnd();
    }

    private void drawFilledBox(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);

        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);

        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);

        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);

        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

        GL11.glEnd();
    }
}

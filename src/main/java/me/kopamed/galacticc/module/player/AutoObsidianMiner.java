package me.kopamed.galacticc.module.player;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AutoObsidianMiner extends Module {
    private boolean isMining = false;
    private BlockPos targetBlockPos = null;
    private long lastMiningTime = 0;
    private long miningDuration = 0;
    private final Map<BlockPos, Float> fadeBlocks = new HashMap<>();

    public AutoObsidianMiner() {
        super("AutoObsidianMiner", "Mines Obsidian blocks automatically when left-clicked.", false, false, Category.SPIELER);

        // Register modes
        ArrayList<String> miningModes = new ArrayList<>();
        miningModes.add("Vanilla");
        miningModes.add("Damage");
        miningModes.add("Packet");
        miningModes.add("SpeedMine");

        Galacticc.instance.settingsManager.rSetting(new Setting("Mode", this, "Vanilla", miningModes));
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (mc.player == null || mc.world == null) return;

        Block block = mc.world.getBlockState(event.getPos()).getBlock();

        if (block != Blocks.OBSIDIAN) return;

        targetBlockPos = event.getPos();
        IBlockState blockState = mc.world.getBlockState(targetBlockPos);
        float blockHardness = getBlockStrength(blockState, targetBlockPos);

        miningDuration = getMiningDuration(blockHardness);
        lastMiningTime = System.currentTimeMillis();

        int pickaxeSlot = findPickaxeInHotbar();
        if (pickaxeSlot != -1) {
            silentSwitchToPickaxe(pickaxeSlot);
        }

        fadeBlocks.put(targetBlockPos, 0.4f);

        isMining = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Handle mining logic
        if (isMining) {
            String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
            long elapsedTime = System.currentTimeMillis() - lastMiningTime;

            switch (mode) {
                case "Damage":
                    handleDamageMining(elapsedTime);
                    break;
                case "Packet":
                    handlePacketMining(elapsedTime);
                    break;
                case "SpeedMine":
                    handleSpeedMining(elapsedTime);
                    break;
                default:
                    handleVanillaMining(elapsedTime);
                    break;
            }
        }

        fadeBlocks.entrySet().removeIf(entry -> {
            float alpha = entry.getValue() - 0.01f;
            if (alpha <= 0) return true;
            entry.setValue(alpha);
            return false;
        });
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Render fading blocks
        for (Map.Entry<BlockPos, Float> entry : fadeBlocks.entrySet()) {
            BlockPos blockPos = entry.getKey();
            float alpha = entry.getValue();
            renderBlockFade(blockPos, alpha);
        }
    }

    private void renderBlockFade(BlockPos blockPos, float fadeAlpha) {
        IBlockState blockState = mc.world.getBlockState(blockPos);
        AxisAlignedBB boundingBox = blockState.getBoundingBox(mc.world, blockPos)
                .offset(blockPos)
                .offset(-mc.getRenderManager().viewerPosX,
                        -mc.getRenderManager().viewerPosY,
                        -mc.getRenderManager().viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Render green color with fading alpha
        GlStateManager.color(0.0f, 1.0f, 0.0f, fadeAlpha);
        drawFilledBox(boundingBox);

        drawOutline(boundingBox, fadeAlpha);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawOutline(AxisAlignedBB box, float alpha) {
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0.0f, 1.0f, 0.0f, alpha);
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


    private void handleVanillaMining(long elapsedTime) {
        if (elapsedTime >= miningDuration) {
            sendMiningPacket(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, targetBlockPos);
            isMining = false;
        } else {
            sendMiningPacket(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, targetBlockPos);
        }
    }

    // Damage-based mining mode
    private void handleDamageMining(long elapsedTime) {
        if (elapsedTime >= miningDuration / 2) {
            sendMiningPacket(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, targetBlockPos);
            isMining = false;
        } else {
            sendMiningPacket(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, targetBlockPos);
        }
    }

    // Packet-based mining mode
    private void handlePacketMining(long elapsedTime) {
        if (elapsedTime >= miningDuration) {
            sendMiningPacket(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, targetBlockPos);
            isMining = false;
        } else {
            sendMiningPacket(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, targetBlockPos);
        }
    }

    // SpeedMine mode
    private void handleSpeedMining(long elapsedTime) {
        if (elapsedTime >= miningDuration / 3) {
            sendMiningPacket(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, targetBlockPos);
            isMining = false;
        } else {
            sendMiningPacket(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, targetBlockPos);
        }
    }

    // Find a pickaxe in the hotbar
    private int findPickaxeInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.player.inventory.getStackInSlot(i);
            if (item.getItem() instanceof ItemPickaxe) {
                return i;
            }
        }
        return -1;
    }

    // Silent switch to pickaxe
    private void silentSwitchToPickaxe(int pickaxeSlot) {
        if (pickaxeSlot == -1) return;
        mc.player.connection.sendPacket(new CPacketHeldItemChange(pickaxeSlot));
        mc.playerController.updateController();
    }

    // Send mining packet to simulate block breaking
    private void sendMiningPacket(CPacketPlayerDigging.Action action, BlockPos pos) {
        CPacketPlayerDigging packet = new CPacketPlayerDigging(action, pos, mc.player.getHorizontalFacing());
        mc.player.connection.sendPacket(packet);
    }
    // Get block strength (hardness)
    public float getBlockStrength(IBlockState state, BlockPos position) {
        float hardness = state.getBlockHardness(mc.world, position);

        if (hardness < 0) {
            return 0;
        }

        ItemStack currentTool = mc.player.getHeldItemMainhand();
        float effectiveStrength = hardness;
        int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, currentTool);
        if (efficiencyLevel > 0) {
            effectiveStrength *= (float) (1 + (efficiencyLevel * 0.1)); // Explicit cast to float
        }

        return effectiveStrength;
    }

        // Calculate mining duration
    private long getMiningDuration(float hardness) {
        return (long) (1000 * (1 / hardness)); // Adjust as needed
    }

    @Override
    public String getHUDInfo() {
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
        return TextFormatting.GRAY + "[Mode: " + TextFormatting.GRAY + mode + TextFormatting.GRAY + "]";
    }
}

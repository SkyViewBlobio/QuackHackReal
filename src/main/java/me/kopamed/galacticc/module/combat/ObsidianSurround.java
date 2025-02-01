package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
//todo add extend and auto disable
public class ObsidianSurround extends Module {

    public ObsidianSurround() {
        super("ObsidianSurround", "Places obsidian around your feet", false, false, Category.ANGRIFF);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;
        int obsidianSlot = findObsidianInHotbar();
        if (obsidianSlot == -1) return;

        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        // Get adjacent positions on the same level (horizontal neighbors)
        BlockPos[] neighbors = new BlockPos[]{
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west()
        };

        // Save original slot and switch to obsidian slot
        int originalSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = obsidianSlot;

        for (BlockPos pos : neighbors) {
            if (canPlace(pos)) {
                // Calculate hit vector: target center of the face of the block one below the placement
                Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                // Place obsidian by right-clicking on the block below the target position
                // (so that placement is allowed)
                mc.playerController.processRightClickBlock(mc.player, mc.world, pos.down(), EnumFacing.UP, hitVec, EnumHand.MAIN_HAND);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }

        // Restore original slot
        mc.player.inventory.currentItem = originalSlot;
    }

    // Searches for obsidian in the hotbar (slots 0-8). Returns -1 if not found.
    private int findObsidianInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                ItemBlock blockItem = (ItemBlock) stack.getItem();
                if (blockItem.getBlock() == Blocks.OBSIDIAN) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Determines whether a block can be placed at the given position.
    private boolean canPlace(BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);
        // Check if the position is air or replaceable
        return state.getMaterial().isReplaceable();
    }
}
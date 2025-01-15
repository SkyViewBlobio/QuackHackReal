package me.kopamed.galacticc.module.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
//todo, sneak client and server side to avoid fucking oipening chestsgradle
public class AutomaticBridgeBuilder extends Module {
    public AutomaticBridgeBuilder() {
        super("AutomaticBridgeBuilder", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Platziert automatisch Blocks, um eine Bruecke zu bauen oder in die Hoehe zu bauen." + ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Benutzt diese Funktion, um sicher ueber Luecken oder zu hohen Hoehen zu bauen.",
                false, false, Category.SPIELER);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) return;

        int originalSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = blockSlot;

        if (isFalling()) {
            placeBlockBelow();
        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = 0.35;

            placeBlockBelow();
        }

        mc.player.inventory.currentItem = originalSlot;
    }

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFalling() {
        double yVelocity = mc.player.motionY;
        return yVelocity < 0 && !isSolidBlockBelow();
    }

    /**
     * Checks if there is a solid block directly below the player.
     *
     * @return True if there is a solid block directly below.
     */
    private boolean isSolidBlockBelow() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        return mc.world.getBlockState(pos).getMaterial().isSolid();
    }

    private void placeBlockBelow() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);

        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return;
        }

        if (isContainerBlockBelow()) {
            return;
        }

        Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        mc.playerController.processRightClickBlock(
                mc.player, mc.world, pos.down(), EnumFacing.UP, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    private boolean isContainerBlockBelow() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        Block blockBelow = mc.world.getBlockState(pos).getBlock();
        return blockBelow instanceof BlockContainer;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
    }
}

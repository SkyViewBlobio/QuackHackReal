package me.kopamed.galacticc.module.player;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class AutoObsidianMiner extends Module {
    private boolean isMining = false;
    private BlockPos targetBlockPos = null;
    private long lastMiningTime = 0;
    private long miningDuration = 0;
    private float blockHardness = 0;

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

        // Only mine if the block is Obsidian
        if (block != Blocks.OBSIDIAN) return;

        targetBlockPos = event.getPos();
        IBlockState blockState = mc.world.getBlockState(targetBlockPos);
        blockHardness = getBlockStrength(blockState, targetBlockPos);

        // Calculate mining duration based on block hardness
        miningDuration = getMiningDuration(blockHardness);
        lastMiningTime = System.currentTimeMillis();

        // Silent switch to pickaxe
        int pickaxeSlot = findPickaxeInHotbar();
        if (pickaxeSlot != -1) {
            silentSwitchToPickaxe(pickaxeSlot);
        }

        isMining = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || !isMining) return;

        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
        long elapsedTime = System.currentTimeMillis() - lastMiningTime;

        // Delegate behavior based on mode
        switch (mode) {
            case "Vanilla":
                handleVanillaMining(elapsedTime);
                break;
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
                // Default fallback mode
                handleVanillaMining(elapsedTime);
                break;
        }
    }

    // Vanilla mining mode
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
            effectiveStrength *= (1 + (efficiencyLevel * 0.1));
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
        return TextFormatting.GRAY + "[Mode: " + TextFormatting.GREEN + mode + TextFormatting.GRAY + "]";
    }
}

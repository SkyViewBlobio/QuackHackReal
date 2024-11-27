package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class BlockHighlight extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public BlockHighlight() {
        super("BlockInfo", "Shows valid information of the block you're looking at", false, false, Category.VISUELLES);

        // Register settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Block-Typ", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Block Coordinaten", this, true));
    }

    @Override
    public String getHUDInfo() {
        // Fetch settings
        boolean showBlockType = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block-Typ").getValBoolean();
        boolean showBlockCoordinates = Galacticc.instance.settingsManager
                .getSettingByName(this, "Block Coordinaten").getValBoolean();

        // Perform a ray trace to determine the block being hovered over
        MovingObjectPosition rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return ChatFormatting.GRAY + "[N/B]";
        }

        // Get block position and block type
        BlockPos blockPos = rayTraceResult.getBlockPos();
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        // Extract block name and strip the "minecraft:" prefix if present
        String fullBlockName = Block.blockRegistry.getNameForObject(block).toString();
        String blockName = fullBlockName.contains(":") ? fullBlockName.split(":")[1] : fullBlockName;

        // Build HUD info based on settings
        StringBuilder hudInfo = new StringBuilder(ChatFormatting.GRAY + "[B/I: ");

        if (showBlockType) {
            hudInfo.append("T: ").append(ChatFormatting.GRAY).append(blockName).append(ChatFormatting.GRAY);
        }

        if (showBlockCoordinates) {
            if (showBlockType) {
                hudInfo.append(", ");
            }
            hudInfo.append("C: ").append(ChatFormatting.GRAY)
                    .append(blockPos.getX()).append(", ")
                    .append(blockPos.getY()).append(", ")
                    .append(blockPos.getZ()).append(ChatFormatting.GRAY);
        }

        hudInfo.append("]");
        return hudInfo.toString();
    }
}


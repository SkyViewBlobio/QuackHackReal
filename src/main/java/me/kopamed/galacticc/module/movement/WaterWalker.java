package me.kopamed.galacticc.module.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WaterWalker extends Module {

    public WaterWalker() {
        super("WaterWalker", "" +
                ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                "Laesst dich oeber Wasser/Lava laufen." + ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                "Von sehr weit oben in Lava zu springen, gibt dir Fallschaden." + "Von sehr weit oben in Wasser zu springen, sollte dir| keinen Fallschaden geben.",
                false , false, Category.BEWEGUNG);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        BlockPos posBelow = new BlockPos(mc.player.posX, mc.player.posY - 0.1, mc.player.posZ);
        if (mc.world.getBlockState(posBelow).getBlock() == Blocks.WATER || mc.world.getBlockState(posBelow).getBlock() == Blocks.LAVA) {
            if (mc.player.motionY < 0) {
                mc.player.motionY = 0;
                mc.player.onGround = true;
            }
        }
    }
}

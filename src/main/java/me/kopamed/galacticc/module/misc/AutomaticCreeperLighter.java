package me.kopamed.galacticc.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class AutomaticCreeperLighter extends Module {
//todo: make it so it doesn't retry on already lit creepers. the flint and steel breaks way to fast
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<Integer, Long> litCreepers = new HashMap<>();
    private static final long IGNITE_TIMEOUT_MS = 1000;

    public AutomaticCreeperLighter() {
        super("AutomaticCreeperLighter","" +
                ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                "Zuendet Creeper mit einem Feuerzeug an.", false, false, Category.SONSTIGES);
    }


    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemFlintAndSteel) {
            mc.world.getEntitiesWithinAABB(EntityCreeper.class, new AxisAlignedBB(
                            mc.player.posX - 6, mc.player.posY - 6, mc.player.posZ - 6,
                            mc.player.posX + 6, mc.player.posY + 6, mc.player.posZ + 6))
                    .forEach(creeper -> {
                        long currentTime = System.currentTimeMillis();
                        int creeperId = creeper.getEntityId();

                        // Check if the creeper is burning or already recently ignited
                        if (!creeper.isBurning() && (!litCreepers.containsKey(creeperId) ||
                                currentTime - litCreepers.get(creeperId) >= IGNITE_TIMEOUT_MS)) {
                            lightCreeper(creeper);
                            litCreepers.put(creeperId, currentTime);
                        }
                    });
        }
    }

    private void lightCreeper(EntityCreeper creeper) {
        mc.playerController.interactWithEntity(mc.player, creeper, EnumHand.MAIN_HAND);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        litCreepers.clear();
    }
}
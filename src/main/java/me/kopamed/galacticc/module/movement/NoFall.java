package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoFall extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public NoFall() {
        super("NoFall", "@Hauptinformation: " +
                        "Verhindert Fallschaden beim Herunterfallen oder Springen.",
                true, false, Category.BEWEGUNG);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player != null && !mc.player.capabilities.isFlying) {
            if (mc.player.fallDistance > 2.0F) {
                mc.player.connection.sendPacket(new CPacketPlayer(true));
            }
        }
    }

    @SubscribeEvent
    public void onFallDamage(LivingFallEvent event) {
        if (event.getEntityLiving() == mc.player) {
            event.setCanceled(true); // Cancel the fall damage
        }
    }
}
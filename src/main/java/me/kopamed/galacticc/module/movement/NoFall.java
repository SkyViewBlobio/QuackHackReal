package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
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
        if (mc.thePlayer != null && !mc.thePlayer.capabilities.isFlying) {
            if (mc.thePlayer.fallDistance > 2.0F) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            }
        }
    }

    @SubscribeEvent
    public void onFallDamage(LivingFallEvent event) {
        if (event.entity == mc.thePlayer) {
            event.setCanceled(true);
        }
    }
}

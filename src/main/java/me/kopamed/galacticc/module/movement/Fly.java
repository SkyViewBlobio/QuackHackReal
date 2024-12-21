//Noi bypass lol sry vbabay ur going back to mumbay
package me.kopamed.galacticc.module.movement;
//todo maybe only trigger if elytra enabled?
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;
//ist draußen
public class Fly extends Module {
    public Fly(){
    super("Fly", "nyooom", true, false, Category.BEWEGUNG);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        if (mc.player != null) {
            if (!mc.player.capabilities.isFlying) {
                mc.player.jump();
                mc.player.capabilities.allowFlying = true;
                mc.player.capabilities.isFlying = true;
            }
        }
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (mc.player != null) {
            if (mc.player.capabilities.allowFlying) {
                mc.player.capabilities.allowFlying = false;
                mc.player.capabilities.isFlying = false;
            }
        }
    }

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent e) {
        if (Galacticc.instance.destructed) {return;}
        Set<Double> hash_Set = new HashSet<Double>();
        if (e.player.height == 0) {
            this.toggle();
        }
    }
}

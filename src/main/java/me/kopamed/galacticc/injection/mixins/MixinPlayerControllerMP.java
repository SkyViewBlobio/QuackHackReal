package me.kopamed.galacticc.injection.mixins;

import me.kopamed.galacticc.events.Event;
import me.kopamed.galacticc.events.Network.EventClickBlock;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = { PlayerControllerMP.class }, priority = Integer.MAX_VALUE)
public class MixinPlayerControllerMP {
    @Inject(method = {"onPlayerDamageBlock"}, at = {@At("HEAD")}, cancellable = true)
    public void onPlayerDamageBlock(final BlockPos position, final EnumFacing side, final CallbackInfoReturnable<Boolean> info) {
        final EventClickBlock event = new EventClickBlock(Event.Stage.PRE, position, side);
        MinecraftForge.EVENT_BUS.post((net.minecraftforge.fml.common.eventhandler.Event) event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = {"clickBlock"}, at = {@At("HEAD")}, cancellable = true)
    public void clickBlock(final BlockPos position, final EnumFacing side, final CallbackInfoReturnable<Boolean> info) {
        final EventClickBlock event = new EventClickBlock(Event.Stage.POST, position, side);
        MinecraftForge.EVENT_BUS.post((net.minecraftforge.fml.common.eventhandler.Event) event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}
    /**
     * @author SkyView
     * @reason Used for basic reach manipulation.
     */
//    @Overwrite
//    public float getBlockReachDistance() {
//        final float baseReach = (float) Minecraft.getMinecraft().player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
//
//        if (ModuleReach.INSTANCE.isToggled()) {
//            float reachAdd = (float) Galacticc.instance.settingsManager.getSettingByName(ModuleReach.INSTANCE, "Reach Add").getValDouble();
//            return baseReach + reachAdd;
//        }
//
//        return Minecraft.getMinecraft().player.isCreative() ? baseReach : (baseReach - 0.5f);
//    }
//    @Inject(method = {"getBlockReachDistance"}, at = {@At(value = "RETURN")}, cancellable = true)
//    private void getReachDistanceHook(CallbackInfoReturnable<Float> distance) {
//        System.out.println("Mixin fired!");  // Add this line to verify if the Mixin is firing
//        // Check if the Reach module is enabled
//        Reach2 reachModule = Reach2.getInstance(); // Get the Reach instance
//        if (reachModule.isToggled()) {  // Check if the module is toggled (enabled)
//            float range = distance.getReturnValue();  // Get the original reach distance
//
//            // If override is enabled, set the reach to the custom value, otherwise add the custom value to the original reach
//            if (reachModule.override.getValBoolean()) {
//                distance.setReturnValue((float) reachModule.reach.getValDouble());  // Set custom reach
//            } else {
//                distance.setReturnValue(range + (float) reachModule.reach.getValDouble());  // Add custom reach value to the original reach
//            }
//        }
//    }
//}
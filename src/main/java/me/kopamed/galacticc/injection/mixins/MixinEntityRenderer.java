package me.kopamed.galacticc.injection.mixins;

import me.kopamed.galacticc.events.Network.ReachEventEarthhack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = { EntityRenderer.class }, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer

{
    private float lastReach;
    @Shadow
    @Final
    private Minecraft mc;
    @Redirect(
            method = "getMouseOver",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/multiplayer/PlayerControllerMP" +
                            ".getBlockReachDistance()F"))
    public float getBlockReachDistanceHook(PlayerControllerMP controller) {
        System.out.println("[MixinEntityRenderer] getBlockReachDistanceHook triggered.");
        ReachEventEarthhack event = new ReachEventEarthhack(controller.getBlockReachDistance(),
                0.0f);

        MinecraftForge.EVENT_BUS.post(event);
//
//        if (event.isCancelled()) {
//            lastReach = event.getReach();
//        } else {
//            lastReach = 0.0f;
//        }

        return this.mc.playerController.getBlockReachDistance() + lastReach;
    }
    }
package me.kopamed.galacticc.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.events.PacketEvent;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LightningOnTotemPop extends Module {

    public LightningOnTotemPop() {
        super("BlitzTotem", "" +
                ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Zaubert einen Blitz auf Zombies/Spieler die einen Totem nutzen.",
                false, false, Category.VISUELLES);
    }


    @Override
    public void onEnabled() {
        super.onEnabled();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent event) {
        // Check if the packet is received
        if (event.getTime() == PacketEvent.Time.Receive) {
            Packet<?> packet = event.getPacket();

            // Check if it's an SPacketEntityStatus packet
            if (packet instanceof net.minecraft.network.play.server.SPacketEntityStatus) {
                SPacketEntityStatus statusPacket = (SPacketEntityStatus) packet;

                // Status code 35 indicates a totem pop
                if (statusPacket.getOpCode() == 35) {
                    Entity entity = statusPacket.getEntity(Minecraft.getMinecraft().world);

                    // Ensure the entity is either a Zombie or a Player
                    if (entity instanceof EntityZombie || entity instanceof EntityPlayer) {

                        // Spawn lightning at the entity's position
                        spawnClientSideLightning(entity.getPosition());
                    }
                }
            }
        }
    }

    private void spawnClientSideLightning(BlockPos position) {
        // Create a client-side lightning bolt entity
        EntityLightningBolt lightningBolt = new EntityLightningBolt(
                Minecraft.getMinecraft().world,
                position.getX(), position.getY(), position.getZ(),
                false
        );

        // Add the lightning bolt to the world
        Minecraft.getMinecraft().world.addWeatherEffect(lightningBolt);
    }
}
//package me.kopamed.galacticc.module.combat;
//
//import io.netty.buffer.Unpooled;
//import me.kopamed.galacticc.Galacticc;
//import me.kopamed.galacticc.events.PacketEvent;
//import me.kopamed.galacticc.module.Category;
//import me.kopamed.galacticc.module.Module;
//import me.kopamed.galacticc.settings.Setting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.item.ItemBow;
//import net.minecraft.item.ItemStack;
//import net.minecraft.network.Packet;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.network.play.client.CPacketCustomPayload;
//import net.minecraft.network.play.client.CPacketEntityAction;
//import net.minecraft.network.play.client.CPacketPlayer;
//import net.minecraft.network.play.client.CPacketPlayerDigging;
//import net.minecraft.network.play.server.SPacketEntityVelocity;
//import net.minecraft.util.EnumHand;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
//import java.io.IOException;
//
//import java.util.ArrayList;
//
//public class OneShotArrowModule extends Module {
//
//    public OneShotArrowModule() {
//        super("BossKiller", "Applies velocity to the player which increase velocity of an arrow which deals tons of damage.", false, false, Category.ANGRIFF);
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    private boolean isChargingBow = false;
//    private long lastShootTime = 0;
//
//    private static final int VELOCITY_SPOOFS = 20; // Number of velocity spoof packets
//    private static final long COOLDOWN_TIME = 1000; // Cooldown in milliseconds
//
//    private ArrayList<String> spoofModes = new ArrayList<>();
//    private ExploitMode currentExploitMode;
//
//    private enum ExploitMode {
//        Fast, Strong, Strict, Phobos, WB;
//    }
//
//    @Override
//    public void onEnabled() {
//        super.onEnabled();
//
//        // Register modes dynamically
//        spoofModes.add("Fast");
//        spoofModes.add("Strong");
//        spoofModes.add("Strict");
//        spoofModes.add("Phobos");
//        spoofModes.add("WB");
//
//        // Register the "Exploit Modus" setting
//        Galacticc.instance.settingsManager.rSetting(new Setting("Exploit Modus", this, "Fast", spoofModes));
//
//        // Other relevant settings like RGB colors or transparency could be added here as per your requirements
//    }
//
//    @SubscribeEvent
//    public void onPacketSend(PacketEvent event) {
//        if (event.getTime() == PacketEvent.Time.Send) {
//            Packet<?> packet = event.getPacket();
//
//            if (packet instanceof CPacketPlayerDigging) {
//                CPacketPlayerDigging diggingPacket = (CPacketPlayerDigging) packet;
//
//                if (diggingPacket.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
//                    ItemStack handStack = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
//
//                    if (!handStack.isEmpty() && handStack.getItem() instanceof ItemBow) {
//                        long currentTime = System.currentTimeMillis();
//                        if (currentTime - lastShootTime >= COOLDOWN_TIME) {
//                            // Retrieve the selected exploit mode dynamically
//                            String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Exploit Modus").getValString();
//                            switch (mode) {
//                                case "Fast":
//                                    currentExploitMode = ExploitMode.Fast;
//                                    break;
//                                case "Strong":
//                                    currentExploitMode = ExploitMode.Strong;
//                                    break;
//                                case "Strict":
//                                    currentExploitMode = ExploitMode.Strict;
//                                    break;
//                                case "Phobos":
//                                    currentExploitMode = ExploitMode.Phobos;
//                                    break;
//                                case "WB":
//                                    currentExploitMode = ExploitMode.WB;
//                                    break;
//                                default:
//                                    currentExploitMode = ExploitMode.Fast; // Default mode
//                            }
//
//                            // Execute spoofing based on the selected mode
//                            doVelocitySpoofs();
//                            lastShootTime = currentTime;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void doVelocitySpoofs() {
//        Minecraft mc = Minecraft.getMinecraft();
//
//        // Simulate player sprinting
//        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
//
//        double velocityX = (Math.random() - 0.5) * 2.0; // Randomized X velocity
//        double velocityY = (Math.random() - 0.5) * 2.0; // Randomized Y velocity
//        double velocityZ = (Math.random() - 0.5) * 2.0; // Randomized Z velocity
//
//        // Adjust based on the selected exploit mode
//        switch (currentExploitMode) {
//            case Fast:
//                velocityX *= 10; // Increase for faster movement
//                velocityY *= 10;
//                velocityZ *= 10;
//                break;
//
//            case Strong:
//                velocityX *= 20; // Stronger spoof for more significant velocity
//                velocityY *= 20;
//                velocityZ *= 20;
//                break;
//
//            case Strict:
//                // Apply a more controlled movement spoof
//                velocityX *= 5;
//                velocityY *= 5;
//                velocityZ *= 5;
//                break;
//
//            case Phobos:
//                // Apply very small and random position adjustments for confusion
//                velocityX *= 0.00000001;
//                velocityY *= 0.00000001;
//                velocityZ *= 0.00000001;
//                break;
//
//            case WB:
//                // Randomized world border adjustment spoof
//                velocityX *= Math.random() * 29000000;
//                velocityY *= Math.random() * 29000000;
//                velocityZ *= Math.random() * 29000000;
//                break;
//        }
//
//        // Send spoofed position packets to apply adjusted velocity
//        for (int i = 0; i < VELOCITY_SPOOFS; i++) {
//            // Send the first spoofed position packet (false flag means it's not final)
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + velocityX, mc.player.posY + velocityY, mc.player.posZ + velocityZ, false));
//
//            // Send the second spoofed position packet (true flag means it's final)
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - velocityX, mc.player.posY - velocityY, mc.player.posZ - velocityZ, true));
//        }
//    }
//
//    @Override
//    public void onDisabled() {
//        super.onDisabled();
//    }
//}

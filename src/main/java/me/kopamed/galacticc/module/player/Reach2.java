package me.kopamed.galacticc.module.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
//todo fix
    public class Reach2 extends Module {
        private double currentReach;

        public Reach2() {
            super("Reach", "" +
                            ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Main Info:|" + ChatFormatting.WHITE +
                            "Allows modifying interaction distance up to 6 blocks." + ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Usage Info:|" + ChatFormatting.WHITE +
                            "Use this module to increase the distance for interacting with blocks and entities.",
                    false,
                    false,
                    Category.SPIELER);

            Galacticc.instance.settingsManager.rSetting(new Setting("Reach Distance", this, 3, 1, 6, false));
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (Galacticc.instance.destructed || mc.player == null || mc.world == null) return;

            double reachDistance = Galacticc.instance.settingsManager.getSettingByName(this, "Reach Distance").getValDouble();

            if (event.phase == TickEvent.Phase.END) {
                try {
                    // Only update reach distance if it has changed
                    if (currentReach != reachDistance) {
                        currentReach = reachDistance;
                    }

                    // Handle extended interaction logic (ray tracing for both block and entity)
                    Vec3d eyes = mc.player.getPositionEyes(1.0f);
                    Vec3d look = mc.player.getLook(1.0f);
                    Vec3d reachVec = eyes.add(look.scale(reachDistance)); // Extend the reach by your custom value

                    // Custom Block Ray Trace
                    RayTraceResult blockTrace = mc.world.rayTraceBlocks(eyes, reachVec, false, false, false);

                    if (blockTrace != null && blockTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                        BlockPos pos = blockTrace.getBlockPos();
                        // Custom logic for interacting with the block
                        System.out.println("Block hit at " + pos);
                    }

                    // Custom Entity Ray Trace (manual)
                    Entity entityHit = null;
                    AxisAlignedBB playerBox = mc.player.getEntityBoundingBox().grow(reachDistance); // Create the player's reach bounding box

                    for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, playerBox)) {
                        if (entity != mc.player) {
                            AxisAlignedBB entityBox = entity.getEntityBoundingBox(); // Get entity's bounding box
                            if (playerBox.intersects(entityBox)) { // Check if player reach intersects with entity bounding box
                                entityHit = entity;
                                break;
                            }
                        }
                    }

                    if (entityHit != null) {
                        // Custom logic for interacting with the entity
                        System.out.println("Entity hit: " + entityHit.getName());
                        if (entityHit instanceof EntityPlayer) {
                            // Perform custom interaction with the player
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[Reach Module] Error during tick: " + e.getMessage());
                }
            }
        }

        @Override
        public void onEnabled() {
            super.onEnabled();
            currentReach = -1; // Force recalculation of reach distance
        }

        @Override
        public void onDisabled() {
            super.onDisabled();
            // Reset to the default reach for Survival mode (4.5 blocks)
            currentReach = 4.5; // Restore default reach distance when disabled
        }
    }

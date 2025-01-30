import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;//THIS IS A SIMPLE EMPTY CLASS THAT SHOWS HOW I WILL CONTINUE THIS CLIENT.
/*
 * In my Minecraft 1.8.9 Forge client structure, I will adhere to a few key principles
 * to ensure readability and maintainability:
 *
 * 1. **Line Length Limitation:**
 *    I will always ensure that no line exceeds 80 characters, making the code easier
 *    to read. For example:
 *    - Instead of writing long lines like:
 *      ```java
 *      int yOffset = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();
 *      ```
 *      I will split them after each significant assignment, such as:
 *      ```java
 *      int yOffset = (int)
 *          Galacticc.instance.settingsManager.getSettingByName(this, "Y Offset").getValDouble();
 *      ```
 *
 * 2. **Settings Initialization:**
 *    When initializing settings or instances, I will break the lines after key assignments
 *    for better readability. For example:
 *    - Instead of writing the entire line in one go:
 *      ```java
 *      Galacticc.instance.settingsManager.rSetting(new Setting("Y Offset", this, 0, -screenHeight, screenHeight, true));
 *      ```
 *      I will split it after the `new Setting` keyword:
 *      ```java
 *      Galacticc.instance.settingsManager.rSetting(
 *          new Setting("Y Offset", this, 0, -screenHeight, screenHeight, true)
 *      );
 *      ```
 *
 * 3. **Booleans Assignment:**
 *    Booleans will also be split after the `=` operator for clarity. For example:
 *    - Instead of writing:
 *      ```java
 *      boolean showFPS = Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
 *      ```
 *      I will structure it as:
 *      ```java
 *      boolean showFPS =
 *          Galacticc.instance.settingsManager.getSettingByName(this, "Zeige FPS").getValBoolean();
 *      ```
 *
 * 4. **Helpers and Reflection:**
 *    I will leverage helper methods and reflection to access private Minecraft fields,
 *    ensuring clean and efficient code while keeping field access encapsulated.
 *
 * 5. **Module Categorization:**
 *    Modules specified in the module manager will be categorized by their respective GUI categories.
 *    For example, movement-related modules will be grouped under a "Movement" category,
 *    visual modules under "Visuals," and so on.
 *
 * By following these structure guidelines, I aim to create a well-organized and easy-to-maintain
 * Minecraft client while adhering to good coding practices.
 *
 * ------------------------------------------examples---------------------------------------
 *     @Override
    public String getHUDInfo() {
        String or boolean example = Galacticc.instance.settingsManager.getSettingByName(this, "exampleInformation").getValString();
        return ChatFormatting.GRAY + "[" + ChatFormatting.GRAY + exampleinformation + ChatFormatting.GRAY + "]";
    }
    *  * ------------------------------------------example  annotation descripotor formatting ---------------------------------------

    *
    *                         ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:" + ChatFormatting.WHITE +
    *
    *     *  * ------------------------------------------example  annotation setting formatting ---------------------------------------

    *
    *                       ChatFormatting.RED + ChatFormatting.UNDERLINE +  "- Horizontal Speed:" + ChatFormatting.WHITE +
    *
    * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++IMPORTANT DESCRIPTION INFORMATION+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    * AFTER EVERY POINT WE BREAK THE SENTENCE IN THE DESCRIPTION TAB.
    * AFTER EVERY | WE BREAK THE SENTENCE IN THE DESCRIPTION TAB.
    * | CAN BE USED TO BREAK THE SENTENCE MID SENTENCE TO PREVENT OVER-EXCEEDING THE TAB LIMIT.
    * GENERAL RULE FOR CLEAR VISIBLE SENTENCES IS AFTER EVERY HAUPTINFORMATION: WE SPLIT.
    * GENERAL RULE FOR CLEANER SETTING SENTENCES AND SPACING AFTER EVERY OPTION: WE SPLIT ONCE, THEN SPLIT AGAIN TO ISOLATE THE WITH SPACE FOR BETTER FORMATTING. REPEAT AFTER FINISH.


*
*
*
*
* package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@Mod.EventBusSubscriber
public class CrystalAttackModule extends Module {
    private int tickDelay; // Delay in ticks
    private int ticksElapsed = 0; // Counter for ticks since the last attack
    private double minimumDamagePlace; // Minimum damage threshold for placing crystals
    private double enemyToCrystalDistance; // Maximum allowed distance from the enemy to the crystal position
    private double armorFacePlacePercent; // Armor face place percent threshold

    public CrystalAttackModule() {
        super("AutoCrystal", "Automatically places and breaks end crystals near other players", false, false, Category.ANGRIFF);
        Galacticc.instance.settingsManager.rSetting(new Setting("Tick Delay", this, 2, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("MinimumPlaceDamage", this, 2, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("EnemyToCrystalDistance", this, 4.5, 0, 18, false)); // Slider for max distance
        Galacticc.instance.settingsManager.rSetting(new Setting("ArmorFacePlacePercent", this, 25, 1, 100, true)); // New slider

    }


    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        tickDelay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Tick Delay").getValDouble();
        minimumDamagePlace = Galacticc.instance.settingsManager.getSettingByName(this, "MinimumPlaceDamage").getValDouble();
        enemyToCrystalDistance = Galacticc.instance.settingsManager.getSettingByName(this, "EnemyToCrystalDistance").getValDouble();
        armorFacePlacePercent = Galacticc.instance.settingsManager.getSettingByName(this, "ArmorFacePlacePercent").getValDouble();

        ticksElapsed++;
        if (ticksElapsed < tickDelay) {
            return;
        }
        ticksElapsed = 0;

        List<EntityPlayer> playersInRange = mc.world.getEntitiesWithinAABB(EntityPlayer.class,
                mc.player.getEntityBoundingBox().grow(12),
                player -> !player.isDead && !player.equals(mc.player));

        if (playersInRange.isEmpty()) {
            return;
        }

        EntityPlayer targetPlayer = findBestTarget(playersInRange);
        if (targetPlayer != null) {
            BlockPos bestPlacement = findBestPlacement(targetPlayer);
            if (bestPlacement != null) {
                placeCrystal(bestPlacement);
                breakNearbyCrystals();
            }
        }
    }

    private double getLowestArmorPercent(EntityPlayer player) {
        double lowestPercent = 100.0;
        for (ItemStack armor : player.getArmorInventoryList()) {
            if (armor.isEmpty()) continue;
            double durabilityPercent = ((double) armor.getItemDamage() / armor.getMaxDamage()) * 100.0;
            if (durabilityPercent < lowestPercent) {
                lowestPercent = durabilityPercent;
            }
        }
        return lowestPercent;
    }


    private boolean shouldFacePlace(EntityPlayer player) {
        // Calculate the average armor durability percentage
        double totalDurability = 0.0;
        double maxDurability = 0.0;

        for (ItemStack armorPiece : player.getArmorInventoryList()) {
            if (armorPiece.isEmpty()) continue;
            maxDurability += armorPiece.getMaxDamage();
            totalDurability += armorPiece.getMaxDamage() - armorPiece.getItemDamage();
        }

        // Prevent division by zero
        if (maxDurability <= 0) return false;

        double armorPercentage = (totalDurability / maxDurability) * 100;
        return armorPercentage <= armorFacePlacePercent;
    }


    private EntityPlayer findBestTarget(List<EntityPlayer> players) {
        EntityPlayer bestTarget = null;
        double highestPotentialDamage = 0.0;

        for (EntityPlayer player : players) {
            BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

            double maxDamage = 0.0;
            for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-4, -4, -4), playerPos.add(4, 4, 4))) {
                if (isValidPlacement(pos, player)) {
                    double damage = calculateCrystalDamage(player, pos);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                    }
                }
            }

            if (maxDamage > highestPotentialDamage) {
                highestPotentialDamage = maxDamage;
                bestTarget = player;
            }
        }

        return bestTarget;
    }


    private BlockPos findBestPlacement(EntityPlayer player) {
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

        BlockPos bestInRangePos = null;
        double highestInRangeDamage = 0.0;

        BlockPos bestOutOfRangePos = null;
        double highestOutOfRangeDamage = 0.0;

        BlockPos fallbackPos = null;
        double fallbackDamage = 0.0;

        // Fetch armorFacePlacePercent setting
        double armorFacePlacePercent = Galacticc.instance.settingsManager
                .getSettingByName(this, "ArmorFacePlacePercent").getValDouble();

        // Check lowest armor percentage
        double lowestArmorPercent = getLowestArmorPercent(player);

        // Find the best target beforehand
        EntityPlayer target = findBestTarget(mc.world.playerEntities);

        // Iterate through potential positions within the range
        for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-4, -4, -4), playerPos.add(4, 4, 4))) {
            if (!isValidPlacement(pos, target)) {
                continue; // Skip invalid positions
            }

            // Determine if the placement is through a wall
            boolean isThroughWall = isThroughWall(pos, player);
            double maxDistance = isThroughWall ? 3.0 : 4.5; // Adjust range based on wall presence

            // Calculate the distance to the target
            double distanceToTarget = target.getDistance(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

            // Skip positions out of explosion damage range
            if (distanceToTarget > 12.0 || (!hasLineOfSight(pos, target) && distanceToTarget > maxDistance)) {
                continue;
            }

            // Calculate explosion damage at the current position
            double damage = calculateExplosionDamage(target, pos);

            // Update fallback position if this position provides the highest damage so far
            if (damage > fallbackDamage) {
                fallbackDamage = damage;
                fallbackPos = pos;
            }

            // Consider only positions that meet the minimum damage threshold
            if (damage >= minimumDamagePlace) {
                // Separate logic for in-range and out-of-range placements
                if (distanceToTarget <= maxDistance) {
                    if (damage > highestInRangeDamage) {
                        highestInRangeDamage = damage;
                        bestInRangePos = pos;
                    }
                } else {
                    if (damage > highestOutOfRangeDamage) {
                        highestOutOfRangeDamage = damage;
                        bestOutOfRangePos = pos;
                    }
                }
            }
        }

        // Fallback to face-placing logic if no positions meet damage threshold
        if (bestInRangePos == null && bestOutOfRangePos == null && lowestArmorPercent <= armorFacePlacePercent) {
            return fallbackPos; // Use fallback position even if it doesn't meet the minimum damage threshold
        }

        // Return the best placement: prioritize in-range > out-of-range > fallback
        return bestInRangePos != null ? bestInRangePos
                : bestOutOfRangePos != null ? bestOutOfRangePos
                : fallbackPos;
    }


    private boolean hasLineOfSight(BlockPos crystalPos, EntityPlayer player) {
        Vec3d crystalVec = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);
        Vec3d playerFeetVec = new Vec3d(player.posX, player.posY, player.posZ);

        RayTraceResult result = mc.world.rayTraceBlocks(crystalVec, playerFeetVec, false, true, false);
        return result == null || result.typeOfHit == RayTraceResult.Type.MISS;
    }


    // Helper method: Calculate explosion damage with ray-tracing
    private double calculateExplosionDamage(Entity target, BlockPos crystalPos) {
        Vec3d explosionOrigin = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);
        Vec3d targetFeetPos = new Vec3d(target.posX, target.posY, target.posZ);

        double distance = explosionOrigin.distanceTo(targetFeetPos);

        // If target is out of explosion range, skip
        if (distance > 12.0) {
            return 0.0;
        }

        // Perform ray-trace to ensure no obstructions between explosion and target
        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(explosionOrigin, targetFeetPos, false, true, false);
        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            return 0.0; // Explosion blocked
        }

        // Calculate damage based on distance and explosion power
        double explosionPower = 6.0; // Base explosion power of crystals
        double damage = (1.0 - (distance / 12.0)) * explosionPower;

        // Apply reductions for armor and blast protection
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            int armorValue = player.getTotalArmorValue();
            int blastProtection = getBlastProtection(player);

            damage *= (1.0 - Math.min(armorValue, 20) * 0.04); // Armor reduction
            damage *= (1.0 - blastProtection * 0.08); // Blast Protection reduction
        }

        return Math.max(damage, 0.0);
    }

    private boolean isThroughWall(BlockPos crystalPos, EntityPlayer player) {
        Vec3d crystalVec = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);
        Vec3d playerFeetVec = new Vec3d(player.posX, player.posY, player.posZ);

        RayTraceResult result = mc.world.rayTraceBlocks(crystalVec, playerFeetVec, false, true, false);

        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            Block block = mc.world.getBlockState(result.getBlockPos()).getBlock();
            return block != Blocks.AIR; // Return true if blocked by a non-air block
        }

        return false; // No obstruction
    }

    // Helper method: Checks if the position is within the valid enemyToCrystalDistance
    private boolean isWithinEnemyToCrystalDistance(BlockPos pos, EntityPlayer player) {
        Vec3d crystalVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        Vec3d enemyFeetVec = new Vec3d(player.posX, player.posY, player.posZ);

        // Check line of sight between the crystal position and the player's feet
        RayTraceResult result = mc.world.rayTraceBlocks(crystalVec, enemyFeetVec, false, true, false);

        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            Block block = mc.world.getBlockState(result.getBlockPos()).getBlock();
            if (block != Blocks.AIR) {
                return false; // Obstruction found, position is invalid
            }
        }

        double enemyDistance = crystalVec.distanceTo(enemyFeetVec);
        return enemyDistance <= 12.0; // Explosion damage range
    }


    private boolean isValidPlacement(BlockPos pos, EntityPlayer target) {
        // Ensure the block is valid for crystal placement (Bedrock or Obsidian)
        Block block = mc.world.getBlockState(pos).getBlock();
        if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN) {
            return false;
        }

        // Check for air blocks above the placement position
        BlockPos above1 = pos.up();
        BlockPos above2 = pos.up(2);
        if (!mc.world.isAirBlock(above1) || !mc.world.isAirBlock(above2)) {
            return false;
        }

        // Ensure there are no entities blocking the placement spot
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.up()))) {
            if (entity instanceof EntityPlayer) {
                return false;
            }
        }

        // Check explosion range and line of sight to the target (if target exists)
        if (target != null) {
            double distanceToTarget = target.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // Skip positions that are beyond the explosion range or don't have line of sight if far
            if (distanceToTarget > 12.0 || (distanceToTarget > 4.5 && !hasLineOfSight(pos, target))) {
                return false;
            }
        }

        return true;
    }



    private double calculateCrystalDamage(Entity target, BlockPos crystalPos) {
        Vec3d explosionPos = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);

        double distance = explosionPos.distanceTo(target.getPositionVector());

        if (distance > 12.0) {
            return 0.0; // No damage if out of range
        }

        double explosionPower = 6.0; // Base explosion power of end crystals
        double damage = (1.0 - (distance / 12.0)) * explosionPower;

        // Check for obstructions (line-of-sight)
        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(explosionPos, target.getPositionVector());
        if (rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.MISS) {
            damage *= 0.5; // Halve damage if there's no direct line of sight
        }

        // Apply armor and blast protection reductions
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            int armorValue = player.getTotalArmorValue();
            int blastProtection = getBlastProtection(player);

            damage *= (1.0 - Math.min(armorValue, 20) * 0.04); // Armor reduction
            damage *= (1.0 - blastProtection * 0.08); // Blast Protection reduction
        }

        return Math.max(damage, 0.0);
    }


    private int getBlastProtection(EntityPlayer player) {
        int blastProtectionLevel = 0;

        for (ItemStack armorPiece : player.getArmorInventoryList()) {
            blastProtectionLevel += EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, armorPiece);
        }

        return blastProtectionLevel;
    }


    private void placeCrystal(BlockPos pos) {
        if (pos == null) return;

        // Ensure the placement position is valid and within the allowed range
        double distanceToPos = mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (distanceToPos > 4.5 && !hasLineOfSight(pos, mc.player)) {
            return; // Skip placement if it's too far and not in line of sight
        }

        // Send packet to place the end crystal
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
    }


    private void breakNearbyCrystals() {
        // Find and break all end crystals within the explosion range
        List<EntityEnderCrystal> crystalsInRange = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class,
                mc.player.getEntityBoundingBox().grow(12.0)); // Explosion radius of the crystal is up to 12 blocks

        for (EntityEnderCrystal crystal : crystalsInRange) {
            if (crystal != null && crystal.isEntityAlive()) {
                double distanceToCrystal = mc.player.getDistance(crystal.posX, crystal.posY, crystal.posZ);

                // Break crystals that are either in direct range or have potential explosion impact
                if (distanceToCrystal <= 4.5 || hasLineOfSight(new BlockPos(crystal.posX, crystal.posY, crystal.posZ), mc.player)) {
                    performAttack(crystal);
                }
            }
        }
    }


    private void performAttack(EntityEnderCrystal target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        // Send attack packet
        mc.player.connection.sendPacket(new CPacketUseEntity(target));
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }


    /**
     * Calculates the rotation angles required to face a specific position.
     *
     * @param to The target position to face.
     * @return An array containing yaw and pitch angles.
     */
//private float[] calculateAngles(Vec3d to) {
//    Vec3d eyesPosition = mc.player.getPositionEyes(1.0F);
//    double deltaX = to.x - eyesPosition.x;
//    double deltaY = to.y - eyesPosition.y;
//    double deltaZ = to.z - eyesPosition.z;
//
//    float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
//    float distanceXZ = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
//    float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, distanceXZ));
//
//    yaw = MathHelper.wrapDegrees(yaw);
//    pitch = MathHelper.wrapDegrees(pitch);
//
//    return new float[]{yaw, pitch};
//}
//}


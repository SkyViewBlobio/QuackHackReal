//package me.kopamed.galacticc.module.combat;
//
//import me.kopamed.galacticc.Galacticc;
//import me.kopamed.galacticc.module.Category;
//import me.kopamed.galacticc.module.Module;
//import me.kopamed.galacticc.settings.Setting;
//import net.minecraft.block.Block;
//import net.minecraft.enchantment.EnchantmentHelper;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.SharedMonsterAttributes;
//import net.minecraft.entity.item.EntityEnderCrystal;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Blocks;
//import net.minecraft.init.Enchantments;
//import net.minecraft.item.ItemStack;
//import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
//import net.minecraft.network.play.client.CPacketUseEntity;
//import net.minecraft.util.CombatRules;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.EnumHand;
//import net.minecraft.util.math.*;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent;
//
//import java.util.List;
//
//@Mod.EventBusSubscriber
//public class CrystalAttackModule extends Module {
//    private int tickDelay; // Delay in ticks
//    private int ticksElapsed = 0; // Counter for ticks since the last attack
//    private double minimumDamagePlace; // Minimum damage threshold for placing crystals
//    private double enemyToCrystalDistance; // Maximum allowed distance from the enemy to the crystal position
//
//    public CrystalAttackModule() {
//        super("AutoCrystal", "Automatically places and breaks end crystals near other players", false, false, Category.ANGRIFF);
//        Galacticc.instance.settingsManager.rSetting(new Setting("Tick Delay", this, 2, 0, 10, true));
//        Galacticc.instance.settingsManager.rSetting(new Setting("MinimumPlaceDamage", this, 2, 0, 10, true));
//        Galacticc.instance.settingsManager.rSetting(new Setting("EnemyToCrystalDistance", this, 4.5, 0, 10, false)); // Slider for max distance
//    }
//
//    @SubscribeEvent
//    public void onTick(TickEvent.PlayerTickEvent event) {
//        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
//            return;
//        }
//
//        // Update settings dynamically
//        tickDelay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Tick Delay").getValDouble();
//        minimumDamagePlace = Galacticc.instance.settingsManager.getSettingByName(this, "MinimumPlaceDamage").getValDouble();
//        enemyToCrystalDistance = Galacticc.instance.settingsManager.getSettingByName(this, "EnemyToCrystalDistance").getValDouble();
//
//        ticksElapsed++;
//        if (ticksElapsed < tickDelay) {
//            return;
//        }
//        ticksElapsed = 0;
//
//        // Adjust range for players based on enemyToCrystalDistance
//        List<EntityPlayer> playersInRange = mc.world.getEntitiesWithinAABB(EntityPlayer.class,
//                mc.player.getEntityBoundingBox().grow(enemyToCrystalDistance),
//                player -> !player.isDead && !player.equals(mc.player));
//
//        if (playersInRange.isEmpty()) {
//            return;
//        }
//
//        EntityPlayer targetPlayer = findBestTarget(playersInRange);
//        if (targetPlayer != null) {
//            BlockPos bestPlacement = findBestPlacement(targetPlayer);
//            if (bestPlacement != null) {
//                placeCrystal(bestPlacement);
//                breakNearbyCrystals();
//            }
//        }
//    }
//
//
//    private EntityPlayer findBestTarget(List<EntityPlayer> players) {
//        EntityPlayer bestTarget = null;
//        double highestPotentialDamage = 0.0;
//
//        for (EntityPlayer player : players) {
//            BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
//
//            double maxDamage = 0.0;
//            for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-4, -4, -4), playerPos.add(4, 4, 4))) {
//                if (isValidPlacement(pos)) {
//                    double damage = calculateCrystalDamage(player, pos);
//                    if (damage > maxDamage) {
//                        maxDamage = damage;
//                    }
//                }
//            }
//
//            if (maxDamage > highestPotentialDamage) {
//                highestPotentialDamage = maxDamage;
//                bestTarget = player;
//            }
//        }
//
//        return bestTarget;
//    }
//
//    private BlockPos findBestPlacement(EntityPlayer player) {
//        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
//
//        BlockPos bestInRangePos = null;
//        double highestInRangeDamage = 0.0;
//
//        BlockPos bestOutOfRangePos = null;
//        double highestOutOfRangeDamage = 0.0;
//
//        BlockPos fallbackPos = null; // To store the best fallback position
//        double fallbackDamage = 0.0;
//
//        // Iterate over positions within a range determined by enemyToCrystalDistance
//        for (BlockPos pos : BlockPos.getAllInBox(playerPos.add(-enemyToCrystalDistance, -enemyToCrystalDistance, -enemyToCrystalDistance),
//                playerPos.add(enemyToCrystalDistance, enemyToCrystalDistance, enemyToCrystalDistance))) {
//            if (isValidPlacement(pos)) {
//                double damage = calculateCrystalDamage(player, pos);
//                Vec3d crystalVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
//                Vec3d playerVec = player.getPositionVector();
//                double distanceToPlayer = crystalVec.distanceTo(playerVec);
//
//                // Update fallback position
//                if (damage > fallbackDamage) {
//                    fallbackDamage = damage;
//                    fallbackPos = pos;
//                }
//
//                // Check for placements meeting minimum damage requirements
//                if (damage >= minimumDamagePlace) {
//                    if (distanceToPlayer <= 4.5) { // Close-range logic
//                        if (damage > highestInRangeDamage) {
//                            highestInRangeDamage = damage;
//                            bestInRangePos = pos;
//                        }
//                    } else if (distanceToPlayer <= enemyToCrystalDistance) { // Extended range logic
//                        if (damage > highestOutOfRangeDamage) {
//                            highestOutOfRangeDamage = damage;
//                            bestOutOfRangePos = pos;
//                        }
//                    }
//                }
//            }
//        }
//
//        // Prioritize placements: In-range > Out-of-range > Fallback
//        if (bestInRangePos != null) {
//            return bestInRangePos;
//        }
//        if (bestOutOfRangePos != null) {
//            return bestOutOfRangePos;
//        }
//        return fallbackPos;
//    }
//
//    private float calculateDamage(Entity entity, BlockPos pos) {
//        Vec3d explosionPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
//        return getDamageFromExplosion(entity, explosionPos, true);
//    }
//
//    public static float getDamageFromExplosion(Entity entity, Vec3d vector, boolean blockDestruction) {
//        return calculateExplosionDamage(entity, vector, 6.0f, blockDestruction);
//    }
//
//    public static float calculateExplosionDamage(Entity entity, Vec3d vector, float explosionSize, boolean blockDestruction) {
//        double doubledExplosionSize = explosionSize * 2.0;
//        double dist = entity.getDistance(vector.x, vector.y, vector.z) / doubledExplosionSize;
//
//        if (dist > 1) return 0;
//
//        double v = (1 - dist);
//        float damage = CombatRules.getDamageAfterAbsorb(
//                (float) ((v * v + v) / 2.0 * 7.0 * doubledExplosionSize + 1.0),
//                ((EntityLivingBase) entity).getTotalArmorValue(),
//                (float) ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()
//        );
//
//        return Math.max(damage, 0);
//    }
//
//    private boolean isValidPlacement(BlockPos pos) {
//        Block block = mc.world.getBlockState(pos).getBlock();
//        if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN) {
//            return false;
//        }
//
//        BlockPos above1 = pos.up();
//        BlockPos above2 = pos.up(2);
//        if (!mc.world.isAirBlock(above1) || !mc.world.isAirBlock(above2)) {
//            return false;
//        }
//
//        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.up()))) {
//            if (entity instanceof EntityPlayer) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//
//    private double calculateCrystalDamage(Entity target, BlockPos crystalPos) {
//        Vec3d explosionPos = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);
//        double distance = explosionPos.distanceTo(target.getPositionVector());
//
//        if (distance > enemyToCrystalDistance) {
//            return 0.0; // No damage if out of range
//        }
//
//        double explosionPower = 6.0;
//        double damage = (1.0 - (distance / enemyToCrystalDistance)) * explosionPower;
//
//        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(explosionPos, target.getPositionVector());
//        if (rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.MISS) {
//            damage *= 0.5; // Reduce damage if there's no direct line of sight
//        }
//
//        if (target instanceof EntityPlayer) {
//            EntityPlayer player = (EntityPlayer) target;
//            int armorValue = player.getTotalArmorValue();
//            int blastProtection = getBlastProtection(player);
//
//            damage *= (1.0 - Math.min(armorValue, 20) * 0.04); // Armor reduction
//            damage *= (1.0 - blastProtection * 0.08); // Blast Protection reduction
//        }
//
//        if (Math.abs(crystalPos.getY() - Math.floor(target.posY)) <= 1) {
//            damage *= 1.5; // Boost for feet-level placement
//        }
//
//        return Math.max(damage, 0.0);
//    }
//
//    private int getBlastProtection(EntityPlayer player) {
//        int blastProtectionLevel = 0;
//
//        for (ItemStack armorPiece : player.getArmorInventoryList()) {
//            blastProtectionLevel += EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, armorPiece);
//        }
//
//        return blastProtectionLevel;
//    }
//
//
//    private void placeCrystal(BlockPos pos) {
//        if (pos == null) return;
//
//        // Send packet to place the end crystal
//        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
//                pos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
//    }
//
//    private void breakNearbyCrystals() {
//        // Find and break all end crystals within range
//        List<EntityEnderCrystal> crystalsInRange = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class,
//                mc.player.getEntityBoundingBox().grow(4.5));
//
//        for (EntityEnderCrystal crystal : crystalsInRange) {
//            if (crystal != null && crystal.isEntityAlive()) {
//                performAttack(crystal);
//            }
//        }
//    }
//
//    private void performAttack(EntityEnderCrystal target) {
//        if (target == null || !target.isEntityAlive()) {
//            return;
//        }
//
//        // Send attack packet
//        mc.player.connection.sendPacket(new CPacketUseEntity(target));
//        mc.player.swingArm(EnumHand.MAIN_HAND);
//    }
//
//    /**
//     * Calculates the rotation angles required to face a specific position.
//     *
//     * @param to The target position to face.
//     * @return An array containing yaw and pitch angles.
//     */
//    private float[] calculateAngles(Vec3d to) {
//        Vec3d eyesPosition = mc.player.getPositionEyes(1.0F);
//        double deltaX = to.x - eyesPosition.x;
//        double deltaY = to.y - eyesPosition.y;
//        double deltaZ = to.z - eyesPosition.z;
//
//        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
//        float distanceXZ = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
//        float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, distanceXZ));
//
//        yaw = MathHelper.wrapDegrees(yaw);
//        pitch = MathHelper.wrapDegrees(pitch);
//
//        return new float[]{yaw, pitch};
//    }
//}
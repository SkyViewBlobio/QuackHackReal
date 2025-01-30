package me.kopamed.galacticc.module.combat;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;
//todo add enemy range and uh add armor breaker
@Mod.EventBusSubscriber
public class CrystalAttackModule extends Module {
    private int ticksElapsed = 0;
    private double minimumDamagePlace;
    private final Map<EntityPlayer, Double> damageCache = new HashMap<>();
    private final Map<BlockPos, Boolean> placementCache = new ConcurrentHashMap<>();

    public CrystalAttackModule() {
        super("AutoCrystal", "Automatically places and breaks end crystals near other players", false, false, Category.ANGRIFF);
        Galacticc.instance.settingsManager.rSetting(new Setting("Tick Delay", this, 2, 0, 10, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("MinimumPlaceDamage", this, 2, 0, 6, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SelfDamage", this, 2, 0, 10, true));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) return;

        int tickDelay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Tick Delay").getValDouble();
        if (ticksElapsed++ < tickDelay) return;
        ticksElapsed = 0;

        updateSettings();
        damageCache.clear(); // Clear cache to recalculate priorities with current settings
        EntityPlayer target = findBestTarget();
        if (target != null) processTarget(target);
    }

    private void updateSettings() {
        minimumDamagePlace = Galacticc.instance.settingsManager.getSettingByName(this, "MinimumPlaceDamage").getValDouble();
    }

    private EntityPlayer findBestTarget() {
        return mc.world.getEntitiesWithinAABB(EntityPlayer.class,
                        mc.player.getEntityBoundingBox().grow(4.5),
                        player -> !player.isDead && !player.equals(mc.player))
                .stream()
                .max(Comparator.comparingDouble(this::calculatePlayerPriority))
                .orElse(null);
    }

    private double calculatePlayerPriority(EntityPlayer player) {
        return damageCache.computeIfAbsent(player, p -> {
            BlockPos center = new BlockPos(p.posX, p.posY, p.posZ);
            return StreamSupport.stream(
                            BlockPos.getAllInBox(center.add(-3, -2, -3), center.add(3, 2, 3)).spliterator(),
                            true)
                    .filter(pos -> placementCache.computeIfAbsent(pos, this::isValidPlacement))
                    .mapToDouble(pos -> calculateCrystalDamage(p, pos))
                    .filter(damage -> damage >= minimumDamagePlace) // Add this filter
                    .max()
                    .orElse(0.0); // No valid positions = 0 priority
        });
    }

    private void processTarget(EntityPlayer target) {
        double maxSelfDamage = Galacticc.instance.settingsManager.getSettingByName(this, "SelfDamage").getValDouble();
        BlockPos bestPos = findBestPlacement(target, maxSelfDamage);
        if (bestPos != null) {
            placeCrystal(bestPos);
            breakOptimalCrystals();
        }
    }


    private BlockPos findBestPlacement(EntityPlayer target, double maxSelfDamage) {
        BlockPos center = new BlockPos(target.posX, target.posY, target.posZ);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos bestPos = null;
        double bestDamage = -1.0;
        double closestDistance = Double.MAX_VALUE;

        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    mutablePos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);

                    if (!isValidPlacement(mutablePos)) continue;

                    double attackerDistance = mc.player.getDistance(
                            mutablePos.getX() + 0.5,
                            mutablePos.getY() + 1.0,
                            mutablePos.getZ() + 0.5
                    );
                    if (attackerDistance > 4.5) continue;

                    if (!hasLineOfSightToFeet(mutablePos, target)) continue;

                    double targetDamage = calculateCrystalDamage(target, mutablePos);
                    double selfDamage = calculateCrystalDamage(mc.player, mutablePos);

                    // Use pre-reduction damage for target threshold
                    double targetDamageRaw = calculateCrystalDamage(target, mutablePos);
                    boolean selfDamageValid = maxSelfDamage > 0 ?
                            selfDamage <= maxSelfDamage :
                            selfDamage <= 0.1; // Increased tolerance for "no self damage"

                    if (targetDamageRaw < minimumDamagePlace || !selfDamageValid) continue;

                    if (targetDamage > bestDamage ||
                            (targetDamage == bestDamage && attackerDistance < closestDistance)) {
                        bestPos = mutablePos.toImmutable();
                        bestDamage = targetDamage;
                        closestDistance = attackerDistance;
                    }
                }
            }
        }
        return bestPos;
    }

    private boolean hasLineOfSightToFeet(BlockPos pos, EntityPlayer target) {
        Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        Vec3d targetFeet = new Vec3d(target.posX, target.posY, target.posZ);
        return mc.world.rayTraceBlocks(crystalPos, targetFeet, false, true, false) == null;
    }

    private boolean isValidPlacement(BlockPos pos) {
        // Original validation checks
        IBlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.OBSIDIAN) return false;

        BlockPos above1 = pos.up();
        BlockPos above2 = pos.up(2);
        if (!mc.world.isAirBlock(above1) || !mc.world.isAirBlock(above2)) return false;

        // Original collision check (only check the first air block)
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(above1))
                .stream()
                .noneMatch(e -> e instanceof EntityPlayer);
    }


    private double calculateCrystalDamage(Entity target, BlockPos crystalPos) {
        Vec3d explosionVec = new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() + 1.0, crystalPos.getZ() + 0.5);
        double distanceSq = explosionVec.squareDistanceTo(target.getPositionVector());
        if (distanceSq > 144.0) return 0.0;

        double scaledDistance = Math.sqrt(distanceSq) / 12.0;
        double rawDamage = (1.0 - scaledDistance) * getExplosionPower();

        // Calculate damage before reductions for minimumDamagePlace check
        double preReductionDamage = rawDamage;
        if (hasObstruction(explosionVec, target.getPositionVector())) preReductionDamage *= 0.5;

        // Apply reductions for final damage
        double finalDamage = preReductionDamage;
        if (target instanceof EntityPlayer) {
            finalDamage = applyProtectionReductions((EntityPlayer) target, finalDamage);
        }

        // Return pre-reduction damage for placement checks
        return (target == mc.player) ? finalDamage : preReductionDamage;
    }


    private double getExplosionPower() {
        return mc.world.getDifficulty() == EnumDifficulty.HARD ? 9.0 : 6.0;
    }

    private boolean hasObstruction(Vec3d start, Vec3d end) {
        return mc.world.rayTraceBlocks(start, end, false, true, false) != null;
    }

    private double applyProtectionReductions(EntityPlayer player, double damage) {
        int armor = player.getTotalArmorValue();
        int blastProt = StreamSupport.stream(player.getArmorInventoryList().spliterator(), false)
                .mapToInt(stack -> EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, stack))
                .sum();

        damage *= (1.0 - Math.min(armor, 20) * 0.04);
        return damage * (1.0 - blastProt * 0.08);
    }


    private void breakOptimalCrystals() {
        mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, mc.player.getEntityBoundingBox().grow(4.5))
                .stream()
                .filter(crystal -> crystal != null && crystal.isEntityAlive())
                .filter(crystal -> mc.player.getDistanceSq(crystal) <= 20.25)
                .max(Comparator.comparingDouble(crystal ->
                        -crystal.getDistanceSq(mc.player.posX, mc.player.posY, mc.player.posZ)))
                .ifPresent(this::performAttack);
    }

    private void placeCrystal(BlockPos pos) {
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
    }

    private void performAttack(EntityEnderCrystal crystal) {
        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }
}
package me.kopamed.galacticc.module.render;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


//todo cleanup and explosion damage aka take damage in general.
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakePlayer extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private EntityOtherPlayerMP fakePlayer; // The fake player entity
    private float angle; // Current angle for walking in a circle
    private long lastEatTime; // Track the last time the fake player ate
    private static final net.minecraft.util.DamageSource FAKE_EXPLOSION_DAMAGE = new net.minecraft.util.DamageSource("fakeExplosion").setExplosion();

    public FakePlayer() {
        super("FakePlayer", "Spawns a fake player (Notch) that walks in a circle around you.", true, false, Category.VISUELLES);
        Galacticc.instance.settingsManager.rSetting(new Setting("Range", this, 6.0F, 3.0F, 15.0F, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("BlastProtection", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Regenerate", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("EatDelay", this, 20.0F, 1.0F, 40.0F, false));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onEnabled() {
        if (mc.world == null || mc.player == null) return;

        // Create the fake player entity with Notch's profile
        GameProfile notchProfile = new GameProfile(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Notch");
        fakePlayer = new EntityOtherPlayerMP(mc.world, notchProfile);

        // Ensure the fake player is positioned before applying equipment
        fakePlayer.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ);

        // Check if the fake player is valid before equipping armor
        if (fakePlayer != null && Galacticc.instance.settingsManager.getSettingByName(this, "BlastProtection").getValBoolean()) {
            equipBlastProtection(); // Equip armor
        }

        // Add the fake player to the client world
        mc.world.addEntityToWorld(-12345, fakePlayer); // Use a unique ID for the fake player
        angle = 0.0F; // Reset the angle
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return; // Handle only the END phase
        if (mc.world == null || mc.player == null || fakePlayer == null) {
            return;
        }

        // Get the circle range from the settings slider
        float range = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();

        // Maintain circular motion and range
        maintainCircularPath(range);

        // Apply gravity
        applyGravity();

        // Step handling
        handleStepLogic();

        // Call custom damage handling
        handleDamage();

        // Handle regeneration if enabled
        if (Galacticc.instance.settingsManager.getSettingByName(this, "Regenerate").getValBoolean()) {
            handleRegeneration();
        }

        // Check and update armor dynamically based on BlastProtection setting
        updateArmor();

        // Force position update
        fakePlayer.setPosition(fakePlayer.posX + fakePlayer.motionX, fakePlayer.posY + fakePlayer.motionY, fakePlayer.posZ + fakePlayer.motionZ);
    }


    private void maintainCircularPath(float range) {
        double centerX = mc.player.posX;
        double centerZ = mc.player.posZ;
        double currentDistance = Math.sqrt(Math.pow(fakePlayer.posX - centerX, 2) + Math.pow(fakePlayer.posZ - centerZ, 2));

        if (currentDistance > range + 1.0 || currentDistance < range - 1.0) {
            // Adjust position to match the circular path
            double radians = Math.toRadians(mc.player.ticksExisted * 2.0); // Adjust speed as needed
            double targetX = centerX + range * Math.cos(radians);
            double targetZ = centerZ + range * Math.sin(radians);

            fakePlayer.setPosition(targetX, fakePlayer.posY, targetZ);
        } else {
            moveInCircle(range);
        }
    }

    private void moveInCircle(float range) {
        double centerX = mc.player.posX;
        double centerZ = mc.player.posZ;
        double radians = Math.toRadians(mc.player.ticksExisted * 2.0); // Adjust speed as needed
        double newX = centerX + range * Math.cos(radians);
        double newZ = centerZ + range * Math.sin(radians);

        double motionX = newX - fakePlayer.posX;
        double motionZ = newZ - fakePlayer.posZ;

        fakePlayer.motionX = motionX;
        fakePlayer.motionZ = motionZ;

        // Use move method instead of moveEntity
        fakePlayer.move(MoverType.SELF, fakePlayer.motionX, fakePlayer.motionY, fakePlayer.motionZ);
    }


    private void applyGravity() {
        BlockPos below = new BlockPos(fakePlayer.posX, fakePlayer.posY - 0.1, fakePlayer.posZ);
        if (!mc.world.isAirBlock(below)) {
            fakePlayer.motionY = 0; // Stop falling if on solid ground
        } else {
            fakePlayer.motionY -= 0.08; // Gravity effect
        }
    }

    private void handleStepLogic() {
        double stepHeight = 10.0; // Allow stepping up to 10 blocks
        if (!mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isSneaking()) {
            fakePlayer.stepHeight = (float) stepHeight;
        } else {
            fakePlayer.stepHeight = 0.6F; // Default step height
        }

        if (fakePlayer.collidedHorizontally) { // Updated field name
            // Replace fake player if stuck
            float range = (float) Galacticc.instance.settingsManager.getSettingByName(this, "Range").getValDouble();
            double radians = Math.toRadians(mc.player.ticksExisted * 2.0);
            double targetX = mc.player.posX + range * Math.cos(radians);
            double targetZ = mc.player.posZ + range * Math.sin(radians);
            fakePlayer.setPosition(targetX, mc.player.posY, targetZ);
        }
    }

    private void handleRegeneration() {
        float eatDelay = (float) Galacticc.instance.settingsManager.getSettingByName(this, "EatDelay").getValDouble();
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastEatTime >= eatDelay * 50) { // Convert ticks to milliseconds
            fakePlayer.inventory.setInventorySlotContents(0, new ItemStack(Items.GOLDEN_APPLE, 1, 1)); // Enchanted golden apple
            mc.playerController.processRightClick(fakePlayer, mc.world, EnumHand.MAIN_HAND); // Use processRightClick
            lastEatTime = currentTime;
        }
    }

    private void equipBlastProtection() {
        if (fakePlayer == null || fakePlayer.inventory == null) {
            System.out.println("Fake player or inventory is null. Cannot equip armor.");
            return;
        }

        HashMap<Enchantment, Integer> blastProt = new HashMap<>();
        blastProt.put(Enchantments.BLAST_PROTECTION, 4); // Blast Protection IV

        try {
            // Assign enchanted armor directly to the fake player's inventory
            fakePlayer.inventory.armorInventory.set(3, addEnchantment(new ItemStack(Items.DIAMOND_HELMET), blastProt));
            fakePlayer.inventory.armorInventory.set(2, addEnchantment(new ItemStack(Items.DIAMOND_CHESTPLATE), blastProt));
            fakePlayer.inventory.armorInventory.set(1, addEnchantment(new ItemStack(Items.DIAMOND_LEGGINGS), blastProt));
            fakePlayer.inventory.armorInventory.set(0, addEnchantment(new ItemStack(Items.DIAMOND_BOOTS), blastProt));
        } catch (Exception e) {
            System.err.println("Failed to equip armor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateArmor() {
        boolean blastProtection = Galacticc.instance.settingsManager.getSettingByName(this, "BlastProtection").getValBoolean();

        if (blastProtection) {
            if (fakePlayer.inventory.armorInventory.get(3).isEmpty() ||
                    fakePlayer.inventory.armorInventory.get(2).isEmpty() ||
                    fakePlayer.inventory.armorInventory.get(1).isEmpty() ||
                    fakePlayer.inventory.armorInventory.get(0).isEmpty()) {
                equipBlastProtection(); // Equip armor if not already equipped
            }
        } else {
            // Remove armor if BlastProtection is turned off
            for (int i = 0; i < 4; i++) {
                fakePlayer.inventory.armorInventory.set(i, ItemStack.EMPTY); // Clear armor slots
            }
        }
    }

    private ItemStack addEnchantment(ItemStack itemStack, HashMap<Enchantment, Integer> enchantments) {
        EnchantmentHelper.setEnchantments(enchantments, itemStack); // Directly use Enchantment as the key
        return itemStack;
    }
//todo make this for endcrystals
    private void handleDamage() {
        if (fakePlayer == null) return;

        // Handle damage from explosions
        fakePlayer.world.getEntitiesWithinAABBExcludingEntity(fakePlayer, fakePlayer.getEntityBoundingBox().expand(5.0, 5.0, 5.0))
                .stream()
                .filter(entity -> entity instanceof EntityTNTPrimed)
                .map(entity -> (EntityTNTPrimed) entity)
                .forEach(tnt -> {
                    try {
                        int fuse = tnt.getFuse(); // Use the getter method if available
                        if (fuse <= 1) {
                            double distance = fakePlayer.getDistance(tnt.posX, tnt.posY, tnt.posZ);
                            double maxDamageDistance = 5.0; // Explosion radius
                            if (distance <= maxDamageDistance) {
                                float damage = (float) (10.0 * (1.0 - distance / maxDamageDistance)); // Scale damage by distance
                                fakePlayer.attackEntityFrom(DamageSource.causeExplosionDamage((Explosion) null), damage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        // Handle damage from your sword
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit == fakePlayer && mc.gameSettings.keyBindAttack.isKeyDown()) {
            fakePlayer.attackEntityFrom(DamageSource.causePlayerDamage(mc.player), 8.0F); // Example sword damage
        }

        // Handle damage from your arrows
        fakePlayer.world.getEntitiesWithinAABBExcludingEntity(fakePlayer, fakePlayer.getEntityBoundingBox().expand(5.0, 5.0, 5.0))
                .stream()
                .filter(entity -> entity instanceof EntityArrow)
                .map(entity -> (EntityArrow) entity)
                .forEach(arrow -> {
                    if (arrow.shootingEntity == mc.player) { // Only accept arrows shot by you
                        fakePlayer.attackEntityFrom(DamageSource.causeArrowDamage(arrow, arrow.shootingEntity), 5.0F); // Example arrow damage
                        arrow.setDead(); // Remove arrow after hitting
                    }
                });

        // Prevent death
        if (fakePlayer.getHealth() <= 0.0F) {
            fakePlayer.setHealth(fakePlayer.getMaxHealth()); // Reset health
        }
    }

    @Override
    public String getHUDInfo() {
        boolean blastProtection = Galacticc.instance.settingsManager.getSettingByName(this, "BlastProtection").getValBoolean();
        boolean regenerate = Galacticc.instance.settingsManager.getSettingByName(this, "Regenerate").getValBoolean();
        float eatDelay = (float) Galacticc.instance.settingsManager.getSettingByName(this, "EatDelay").getValDouble();

        // Display fake player's health if it exists
        String fakePlayerHealth = fakePlayer != null ? String.format("HP: %.1f", fakePlayer.getHealth()) : "No FakePlayer";

        return ChatFormatting.GRAY + "[" +
                (blastProtection ? "BlastProtection" : "NoProtection") + ", " +
                (regenerate ? "Regen" : "NoRegen") + ", " +
                "EatDelay: " + eatDelay + ", " +
                fakePlayerHealth + "]";
    }

    @Override
    public void onDisabled() {
        if (mc.world != null && fakePlayer != null) {
            mc.world.removeEntityFromWorld(-12345); // Remove the fake player using its unique ID
            fakePlayer = null; // Dereference the fake player
        }
    }
}
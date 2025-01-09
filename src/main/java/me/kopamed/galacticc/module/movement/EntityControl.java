package me.kopamed.galacticc.module.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EntityControl extends Module {
    private long lastStepTime = 0;

    public EntityControl() {
        super("EntityControl", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Ermoeglicht die vollstaendige Kontrolle ueber| reitbare Entitaeten, einschliesslich| Pferden, Schweinen und anderen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Geschwindigkeit: " + ChatFormatting.WHITE +
                        "Steuert die Bewegungsgeschwindigkeit| der Entitaet basierend auf der| Eingabe durch die WASD-Tasten." + ChatFormatting.RED +
                        "- Schritt Hoehe: " + ChatFormatting.WHITE +
                        "Ermoeglicht die Anpassung der Hoehe,| ueber die die Entitaet steigen kann,| mit einer optionalen Verzoegerung." + ChatFormatting.RED +
                        "- Gehen auf Fluessigkeiten: " + ChatFormatting.WHITE +
                        "Erlaubt Entitaeten,| auf Wasser oder| Lava zu laufen, indem sie sanft| zur Oberflaeche steigen." + ChatFormatting.RED +
                        "- Lag verhindern: " + ChatFormatting.WHITE +
                        "Verhindert Ruecksetzbewegungen (Rubberbanding),| indem Bewegungskontrollen und Packet-Handling optimiert werden." + ChatFormatting.RED +
                        "- Fallschaden vermeiden: " + ChatFormatting.WHITE +
                        "Schuetzt die Entitaet vor Fallschaden,| auch bei grossen Hoehen oder| plötzlichen Abstiegen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Steuere die Bewegung von Entitaeten mit| den WASD-Tasten. Anpassungen koennen im| Modulmenue vorgenommen werden, um die| Leistung und Kontrolle zu optimieren.",
                true, false, Category.BEWEGUNG);


        float defaultSpeed = 0.3f;
        float defaultStepHeight = 1.0f;

        Galacticc.instance.settingsManager.rSetting(new Setting("Speed", this, defaultSpeed, 0.1, 2.0, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Step Height", this, defaultStepHeight, 0.5, 10.0, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Step Delay", this, 200.0, 50.0, 8000.0, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Walk on Liquids", this, true));
    }


    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null || mc.player.getRidingEntity() == null) {
            return;
        }

        Entity riddenEntity = mc.player.getRidingEntity();
        if (isControllableEntity(riddenEntity)) {
            double speed = Galacticc.instance.settingsManager.getSettingByName(this, "Speed").getValDouble();
            double stepHeight = Galacticc.instance.settingsManager.getSettingByName(this, "Step Height").getValDouble();
            double stepDelay = Galacticc.instance.settingsManager.getSettingByName(this, "Step Delay").getValDouble();
            boolean walkOnLiquids = Galacticc.instance.settingsManager.getSettingByName(this, "Walk on Liquids").getValBoolean();

            // Apply custom step height with delay
            applyStepHeightWithDelay(riddenEntity, stepHeight, (long) stepDelay);

            // Enable walking on liquids and automatic rising
            if (walkOnLiquids) {
                enableLiquidWalking(riddenEntity);
            }

            // Control entity movement via WASD
            handleEntityMovement(riddenEntity, speed);

            // Prevent fall damage using improved logic
            trickFallDamage(riddenEntity);

            // Trick the server into thinking the entity is on the ground
            riddenEntity.onGround = true;

            // Prevent fall damage
            riddenEntity.fallDistance = 0.0f;

            // Edge handling logic
            handleEdgeTraversal(riddenEntity, speed);
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (mc.player == null || mc.player.getRidingEntity() == null) {
            return; // No player or no entity being ridden
        }

        // Only cancel fall damage for the ridden entity
        if (event.getEntity() == mc.player.getRidingEntity()) {
            event.setCanceled(true); // Cancel fall damage event for the ridden entity
        }
    }

    private boolean isControllableEntity(Entity entity) {
        return entity instanceof AbstractHorse || entity instanceof EntityPig;
    }

    private void handleEntityMovement(Entity entity, double speed) {
        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();

        if (forward || back || left || right) {
            float yaw = entity.rotationYaw;

            if (left && forward) yaw -= 45.0F;
            else if (right && forward) yaw += 45.0F;
            else if (left && back) yaw -= 135.0F;
            else if (right && back) yaw += 135.0F;
            else if (left) yaw -= 90.0F;
            else if (right) yaw += 90.0F;
            else if (back) yaw += 180.0F;

            double rad = Math.toRadians(yaw);
            entity.motionX = -MathHelper.sin((float) rad) * speed;
            entity.motionZ = MathHelper.cos((float) rad) * speed;
        } else {
            entity.motionX = 0;
            entity.motionZ = 0;
        }
    }

    private void applyStepHeightWithDelay(Entity entity, double stepHeight, long stepDelay) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStepTime >= stepDelay) {
            entity.stepHeight = (float) stepHeight;
            lastStepTime = currentTime;
        }
    }

    private void enableLiquidWalking(Entity entity) {
        BlockPos entityPos = new BlockPos(entity.posX, entity.posY, entity.posZ);

        if (mc.world.getBlockState(entityPos).getMaterial().isLiquid() ||
                mc.world.getBlockState(entityPos.down()).getMaterial().isLiquid()) {
            entity.motionY = 0.1; // Slowly rise upwards
        }

        if (entity.posY < mc.world.getTopSolidOrLiquidBlock(entityPos).getY()) {
            entity.motionY = 0.1; // Continue rising until on the surface
        }
    }

    private void trickFallDamage(Entity entity) {
        entity.fallDistance = 0.0f; // This resets the fall distance constantly
    }

    private void handleEdgeTraversal(Entity entity, double speed) {
        if (entity.motionX == 0 && entity.motionZ == 0 && mc.gameSettings.keyBindForward.isKeyDown()) {
            entity.motionY = 0.1;
            entity.motionX = -MathHelper.sin(entity.rotationYaw * (float) Math.PI / 180.0F) * speed;
            entity.motionZ = MathHelper.cos(entity.rotationYaw * (float) Math.PI / 180.0F) * speed;
        }
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (mc.player != null && mc.player.getRidingEntity() != null) {
            mc.player.getRidingEntity().stepHeight = 0.6f; // Reset step height to default
        }
    }
}
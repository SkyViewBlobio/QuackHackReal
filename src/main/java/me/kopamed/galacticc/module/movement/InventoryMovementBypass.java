package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
//todo fix the movement
public class InventoryMovementBypass extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public InventoryMovementBypass() {
        super("InventoryMovementBypass", "Allows you to move while any inventory is open.", true, false, Category.BEWEGUNG);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mc.currentScreen != null) {
            // Ignore certain screens like chat and pause menu
            if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) {
                return;
            }

            // Enable movement when a GUI container is open
            if (mc.currentScreen instanceof GuiContainer) {
                handleMovement();
                handleJumping();
                handleRotation();
            }
        }
    }

    private void handleMovement() {
        mc.thePlayer.movementInput.moveForward = mc.gameSettings.keyBindForward.isKeyDown() ? 1.0F : 0.0F;
        mc.thePlayer.movementInput.moveStrafe = (mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0F : 0.0F)
                - (mc.gameSettings.keyBindRight.isKeyDown() ? 1.0F : 0.0F);
    }

    private void handleJumping() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.jump();
        }
    }

    private void handleRotation() {
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            mc.thePlayer.rotationPitch = Math.max(mc.thePlayer.rotationPitch - 1.0F, -90.0F);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            mc.thePlayer.rotationPitch = Math.min(mc.thePlayer.rotationPitch + 1.0F, 90.0F);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            mc.thePlayer.rotationYaw -= 1.0F;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            mc.thePlayer.rotationYaw += 1.0F;
        }
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        // Adjust slipperiness for ice blocks
        Blocks.ice.slipperiness = 0.6F;
    }

    @Override
    public void onDisabled() {
        super.onDisabled();

        // Reset key states
        resetKeyBinding(mc.gameSettings.keyBindForward);
        resetKeyBinding(mc.gameSettings.keyBindBack);
        resetKeyBinding(mc.gameSettings.keyBindLeft);
        resetKeyBinding(mc.gameSettings.keyBindRight);
        resetKeyBinding(mc.gameSettings.keyBindJump);
        resetKeyBinding(mc.gameSettings.keyBindSprint);

        // Reset slipperiness
        Blocks.ice.slipperiness = 0.98F;
    }

    private void resetKeyBinding(KeyBinding keyBinding) {
        KeyBinding.setKeyBindState(keyBinding.getKeyCode(), false);
    }
}

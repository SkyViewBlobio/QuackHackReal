package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

public class InventoryMovementBypass extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static Field isKeyBindPressed;

    public InventoryMovementBypass() {
        super("InventoryMovementBypass", "Allows you to move while any inventory is open.", true, false, Category.BEWEGUNG);
    }

    static {
        try {
            isKeyBindPressed = KeyBinding.class.getDeclaredField("pressed"); // Obfuscated field name may differ
            isKeyBindPressed.setAccessible(true);
            System.out.println("Successfully accessed `pressed` field in KeyBinding.");
        } catch (NoSuchFieldException e) {
            System.err.println("Field `pressed` not found in KeyBinding. Obfuscated field name may differ.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error accessing `pressed` field.");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.currentScreen != null) {
            // Ignore certain screens like chat and pause menu
            if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) {
                return;
            }

            // Allow movement in inventory or other GUI screens
            if (mc.currentScreen instanceof GuiContainer) {
                allowMovement();
            }
        }
    }

    private void allowMovement() {
        try {
            // Set keybind states manually for movement keys
            setKeyState(mc.gameSettings.keyBindForward, Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            setKeyState(mc.gameSettings.keyBindBack, Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            setKeyState(mc.gameSettings.keyBindLeft, Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            setKeyState(mc.gameSettings.keyBindRight, Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            setKeyState(mc.gameSettings.keyBindJump, Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            setKeyState(mc.gameSettings.keyBindSprint, Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setKeyState(KeyBinding keyBinding, boolean state) throws IllegalAccessException {
        isKeyBindPressed.setBoolean(keyBinding, state);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();

        // Reset key states to prevent lingering inputs
        try {
            resetKeyState(mc.gameSettings.keyBindForward);
            resetKeyState(mc.gameSettings.keyBindBack);
            resetKeyState(mc.gameSettings.keyBindLeft);
            resetKeyState(mc.gameSettings.keyBindRight);
            resetKeyState(mc.gameSettings.keyBindJump);
            resetKeyState(mc.gameSettings.keyBindSprint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetKeyState(KeyBinding keyBinding) throws IllegalAccessException {
        isKeyBindPressed.setBoolean(keyBinding, false);
    }
}

package me.kopamed.galacticc.module.movement;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.events.Network.EntityUseItemEvent;
import me.kopamed.galacticc.events.Network.PacketEventCos;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class NoSlowModule extends Module {

    private boolean isSneaking = false;
    private long milliseconds = -1;
    private long ticks = -1;

    // Timer Format Enum
    public enum Format {
        MILLISECONDS,
        SECONDS,
        TICKS
    }

    private static final KeyBinding[] KEYS = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSprint
    };


    public NoSlowModule() {
        super("NoSlow", "Doesn't slow you down", false, false, Category.BEWEGUNG);

        // Anticheat settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Strict", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("AirStrict", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("GroundStrict", this, false));

        // Inventory movement settings
        Galacticc.instance.settingsManager.rSetting(new Setting("InventoryMove", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("ArrowLook", this, 0.0F, 0.0F, 10.0F, false));

        // Slowdown removal settings
        Galacticc.instance.settingsManager.rSetting(new Setting("Items", this, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("SoulSand", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Slime", this, false));
        Galacticc.instance.settingsManager.rSetting(new Setting("Ice", this, true));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        if ((boolean) Galacticc.instance.settingsManager.getSettingByName(this, "Ice").getValBoolean()) {
            Blocks.ICE.setDefaultSlipperiness(0.6F);
            Blocks.PACKED_ICE.setDefaultSlipperiness(0.6F);
            Blocks.FROSTED_ICE.setDefaultSlipperiness(0.6F);
        }
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
        if (isSneaking && (boolean) Galacticc.instance.settingsManager.getSettingByName(this, "AirStrict").getValBoolean()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        isSneaking = false;

        for (KeyBinding binding : KEYS) {
            binding.setKeyConflictContext(KeyConflictContext.IN_GAME);
        }

        if ((boolean) Galacticc.instance.settingsManager.getSettingByName(this, "Ice").getValBoolean()) {
            Blocks.ICE.setDefaultSlipperiness(0.98F);
            Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98F);
            Blocks.PACKED_ICE.setDefaultSlipperiness(0.98F);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEventCos.PacketSendEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();

            if (isSlowed()) {
                if ((boolean) Galacticc.instance.settingsManager.getSettingByName(this, "Strict").getValBoolean()) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                }

                if ((boolean) Galacticc.instance.settingsManager.getSettingByName(this, "GroundStrict").getValBoolean()
                        && getOnGround(packet)) {

                    if (passedTime(2, Format.TICKS)) {
                        setY(packet, mc.player.posY + 0.05);
                        resetTime();
                    }

                    setOnGround(packet, false);
                }
            }
        }
    }

    // Helper method to get the onGround field using ObfuscationReflectionHelper
    private boolean getOnGround(CPacketPlayer packet) {
        return ObfuscationReflectionHelper.getPrivateValue(CPacketPlayer.class, packet, "field_149474_g");
    }

    // Helper method to set the onGround field using ObfuscationReflectionHelper
    private void setOnGround(CPacketPlayer packet, boolean onGround) {
        ObfuscationReflectionHelper.setPrivateValue(CPacketPlayer.class, packet, onGround, "field_149474_g");
    }

    // Helper method to set the Y position using ObfuscationReflectionHelper
    private void setY(CPacketPlayer packet, double y) {
        ObfuscationReflectionHelper.setPrivateValue(CPacketPlayer.class, packet, y, "field_149479_a");
    }


    @SubscribeEvent
    public void onItemUse(EntityUseItemEvent event) {
        if (isSlowed() && (boolean) Galacticc.instance.settingsManager.getSettingByName(this, "AirStrict").getValBoolean() && !isSneaking) {
            isSneaking = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
    }

    private boolean isSlowed() {
        return mc.player.isHandActive()
                && (boolean) Galacticc.instance.settingsManager.getSettingByName(this, "Items").getValBoolean()
                && !mc.player.isRiding()
                && !mc.player.isElytraFlying();
    }

    private boolean isInScreen() {
        return mc.currentScreen != null
                && !(mc.currentScreen instanceof GuiChat
                || mc.currentScreen instanceof GuiEditSign
                || mc.currentScreen instanceof GuiRepair);
    }

    // Timer methods integrated into NoSlowModule
    private boolean passedTime(long time, Format format) {
        switch (format) {
            case MILLISECONDS:
            default:
                return (System.currentTimeMillis() - milliseconds) >= time;
            case SECONDS:
                return (System.currentTimeMillis() - milliseconds) >= (time * 1000);
            case TICKS:
                return ticks >= time;
        }
    }

    private long getMilliseconds() {
        if (milliseconds <= 0) {
            return 0;
        }
        return System.currentTimeMillis() - milliseconds;
    }

    private void setTime(long in, Format format) {
        switch (format) {
            case MILLISECONDS:
            default:
                milliseconds = System.currentTimeMillis() - in;
                break;
            case SECONDS:
                milliseconds = System.currentTimeMillis() - (in * 1000);
                break;
            case TICKS:
                ticks = in;
                break;
        }
    }

    private void resetTime() {
        milliseconds = System.currentTimeMillis();
        ticks = 0;
    }
}

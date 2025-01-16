package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.events.PacketEvent;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class PacketCriticals extends Module {
    private long lastPacketTime = 0;

    // Cached setting values
    private String cachedMode = "";
    private int cachedDelay = 0;
    private int cachedPackets = 0;

    public PacketCriticals() {
        super("PacketCriticals", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Laesst dich mehr schaden pro Schlag erzielen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Optionen:|" + ChatFormatting.RED +
                        "- Modus: " + ChatFormatting.WHITE +
                        "Waehle einen Modus, der dir am liebsten ist." + ChatFormatting.RED +
                        "- Packet: " + ChatFormatting.WHITE +
                        "Verwendet einfache Paket-basierte kritische Treffer,| klappt immer." + ChatFormatting.RED +
                        "- NCP-Bypass: " + ChatFormatting.WHITE +
                        "Umgeht den NCP-Detektor auf NPC-Servern|" + ChatFormatting.RED +
                        "- BlocksMC: " + ChatFormatting.WHITE +
                        "Umgeht den BlocksMC-Detektor auf BlocksMC-Servern." + ChatFormatting.RED +
                        "- CriticalSweep: " + ChatFormatting.WHITE +
                        "Wechselt zwischen Multi/Kritische Treffer." + ChatFormatting.RED +
                        "- Jump: " + ChatFormatting.WHITE +
                        "Laesst den Spieler springen, um den kritischen Treffer| zu aktivieren." + ChatFormatting.RED +
                        "- MiniJump: " + ChatFormatting.WHITE +
                        "Laesst den Spieler nur eine geringe Sprunghoehe erreichen." +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Nutzungsinformation:|" + ChatFormatting.WHITE +
                        "Zwar erzielst du mit diesem Modul| mehr Schaden, kannst aber nur einen Gegner auf distanz halten.",
                true, false, Category.ANGRIFF);

        ArrayList<String> modes = new ArrayList<>();
        modes.add("Packet");
        modes.add("NCP-Bypass");
        modes.add("BlocksMC");
        modes.add("CriticalSweep");
        modes.add("Jump");
        modes.add("MiniJump");

        Galacticc.instance.settingsManager.rSetting(new Setting("Mode", this, "Packet", modes));
        Galacticc.instance.settingsManager.rSetting(new Setting("Delay", this, 5, 5, 500, true));
        Galacticc.instance.settingsManager.rSetting(new Setting("Packets", this, 1, 1, 5, true));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent event) {
        if (event.getTime() != PacketEvent.Time.Send || !(event.getPacket() instanceof CPacketUseEntity)) return;

        CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
        if (packet.getAction() != CPacketUseEntity.Action.ATTACK) return;

        if (!isValidAttack()) return;

        updateSettings();

        if (!canSendPacket(cachedDelay)) return;

        switch (cachedMode) {
            case "Packet":
                performPacketCritical();
                break;
            case "NCP-Bypass":
                performNCPPacketCritical();
                break;
            case "BlocksMC":
                performBlocksMCCritical();
                break;
            case "CriticalSweep":
                performBlocksMC2Critical();
                break;
            case "Jump":
                performJumpCritical();
                break;
            case "MiniJump":
                performMiniJumpCritical();
                break;
        }

        lastPacketTime = System.currentTimeMillis();
    }

    private boolean isValidAttack() {
        return mc.player != null &&
                mc.player.onGround &&
                !mc.gameSettings.keyBindJump.isKeyDown() &&
                mc.player.isEntityAlive() &&
                !mc.player.isInWater() &&
                !mc.player.isInLava();
    }

    private boolean canSendPacket(int delay) {
        return System.currentTimeMillis() - lastPacketTime >= delay;
    }

    private void updateSettings() {
        String newMode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
        int newDelay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Delay").getValDouble();
        int newPackets = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Packets").getValDouble();

        if (!newMode.equals(cachedMode)) {
            cachedMode = newMode;
        }
        if (newDelay != cachedDelay) {
            cachedDelay = newDelay;
        }
        if (newPackets != cachedPackets) {
            cachedPackets = newPackets;
        }
    }

    // Critical Modes Logic
    private void performPacketCritical() {
        sendPackets(0.0625, 0.0);
    }

    private void performNCPPacketCritical() {
        sendPackets(0.11, 0.1100013579, 0.0000013579);
    }

    private void performBlocksMCCritical() {
        sendPackets(0.0011, 0.0);
    }

    private void performBlocksMC2Critical() {
        if (mc.player.ticksExisted % 4 == 0) {
            sendPackets(0.0011, 0.0);
        }
    }

    private void performJumpCritical() {
        if (mc.player.onGround) {
            mc.player.jump();
        }
    }

    private void performMiniJumpCritical() {
        if (mc.player.onGround) {
            mc.player.motionY = 0.1;
            mc.player.fallDistance = 0.1f;
        }
    }

    // Utility Method to Send Packets
    private void sendPackets(double... offsets) {
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        for (double offset : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + offset, z, false));
        }
    }
}
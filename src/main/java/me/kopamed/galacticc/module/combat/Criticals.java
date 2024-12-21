package me.kopamed.galacticc.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class Criticals extends Module {
    private long lastPacketTime = 0;

    public Criticals() {
        super("Criticals", "@HauptInformation: " +
                        "Ermoeglicht dir, kritische Treffer zu landen, ohne normal springen zu muessen. " +
                        "@Optionen: " +
                        "- Mode (Klick auf mode um den Modus zu wechseln). || " +
                        "- Packet (Sendet bestimmte Pakete, um kritische Treffer zu simulieren, ohne zu springen). Dieser Modus wird weiterhin Kritische Treffer erzielen selbst wenn du mit Schwaeche verzaubert bist. || " +
                        "- NCP-Bypass (Verwendet NCP-kompatible Pakete, um kritische Treffer zu erzeugen). || " +
                        "- BlocksMC (Simuliert kritische Treffer, angepasst an den BlocksMC-Server). || " +
                        "- BlocksMC2 (Alternativer Modus für BlocksMC, mit einem zeitlichen Abstand). || " +
                        "- NoGround (Setzt den Spieler kurzzeitig in einen *Nicht auf dem Boden* -Zustand, um kritische Treffer zu ermoeglichen). || " +
                        "- Jump (Springt, um einen regulaeren kritischen Treffer zu erzielen). || " +
                        "- MiniJump (Fuehrt einen kleinen Sprung aus, um den kritischen Treffer zu erzeugen, ohne vollstaendig zu springen). || " +
                        "- Delay (Verzoegerung in Millisekunden zwischen den kritischen Treffern, um das Spammen von Paketen zu vermeiden).",
                false, false, Category.ANGRIFF);

        ArrayList<String> modes = new ArrayList<>();
        modes.add("Packet");
        modes.add("NCP-Bypass");
        modes.add("BlocksMC");
        modes.add("BlocksMC2");
        modes.add("NoGround");
        modes.add("Jump");
        modes.add("MiniJump");

        Galacticc.instance.settingsManager.rSetting(new Setting("Mode", this, "Packet", modes));
        Galacticc.instance.settingsManager.rSetting(new Setting("Delay", this, 5, 5, 500, true));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || mc.player == null || mc.world == null) return;

        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
        int delay = (int) Galacticc.instance.settingsManager.getSettingByName(this, "Delay").getValDouble();

        if (mc.player.onGround && mc.player.isSwingInProgress && canSendPacket(delay)) {
            switch (mode) {
                case "Packet":
                    performPacketCritical();
                    break;
                case "NCP-Bypass":
                    performNCPPacketCritical();
                    break;
                case "BlocksMC":
                    performBlocksMCCritical();
                    break;
                case "BlocksMC2":
                    performBlocksMC2Critical();
                    break;
                case "NoGround":
                    performNoGroundCritical();
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
    }

    private boolean canSendPacket(int delay) {
        return System.currentTimeMillis() - lastPacketTime >= delay;
    }

    private void performPacketCritical() {
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.0625, z, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
    }

    private void performNCPPacketCritical() {
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.11, z, false));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.1100013579, z, false));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.0000013579, z, false));
    }

    private void performBlocksMCCritical() {
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.0011, z, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
    }

    private void performBlocksMC2Critical() {
        if (mc.player.ticksExisted % 4 == 0) {
            double x = mc.player.posX;
            double y = mc.player.posY;
            double z = mc.player.posZ;

            mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + 0.0011, z, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
        }
    }

    private void performNoGroundCritical() {
        mc.player.connection.sendPacket(new CPacketPlayer(false));
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

    @Override
    public String getHUDInfo() {
        String mode = Galacticc.instance.settingsManager.getSettingByName(this, "Mode").getValString();
        return ChatFormatting.GRAY + "[" + mode + "]";
    }
}

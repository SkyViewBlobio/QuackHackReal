package me.kopamed.galacticc.module.textstuff;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class ChangelogDisplay extends Module {

    private boolean hasDisplayed = false;
    private long joinTime = 0;
    private static final String CLIENT_VERSION = Galacticc.VERSION;

    public ChangelogDisplay() {
        super("VersionUpdate-Note", "" +
                        ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                        "Zeigt was sich im Update verhaendert hat| in form einer Nachricht beim Betreten einer Welt oder eines Servers.",
                false, false, Category.TEXTSTUFF);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        hasDisplayed = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) {
            joinTime = 0;
            return;
        }

        if (joinTime == 0) {
            joinTime = System.currentTimeMillis();
        }

        if (!hasDisplayed && System.currentTimeMillis() - joinTime >= 10000) {
            displayChangelog();
            hasDisplayed = true;
        }
    }

    private void displayChangelog() {
        String version = TextFormatting.DARK_PURPLE + "QuackHack" + CLIENT_VERSION;
        String title = TextFormatting.RED + "Change-log";
        String[] changelog = {
                TextFormatting.WHITE + "- AutoCrystal der automatisch Kristalle in der nähe von Spielern platziert und explodiert.",
                TextFormatting.WHITE + "- Leistung und code verbersserungen.",
                TextFormatting.WHITE + "- Neue einordung von RandomChatMessages von Sonstiges > TextStuff.",
                TextFormatting.WHITE + "- VersionUpdateNote die dir anzeigt was neu ist hinzugefügt.",
                TextFormatting.WHITE + "- BlitzTotem unter Visuelles hinzugefügt.",
                TextFormatting.WHITE + "- AutomaticBridgeBuilder hat einen 5-block Modus bekommen."

        };

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(version));
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(title));
        for (String line : changelog) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(line));
        }

        playChangelogSound();
    }

    private void playChangelogSound() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player != null && mc.world != null) {
            mc.getSoundHandler().playSound(
                    PositionedSoundRecord.getMasterRecord(Galacticc.CHANGELOG_SOUND, 1.0F)
            );
        }
    }
}
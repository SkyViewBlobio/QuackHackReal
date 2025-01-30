package me.kopamed.galacticc.module.textstuff;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomChatMessages extends Module {

    private final List<String> messages = new ArrayList<>();
    private long lastMessageTime = 0;
    private final File messagesFile = new File(System.getenv("APPDATA") + "\\.minecraft\\quackhack\\messages.txt");

    public RandomChatMessages() {
        super("RandomChatMessages", "" +
                ChatFormatting.BLUE + ChatFormatting.BOLD + ChatFormatting.UNDERLINE + "Hauptinformation:|" + ChatFormatting.WHITE +
                "Zeigt dir irgendwelche Nachrichten im Chat" , false , false, Category.TEXTSTUFF);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        loadMessagesFromFile();
    }

    @Override
    public void onDisabled() {
        super.onDisabled();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player == null) return;

        long currentTime = System.currentTimeMillis();
        // 5 seconds interval
        int interval = 5000;
        if (currentTime - lastMessageTime >= interval) {
            sendRandomMessage();
            lastMessageTime = currentTime;
        }
    }

    private void loadMessagesFromFile() {
        messages.clear();

        // Check if the file exists, create it if not
        if (!messagesFile.exists()) {
            try {
                messagesFile.getParentFile().mkdirs(); // Create parent folders if they don't exist
                messagesFile.createNewFile();
                System.out.println("Created new messages file at: " + messagesFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // Read the messages from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(messagesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    messages.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRandomMessage() {
        if (messages.isEmpty()) return;

        // Get a random message
        String randomMessage = messages.get(new Random().nextInt(messages.size()));

        // Apply colorization logic
        String coloredMessage = colorizeMessage(randomMessage);

        // Display the message in Minecraft chat
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(coloredMessage));
    }

    private String colorizeMessage(String message) {
        // Define patterns for specific message formats
        String[] patterns = {
                "&3%NAME% &4was violently assassinated by &3%NAME% &4with an &6end crystal",
                "&3%NAME% &4was removed by &3%NAME% &4with an &6end crystal",
                "&3%NAME% &4was turned into red dust by &3%NAME% &4with an &6end crystal",
                "&3%NAME% &4was utterly destroyed by &3%NAME% &4with an &6end crystal",
                "&3%NAME% &4was blown up by &3%NAME% &4with an &6end crystal",
                "&3%NAME% &4was destroyed by &3%NAME% &4with an &6end crystal"
        };

        for (String pattern : patterns) {
            // Replace %NAME% placeholders with regex groups
            String regex = pattern.replace("%NAME%", "(.*?)");
            Pattern compiledPattern = Pattern.compile(regex);

            Matcher matcher = compiledPattern.matcher(message);
            if (matcher.matches()) {
                // Extract names from the message
                String name1 = matcher.group(1);
                String name2 = matcher.group(2);

                // Replace placeholders and apply Minecraft color codes
                return pattern.replace("&3", "§3")
                        .replace("&4", "§4")
                        .replace("&6", "§6")
                        .replaceFirst("%NAME%", name1)
                        .replaceFirst("%NAME%", name2);
            }
        }

        // If no pattern matched, return the original message
        return message;
    }
}

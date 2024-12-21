package me.kopamed.galacticc.command;

import me.kopamed.galacticc.Galacticc;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public abstract class Command {

    public abstract void execute(String[] args);

    public abstract String getName();
    public abstract String getSyntax();
    public abstract String getDesc();

    public static void msg(String msg) {
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(Galacticc.prefix + "§7 " + msg));
    }

    public void normal(String msg) {
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(msg));
    }

    public String getCmd() {
        return getName();
    }

    public String getName1() {
        return getName();
    }

    public String getHelp() {
        return null;
    }
}

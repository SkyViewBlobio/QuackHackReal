package me.kopamed.galacticc.config;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.clickgui.component.Frame;
import me.kopamed.galacticc.module.Category;
import me.kopamed.galacticc.module.Module;
import me.kopamed.galacticc.settings.Setting;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;

public class SaveLoad {
    private File dir;
    private File dataFile;
    private String extension, fileName;

    public SaveLoad() {
        dir = new File(Minecraft.getMinecraft().mcDataDir, Galacticc.MODID.toLowerCase());
        if (!dir.exists()) {
            dir.mkdir();
        }
        this.fileName = "current";
        this.extension = "galaxy";
        dataFile = new File(dir, fileName + "." + extension);
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.load();
    }

    public void save() {
        if (Galacticc.instance.destructed) {
            return;
        }
        ArrayList<String> toSave = new ArrayList<>();

        for (Module mod : Galacticc.instance.moduleManager.getModulesList()) {
            toSave.add(Galacticc.MODID + "MOD:" + mod.getName() + ":" + mod.isToggled() + ":" + mod.visible + ":" + mod.getKey());
        }

        for (Setting set : Galacticc.instance.settingsManager.getSettings()) {
            if (set.isCheck()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValBoolean());
            }
            if (set.isCombo()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValString());
            }
            if (set.isSlider()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValDouble());
            }
        }

        for (Frame frame : Galacticc.instance.clickGui.getFrames()) {
            toSave.add(Galacticc.MODID + "FRAME:" + frame.getCategory().name() + ":" + frame.isOpen());
        }

        try {
            PrintWriter printWriter = new PrintWriter(this.dataFile);
            for (String str : toSave) {
                printWriter.println(str);
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save(String configname) {
        if (Galacticc.instance.destructed) {
            return;
        }
        ArrayList<String> toSave = new ArrayList<String>();

        for (Module mod : Galacticc.instance.moduleManager.getModulesList()) {
            toSave.add(Galacticc.MODID + "MOD:" + mod.getName() + ":" + mod.isToggled() + ":" + mod.visible + ":" + mod.getKey());
        }

        for (Setting set : Galacticc.instance.settingsManager.getSettings()) {
            if (set.isCheck()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValBoolean());
            }
            if (set.isCombo()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValString());
            }
            if (set.isSlider()) {
                toSave.add(Galacticc.MODID + "SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValDouble());
            }
        }

        try {
            PrintWriter printWriter = new PrintWriter(this.dataFile);
            for (String str : toSave) {
                printWriter.println(str);
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (Galacticc.instance.destructed) {
            return;
        }

        ArrayList<String> lines = new ArrayList<>();

        // Read the file safely
        try (BufferedReader reader = new BufferedReader(new FileReader(this.dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return; // Exit early if reading the file fails
        }

        if (lines.isEmpty()) {
            return;
        }

        for (String s : lines) {
            String[] args = s.split(":");
            try {
                if (s.toLowerCase().startsWith(Galacticc.MODID.toLowerCase() + "mod:")) {
                    // Handle module settings
                    Module m = Galacticc.instance.moduleManager.getModule(args[1]);
                    if (m != null) {
                        m.setToggled(Boolean.parseBoolean(args[2]));
                        m.visible = Boolean.parseBoolean(args[3]);
                        m.setKey(Integer.parseInt(args[4]));
                    }
                } else if (s.toLowerCase().startsWith(Galacticc.MODID.toLowerCase() + "set:")) {
                    // Handle individual settings
                    Module m = Galacticc.instance.moduleManager.getModule(args[2]);
                    if (m != null) {
                        Setting set = Galacticc.instance.settingsManager.getSettingByName(m, args[1]);
                        if (set != null) {
                            if (set.isCheck()) {
                                set.setValBoolean(Boolean.parseBoolean(args[3]));
                            }
                            if (set.isCombo()) {
                                set.setValString(args[3]);
                            }
                            if (set.isSlider()) {
                                try {
                                    double value = Double.parseDouble(args[3]);
                                    set.setValDouble(value);
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid slider value for setting: " + set.getName());
                                }
                            }
                        }
                    }
                } else if (s.toLowerCase().startsWith(Galacticc.MODID.toLowerCase() + "frame:")) {
                    // Handle frame settings (line 159 fix)
                    if (args.length > 2) { // Ensure sufficient arguments are present
                        try {
                            Category category = Category.valueOf(args[1].toUpperCase()); // Handle case insensitivity
                            Frame frame = Galacticc.instance.clickGui.getFrameByCategory(category);
                            if (frame != null) {
                                frame.setOpen(Boolean.parseBoolean(args[2]));
                            } else {
                                System.err.println("Frame not found for category: " + category);
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid category in frame line: " + args[1]);
                        }
                    } else {
                        System.err.println("Insufficient arguments for frame: " + s);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing line: " + s);
                e.printStackTrace();
            }
        }
    }


    public String getExtension() {
        return extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.dataFile = new File(dir, this.fileName + this.extension);
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getConfigs() {
        File[] files = new File(Galacticc.MODID.toLowerCase() + "/Configs/").listFiles();
        ArrayList<String> results = new ArrayList<String>();

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }
}

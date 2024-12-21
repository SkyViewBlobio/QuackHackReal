package me.kopamed.galacticc.module;

import me.kopamed.galacticc.Galacticc;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class Module {
    protected static Minecraft mc = Minecraft.getMinecraft();

    private String name, description;
    private int key;
    private boolean detectable, toggled;
    public Category category;
    public boolean visible = true;

    // Global flag to control HUD info visibility
    public static boolean hideHUDInfo = false;

    public Module(String name, String description, boolean detectable, boolean toggled, Category category) {
        this.name = name;
        this.description = description;
        this.key = 0;
        this.detectable = detectable;
        this.toggled = toggled;
        this.category = category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
        if (Galacticc.instance.saveLoad != null) {
            Galacticc.instance.saveLoad.save();
        }
    }

    public boolean isDetectable() {
        return detectable;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void onLoad() {
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;

        if (this.toggled) {
            this.onEnabled();
        } else {
            this.onDisabled();
        }

        if (Galacticc.instance.saveLoad != null) {
            Galacticc.instance.saveLoad.save();
        }
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public void toggle() {
        this.toggled = !this.toggled;

        if (this.toggled) {
            this.onEnabled();
        } else {
            this.onDisabled();
        }

        if (Galacticc.instance.saveLoad != null) {
            Galacticc.instance.saveLoad.save();
        }
    }

    public void onUpdate() {
        // Default behavior, can be overridden
    }

    public void onRender3D() {
        // Default behavior, can be overridden
    }

    public void onEnabled() {
        System.out.println("Enabled " + this.name);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onDisabled() {
        System.out.println("Disabled" + this.name);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    /**
     * Provides extra HUD information for modules that implement it.
     * Automatically hides HUD info if `hideHUDInfo` is enabled globally.
     *
     * @return A string representing additional HUD info, or null if no extra info or hidden.
     */
    public String getHUDInfo() {
        if (hideHUDInfo) {
            return null; // Return null if HUD info is globally hidden
        }
        return null; // Override in individual modules as needed
    }

    @Override
    public String toString() {
        return name;
    }
}

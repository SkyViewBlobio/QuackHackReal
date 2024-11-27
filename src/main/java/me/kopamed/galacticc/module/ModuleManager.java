package me.kopamed.galacticc.module;

import jdk.nashorn.internal.ir.Block;
import me.kopamed.galacticc.module.combat.AutoClicker;
import me.kopamed.galacticc.module.combat.DelayRemover;
import me.kopamed.galacticc.module.combat.Killaura;
import me.kopamed.galacticc.module.combat.Velocity;
import me.kopamed.galacticc.module.movement.Sprint;
import me.kopamed.galacticc.module.player.FastPlace;
import me.kopamed.galacticc.module.render.*;
import me.kopamed.galacticc.module.textstuff.Informationen;
import me.kopamed.galacticc.module.textstuff.Watermark;

import java.util.ArrayList;

public class ModuleManager {
    public ArrayList<Module> modules;

    public ModuleManager(){
        (modules = new ArrayList<Module>()).clear();
        this.modules.add(new ClickGUI());
        this.modules.add(new HUD());
        this.modules.add(new EnchantModifier());
        this.modules.add(new ArmorDisplay());
        this.modules.add(new Sprint());
        this.modules.add(new AutoClicker());
        this.modules.add(new Velocity());
        this.modules.add(new Watermark());
        this.modules.add(new BlockHighlight());
        //this.modules.add(new Fly());
        //this.modules.add(new NoFall());
        this.modules.add(new Fullbright());
        //this.modules.add(new Speed());
        //this.modules.add(new AntiBot());
        this.modules.add(new FastPlace());
        //this.modules.add(new SelfDestruct());
        this.modules.add(new DelayRemover());
        //this.modules.add(new Reach());
        //this.modules.add(new PlayerESP());
        this.modules.add(new Killaura());
        this.modules.add(new Informationen());
        this.modules.add(new CustomSkyColorAndFog());
    }

    public Module getModule(String name) {
        for (Module m : this.modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public ArrayList<Module> getModulesList() {
        return this.modules;
    }

    public ArrayList<Module> getModulesInCategory(Category c) {
        ArrayList<Module> mods = new ArrayList<Module>();
        for (Module m : this.modules) {
            if(m.getCategory() == c){
                mods.add(m);
            }
        }
        return mods;
    }

    public void addModule(Module m){
        this.modules.add(m);
    }
}
package me.kopamed.galacticc.module;

import me.kopamed.galacticc.module.combat.*;
import me.kopamed.galacticc.module.misc.AntiAFK;
import me.kopamed.galacticc.module.misc.AutoFish;
import me.kopamed.galacticc.module.misc.AutomaticCreeperLighter;
import me.kopamed.galacticc.module.misc.RandomChatMessages;
import me.kopamed.galacticc.module.mod.cKontrolle;
import me.kopamed.galacticc.module.movement.*;
import me.kopamed.galacticc.module.player.AutoObsidianMiner;
import me.kopamed.galacticc.module.player.AutomaticBridgeBuilder;
import me.kopamed.galacticc.module.player.FastPlace;
import me.kopamed.galacticc.module.player.Reach2;
import me.kopamed.galacticc.module.render.*;
import me.kopamed.galacticc.module.textstuff.Informationen;
import me.kopamed.galacticc.module.textstuff.Watermark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    public ArrayList<Module> modules;
    private final Map<Module, Boolean> moduleOpenStates = new HashMap<>();

    public ModuleManager() {
        (modules = new ArrayList<Module>()).clear();

        //************************COMBAT**************************//

        this.modules.add(new Velocity());
        this.modules.add(new DelayRemover());
        this.modules.add(new Reach());
        this.modules.add(new Killaurarewrite());
        this.modules.add(new AutomaticTotemOffhand());
        this.modules.add(new PacketCriticals());
        this.modules.add(new CrystalAttackModule());
        //this.modules.add(new OneShotArrowModule());
        // need to fix autostart. this.modules.add(new AutoClicker());

        //************************Mod**************************//
        this.modules.add(new cKontrolle());

        //************************MISC**************************//
        this.modules.add(new AutoFish());
        this.modules.add(new AntiAFK());
        this.modules.add(new RandomChatMessages());
        this.modules.add(new AutomaticCreeperLighter());

        //************************MOVEMENT**************************//

        this.modules.add(new Sprint());
        this.modules.add(new Step());
        this.modules.add(new GlideJump());
        this.modules.add(new NoFall());
        this.modules.add(new InventoryMovementBypass());
        this.modules.add(new ElytraFly());
        this.modules.add(new WaterWalker());
        this.modules.add(new EntityControl());
        this.modules.add(new NoSlowModule());
        this.modules.add(new EatingSpeedBoostModule());

        //************************PLAYER**************************//

        this.modules.add(new FastPlace());
        this.modules.add(new AutomaticBridgeBuilder());
        this.modules.add(new Reach2());
        this.modules.add(new AutoObsidianMiner());

        //************************RENDER**************************//

        this.modules.add(new ClickGUI());
        this.modules.add(new HUD());
        this.modules.add(new ArmorDisplay());
        this.modules.add(new BlockHighlight());
        this.modules.add(new Fullbright());
        this.modules.add(new CustomSkyColorAndFog());
        this.modules.add(new SelfParticle());
        this.modules.add(new AttackParticle());
        this.modules.add(new InventoryDisplay());
        this.modules.add(new FadeWalker());
        this.modules.add(new StorageESP());
        this.modules.add(new HoleESP());
        this.modules.add(new ShulkerPreview());
        this.modules.add(new FakePlayer());
        this.modules.add(new ItemESP());
        this.modules.add(new NameTagHighlight());
        this.modules.add(new ESP());

        //************************TEXT STUFF**************************//

        this.modules.add(new Watermark());
        this.modules.add(new Informationen());

        //************************ BS **************************//

        //this.modules.add(new NoFall());
        //this.modules.add(new AntiBot()); marked for removal
        //this.modules.add(new SelfDestruct());
        this.modules.add(new PlayerESP());

        //************************OLD*****************//
        //this.modules.add(new Killaura()); keep in use?

        // Initialize open states
        for (Module module : modules) {
            moduleOpenStates.put(module, false); // Default to folded
        }

        // Call onLoad for each module
        for (Module module : modules) {
            module.onLoad(); // Trigger module-specific initialization logic
        }
    }

/**
 * Retrieve the open state of a module.
 *
 * @param module The module to check.
 * @return true if the module is open, false if folded.
 */
public boolean isModuleOpen(Module module) {
    return moduleOpenStates.getOrDefault(module, false); // Default to folded
}

/**
 * Set the open state of a module.
 *
 * @param module The module to update.
 * @param isOpen The new state: true for open, false for folded.
 */
public void setModuleOpen(Module module, boolean isOpen) {
    moduleOpenStates.put(module, isOpen);
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
        if (m.getCategory() == c) {
            mods.add(m);
        }
    }
    return mods;
}

public void addModule(Module m) {
    this.modules.add(m);
    moduleOpenStates.put(m, false); // Initialize with default folded state
}
}
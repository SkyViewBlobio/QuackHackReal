package me.kopamed.galacticc.utils;

import me.kopamed.galacticc.Galacticc;
import me.kopamed.galacticc.module.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Arch {

    // Makes a shallow copy of a list
    public static List<Module> copyModules(List<Module> parent) {
        return new ArrayList<>(parent);
    }

    // Sorts modules by name length in descending order
    public static List<Module> sortByNameLengthDescending() {
        List<Module> modules = copyModules(Galacticc.instance.moduleManager.getModulesList());
        modules.sort((m1, m2) -> Integer.compare(m2.getName().length(), m1.getName().length()));
        return modules;
    }

    // Sorts modules by name length in ascending order
    public static List<Module> sortByNameLengthAscending() {
        List<Module> modules = sortByNameLengthDescending();
        Collections.reverse(modules);
        return modules;
    }

    // Sorts modules alphabetically by name
    public static List<Module> sortAlphabetically() {
        List<Module> modules = copyModules(Galacticc.instance.moduleManager.getModulesList());
        modules.sort(Comparator.comparing(m -> m.getName().toLowerCase()));
        return modules;
    }

    // Gets a list of module names (utility method)
    public static List<String> getModuleNames() {
        List<String> moduleNames = new ArrayList<>();
        for (Module module : Galacticc.instance.moduleManager.getModulesList()) {
            moduleNames.add(module.getName());
        }
        return moduleNames;
    }
}

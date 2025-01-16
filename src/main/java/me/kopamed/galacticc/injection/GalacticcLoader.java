package me.kopamed.galacticc.injection;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class GalacticcLoader implements IFMLLoadingPlugin {
    private static final Logger logger = LogManager.getLogger(GalacticcLoader.class);

    public GalacticcLoader() {
        logger.info("--- Galacticc loader entrypoint ---");

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.galacticc.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        logger.info("Mixin system initialized");

    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {}

    public String getAccessTransformerClass() {
        return null;
    }
}
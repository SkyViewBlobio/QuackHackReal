import me.kopamed.galacticc.Galacticc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

@Mod(modid = Galacticc.MODID, version = Galacticc.VERSION)
public class Main {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Galacticc.instance = new Galacticc();
        Galacticc.instance.init();
    }
}

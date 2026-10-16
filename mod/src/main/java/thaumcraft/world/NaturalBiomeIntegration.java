package thaumcraft.world;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class NaturalBiomeIntegration {
    /** Loader metadata is available before biome registries are decoded. No optional API is linked here. */
    public static boolean terraBlenderPresent(){
        var mods=FMLLoader.getLoadingModList();
        return mods!=null&&mods.getMods().stream().anyMatch(mod->mod.getModId().equals("terrablender"));
    }
    public static void register(IEventBus bus){
        bus.addListener((FMLCommonSetupEvent event)->event.enqueueWork(()->{
            if(terraBlenderPresent())TerraBlenderBiomes.register();
            thaumcraft.Thaumcraft.LOGGER.info("Natural TC4 biomes use {}",terraBlenderPresent()?"TerraBlender":"climate preset Mixin");
        }));
    }
    private NaturalBiomeIntegration(){}
}

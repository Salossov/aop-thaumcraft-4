package thaumcraft.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.world.*;

@Mixin(MultiNoiseBiomeSourceParameterList.class)
public abstract class NaturalBiomePresetMixin {
    @Shadow @Final @Mutable private Climate.ParameterList<Holder<Biome>> parameters;
    @Inject(method="<init>",at=@At("RETURN"))
    private void thaumcraft$naturalBiomes(MultiNoiseBiomeSourceParameterList.Preset preset,HolderGetter<Biome> biomes,CallbackInfo ci){
        if(preset!=MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD||NaturalBiomeIntegration.terraBlenderPresent())return;
        // UniversalLookup manufactures a reference even for missing keys; querying it would
        // contaminate VanillaRegistries bootstrap with unbound mod biome references.
        if(biomes.getClass().getName().startsWith("net.minecraft.core.RegistrySetBuilder$"))return;
        // Vanilla bootstrap also creates presets without the mod datapack. Leave those untouched.
        if(biomes.get(WorldBiomes.MAGICAL).isEmpty()||biomes.get(WorldBiomes.TAINT).isEmpty())return;
        var result=new ArrayList<Pair<Climate.ParameterPoint,Holder<Biome>>>();
        for(var entry:parameters.values()){
            var key=entry.getSecond().unwrapKey();
            if(key.isEmpty()){result.add(entry);continue;}
            NaturalBiomeClimate.map(Pair.of(entry.getFirst(),key.get()),NaturalBiomeClimate.FALLBACK_VANILLA_WEIGHT,key.get(),
                mapped->result.add(Pair.of(mapped.getFirst(),biomes.getOrThrow(mapped.getSecond()))));
        }
        for(var dedicated:NaturalBiomeClimate.dedicated())
            result.add(Pair.of(dedicated.getFirst(),biomes.getOrThrow(dedicated.getSecond())));
        parameters=new Climate.ParameterList<>(result);
    }
}

package thaumcraft.api.wands;

import java.util.*;
import net.minecraft.resources.ResourceLocation;

/** Additional composite wand models, baked through the normal resource-pack model pipeline. */
public final class WandVisuals {
    public record Key(String kind,WandParts parts){}
    /** Packed lightmap UV coordinates; the local viewing player's whole tick count is supplied. */
    @FunctionalInterface public interface Lighting {int packedLight(int worldLight,int viewerTicks);}
    public record Layer(ResourceLocation model,Lighting lighting){public Layer{Objects.requireNonNull(model);}}
    private static final Map<Key,ResourceLocation> MODELS=new LinkedHashMap<>();
    private static final Map<Key,List<Layer>> LAYERS=new LinkedHashMap<>();
    public static void register(String kind,WandParts parts,ResourceLocation model){if(!Set.of("wand","staff","sceptre").contains(kind))throw new IllegalArgumentException("Unknown wand kind");if(MODELS.putIfAbsent(new Key(kind,parts),Objects.requireNonNull(model))!=null)throw new IllegalArgumentException("Duplicate wand model");}
    public static ResourceLocation model(String kind,WandParts parts){return MODELS.get(new Key(kind,parts));}
    /** Optional ordered passes over separately baked geometry. Null lighting retains world light. */
    public static void registerLayers(String kind,WandParts parts,List<Layer> layers){
        var key=new Key(kind,parts);if(!MODELS.containsKey(key))throw new IllegalArgumentException("Register the composite model first");
        if(layers.isEmpty()||LAYERS.putIfAbsent(key,List.copyOf(layers))!=null)throw new IllegalArgumentException("Empty or duplicate wand layers");
    }
    public static List<Layer> layers(String kind,WandParts parts){return LAYERS.getOrDefault(new Key(kind,parts),List.of());}
    public static Collection<ResourceLocation> models(){var all=new LinkedHashSet<>(MODELS.values());for(var layers:LAYERS.values())for(var layer:layers)all.add(layer.model());return List.copyOf(all);}
    private WandVisuals(){}
}

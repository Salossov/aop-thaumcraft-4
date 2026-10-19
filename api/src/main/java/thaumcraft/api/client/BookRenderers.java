package thaumcraft.api.client;

import java.util.*;
import net.minecraft.resources.ResourceLocation;

public final class BookRenderers {
    private static final Map<ResourceLocation,BookPageRenderer> VALUES=new HashMap<>();
    public static synchronized void register(ResourceLocation id,BookPageRenderer renderer){if(VALUES.putIfAbsent(id,Objects.requireNonNull(renderer))!=null)throw new IllegalArgumentException("Duplicate book renderer "+id);}
    public static BookPageRenderer get(String id){return VALUES.get(ResourceLocation.parse(id));}
    private BookRenderers(){}
}

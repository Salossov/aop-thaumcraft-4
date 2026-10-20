package thaumcraft.api.client;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

/** Client-only final lightmap pixel policy, selected by dimension effects key.
 * Values returned by a policy use NativeImage's ABGR integer representation.
 * Registration belongs to client setup; no world/client classes load on servers.
 */
public final class DimensionLightmap {
    public record Context(ClientLevel level,float partialTicks,float skyBrightness,float blockLightMultiplier,
                          boolean skyFlash,float darkenCurrent,float darkenInterpolated,float nightVision,float gamma){}
    @FunctionalInterface public interface Policy {int pixel(Context context,int blockLight,int skyLight,int vanillaAbgr);}
    private static final Map<ResourceLocation,Policy> POLICIES=new ConcurrentHashMap<>();
    public static void register(ResourceLocation effects,Policy policy){
        Objects.requireNonNull(effects);Objects.requireNonNull(policy);
        if(POLICIES.putIfAbsent(effects,policy)!=null)throw new IllegalStateException("Lightmap policy already registered: "+effects);
    }
    public static boolean hasPolicy(ResourceLocation effects){return POLICIES.containsKey(effects);}
    public static int pixel(Context context,int blockLight,int skyLight,int vanillaAbgr){
        var policy=POLICIES.get(context.level().dimensionType().effectsLocation());return policy==null?vanillaAbgr:policy.pixel(context,blockLight,skyLight,vanillaAbgr);
    }
    private DimensionLightmap(){}
}

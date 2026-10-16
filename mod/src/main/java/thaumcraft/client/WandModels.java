package thaumcraft.client;
import java.util.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import thaumcraft.items.*;
@EventBusSubscriber(modid="thaumcraft",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WandModels {
    private static final List<String> CAPS=List.of("iron","gold","thaumium","void","copper","silver");
    private static final List<String> RODS=List.of("wood","greatwood","obsidian","silverwood","ice","quartz","reed","blaze","bone");
    private static ModelResourceLocation variant(String kind,String rod,String cap){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft",kind+"/"+rod+"_"+cap));}
    private static ModelResourceLocation focusModel(String kind,ThaumicFocusItem.Kind focus){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft",kind+"/focus_"+focus.name().toLowerCase(Locale.ROOT)));}
    @SubscribeEvent public static void registerFocus(ModelEvent.RegisterAdditional event){for(var kind:List.of("wand","staff"))for(var focus:ThaumicFocusItem.Kind.values())event.register(focusModel(kind,focus));}
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event){for(var kind:List.of("wand","staff","sceptre"))for(var rod:RODS)for(var cap:CAPS){if(kind.equals("staff")&&rod.equals("wood"))continue;event.register(variant(kind,rod,cap));}for(var cap:CAPS)event.register(variant("staff","primal",cap));}
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event){
        var models=event.getModels();for(var kind:List.of("wand","staff","sceptre")){
            var id=new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft","casting_"+kind),"inventory");var original=models.get(id);if(original==null)continue;
            models.put(id,new BakedModelWrapper<BakedModel>(original){
                private final ItemOverrides overrides=new ItemOverrides(){
                    private final Map<String,BakedModel> cache=new HashMap<>();
                    @Override public BakedModel resolve(BakedModel model,ItemStack stack,ClientLevel level,LivingEntity entity,int seed){var parts=WandMaterials.parts(stack);var base=models.getOrDefault(variant(kind,parts.rod().replace("_staff",""),parts.cap()),original);var focus=WandFocus.get(stack);if(!(focus.getItem() instanceof ThaumicFocusItem item)||kind.equals("sceptre"))return base;String key=parts.rod()+":"+parts.cap()+":"+item.kind();return cache.computeIfAbsent(key,ignored->withFocus(base,models.getOrDefault(focusModel(kind,item.kind()),base)));}
                };
                @Override public ItemOverrides getOverrides(){return overrides;}
            });
        }
    }
    private static BakedModel withFocus(BakedModel base,BakedModel head){return new BakedModelWrapper<BakedModel>(base){
        @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random){var quads=new ArrayList<>(base.getQuads(state,side,random));quads.addAll(head.getQuads(state,side,random));return quads;}
        @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random,net.neoforged.neoforge.client.model.data.ModelData data,net.minecraft.client.renderer.RenderType type){return getQuads(state,side,random);}
        @Override public java.util.List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){return List.of(this);}
        @Override public BakedModel applyTransform(net.minecraft.world.item.ItemDisplayContext context,com.mojang.blaze3d.vertex.PoseStack pose,boolean left){base.applyTransform(context,pose,left);return this;}
    };}
    private WandModels(){}
}

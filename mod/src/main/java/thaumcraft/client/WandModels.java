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
    private static net.minecraft.client.renderer.ShaderInstance FOCUS_SHADER;
    static net.minecraft.client.renderer.ShaderInstance focusShader(){return FOCUS_SHADER;}
    @SubscribeEvent public static void shaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event)throws java.io.IOException{event.registerShader(new net.minecraft.client.renderer.ShaderInstance(event.getResourceProvider(),ResourceLocation.parse("thaumcraft:focus"),com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY),shader->FOCUS_SHADER=shader);PortableHoleEffects.registerShader(event);}
    private static final List<String> CAPS=List.of("iron","gold","thaumium","void","copper","silver");
    private static final List<String> RODS=List.of("wood","greatwood","obsidian","silverwood","ice","quartz","reed","blaze","bone");
    private static ModelResourceLocation variant(String kind,String rod,String cap){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft",kind+"/"+rod+"_"+cap));}
    private static ModelResourceLocation focusModel(String kind,String focus){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft",kind+"/focus_"+focus));}
    @SubscribeEvent public static void registerFocus(ModelEvent.RegisterAdditional event){for(var registration:thaumcraft.api.wands.FocusBehaviors.all()){event.register(ModelResourceLocation.standalone(registration.model()));if(registration.ornament()!=null)event.register(ModelResourceLocation.standalone(registration.ornament()));}for(var model:thaumcraft.api.wands.WandVisuals.models())event.register(ModelResourceLocation.standalone(model));for(var kind:List.of("wand","staff")){for(var focus:ThaumicFocusItem.Kind.values())event.register(focusModel(kind,focus.name().toLowerCase(Locale.ROOT)));event.register(focusModel(kind,"pech"));}for(var name:List.of("hellbat","trade","warding"))event.register(focusModel("item",name+"_ornament"));}
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event){for(var kind:List.of("wand","staff","sceptre"))for(var rod:RODS)for(var cap:CAPS){if(kind.equals("staff")&&rod.equals("wood"))continue;event.register(variant(kind,rod,cap));}for(var cap:CAPS)event.register(variant("staff","primal",cap));}
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event){
        var models=event.getModels();for(var kind:List.of("wand","staff","sceptre")){
            var id=new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft","casting_"+kind),"inventory");var original=models.get(id);if(original==null)continue;
            models.put(id,new BakedModelWrapper<BakedModel>(original){
                private final ItemOverrides overrides=new ItemOverrides(){
                    private final Map<String,BakedModel> cache=new HashMap<>();
                    @Override public BakedModel resolve(BakedModel model,ItemStack stack,ClientLevel level,LivingEntity entity,int seed){var parts=WandMaterials.parts(stack);var base=parts.rod().contains(":")||parts.cap().contains(":")?original:models.getOrDefault(variant(kind,parts.rod().replace("_staff",""),parts.cap()),original);var addonBase=thaumcraft.api.wands.WandVisuals.model(kind,parts);if(addonBase!=null)base=models.getOrDefault(ModelResourceLocation.standalone(addonBase),base);base=layered(base,kind,parts,models);final var resolvedBase=base;var focus=WandFocus.get(stack);var addon=thaumcraft.api.wands.FocusBehaviors.get(focus);if(addon!=null&&!kind.equals("sceptre")){final var addonWand=base;return cache.computeIfAbsent(parts.toString()+addon.model()+addon.ornament(),ignored->withFocus(addonWand,models.getOrDefault(ModelResourceLocation.standalone(addon.model()),addonWand),addon.ornament()==null?null:models.get(ModelResourceLocation.standalone(addon.ornament())),kind.equals("staff")));}String name=focus.getItem() instanceof ThaumicFocusItem item?item.kind().name().toLowerCase(Locale.ROOT):focus.getItem() instanceof PechFocusItem?"pech":null;if(name==null||kind.equals("sceptre"))return base;String key=parts.rod()+":"+parts.cap()+":"+name;return cache.computeIfAbsent(key,ignored->withFocus(resolvedBase,models.getOrDefault(focusModel(kind,name),resolvedBase),models.get(focusModel("item",name+"_ornament")),kind.equals("staff")));}
                };
                @Override public ItemOverrides getOverrides(){return overrides;}
            });
        }
    }
    private static BakedModel layered(BakedModel base,String kind,thaumcraft.api.wands.WandParts parts,Map<ModelResourceLocation,BakedModel> models){
        var definitions=thaumcraft.api.wands.WandVisuals.layers(kind,parts);if(definitions.isEmpty())return base;
        var passes=new ArrayList<BakedModel>();for(var layer:definitions){var geometry=models.get(ModelResourceLocation.standalone(layer.model()));if(geometry==null)return base;passes.add(new WandLayerModel(geometry,layer.lighting()));}
        return new BakedModelWrapper<BakedModel>(base){
            @Override public List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){return passes;}
            @Override public BakedModel applyTransform(net.minecraft.world.item.ItemDisplayContext context,com.mojang.blaze3d.vertex.PoseStack pose,boolean left){base.applyTransform(context,pose,left);return this;}
        };
    }
    private static BakedModel withFocus(BakedModel base,BakedModel head,BakedModel ornament,boolean staff){return composite(base,staff?new BakedModelWrapper<BakedModel>(head){@Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random){return head.getQuads(state,side,random).stream().map(q->transform(q,new org.joml.Matrix4f().translate(0,8.2f/16,0))).toList();}}:head,ornament,staff);}
    private static BakedModel composite(BakedModel base,BakedModel head,BakedModel ornament,boolean staff){var extra=new ArrayList<net.minecraft.client.renderer.block.model.BakedQuad>();if(ornament!=null)for(int turn=0;turn<2;turn++){var transform=new org.joml.Matrix4f().translate(.5f,staff?1.65f:1.025f,.5f).rotateY(turn*(float)Math.PI/2).translate(-.25f,0,-.3975f).scale(.5f,.5f,.8f);for(var quad:ornament.getQuads(null,null,net.minecraft.util.RandomSource.create()))extra.add(transform(quad,transform));}return new BakedModelWrapper<BakedModel>(base){
        @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random){var quads=new ArrayList<>(base.getQuads(state,side,random));quads.addAll(head.getQuads(state,side,random));if(side==null)quads.addAll(extra);return quads;}
        @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random,net.neoforged.neoforge.client.model.data.ModelData data,net.minecraft.client.renderer.RenderType type){return getQuads(state,side,random);}
        @Override public java.util.List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){
            boolean depth=head.getQuads(null,null,net.minecraft.util.RandomSource.create()).stream().anyMatch(q->q.getSprite().contents().name().getPath().endsWith("_depth"));
            // Modern item rendering discards baked vertex alpha. Separate passes preserve TC4's glass shell.
            var passes=new ArrayList<BakedModel>(base.getRenderPasses(stack,fabulous));passes.add(focusPass(head,extra,0,1));passes.add(focusPass(head,extra,1,depth?.6f:.95f));passes.add(focusPass(head,extra,2,1));return passes;
        }
        @Override public BakedModel applyTransform(net.minecraft.world.item.ItemDisplayContext context,com.mojang.blaze3d.vertex.PoseStack pose,boolean left){base.applyTransform(context,pose,left);return this;}
    };}
    private static BakedModel focusPass(BakedModel head,java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> extra,int layer,float alpha){return new BakedModelWrapper<BakedModel>(head){
        @Override public java.util.List<net.minecraft.client.renderer.RenderType> getRenderTypes(ItemStack item,boolean fabulous){return List.of(ThaumcraftRenderTypes.focus(alpha));}
        @Override public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction side,net.minecraft.util.RandomSource random){
            if(layer==2)return side==null?extra:List.of();
            return head.getQuads(state,side,random).stream().filter(q->(q.getTintIndex()>=0)==(layer==1)).toList();
        }
    };}
    private static net.minecraft.client.renderer.block.model.BakedQuad transform(net.minecraft.client.renderer.block.model.BakedQuad quad,org.joml.Matrix4f matrix){var data=quad.getVertices().clone();int stride=data.length/4;for(int v=0;v<4;v++){int i=v*stride;var p=new org.joml.Vector3f(Float.intBitsToFloat(data[i]),Float.intBitsToFloat(data[i+1]),Float.intBitsToFloat(data[i+2])).mulPosition(matrix);data[i]=Float.floatToRawIntBits(p.x);data[i+1]=Float.floatToRawIntBits(p.y);data[i+2]=Float.floatToRawIntBits(p.z);}var n=quad.getDirection().step().mulDirection(matrix).normalize();return new net.minecraft.client.renderer.block.model.BakedQuad(data,quad.getTintIndex(),net.minecraft.core.Direction.getNearest(n.x,n.y,n.z),quad.getSprite(),quad.isShade());}
    private WandModels(){}
}

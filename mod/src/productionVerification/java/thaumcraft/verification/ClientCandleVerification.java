package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Actual loaded model data/quads and GPU submission against owned original renderer bytecode. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientCandleVerification {
    private record Face(int side,boolean wick,JsonArray vertices){}
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!"candles-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=net.minecraft.client.Minecraft.getInstance();if(mc.screen==null||mc.level!=null||mc.getOverlay()!=null)return;done=true;
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var result=new JsonObject();var checks=new ArrayList<String>();
        try{
            var geometry=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-candle-geometry.json"))).getAsJsonObject();
            var faces=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-candle-faces.json"))).getAsJsonArray();
            require(faces.size()==156,"six actual original faces for body, wick and all24 drip cuboids",checks);
            var reference=new HashMap<String,List<JsonObject>>();for(var value:faces){var f=value.getAsJsonObject();reference.computeIfAbsent(key(f.getAsJsonArray("box")),k->new ArrayList<>()).add(f);}
            var buffers=mc.renderBuffers().bufferSource();var cutout=RenderType.cutout();
            for(int metadata=0;metadata<16;metadata++){
                var candle=thaumcraft.registry.R4Content.CANDLES.get(metadata).get();var state=candle.defaultBlockState();var stack=new ItemStack(candle);
                var model=mc.getBlockRenderer().getBlockModel(state);var item=mc.getItemRenderer().getModel(stack,null,null,0);var inventory=geometry.getAsJsonArray("inventory").get(metadata).getAsJsonObject();
                require((mc.getItemColors().getColor(stack,0)&0xffffff)==inventory.get("color").getAsInt(),"native item metadata palette "+metadata,checks);
                require(mc.getItemColors().getColor(stack,1)==-1,"native item wick remains white "+metadata,checks);
                require(matches(item.getQuads(null,null,RandomSource.create(0)),expected(inventory.getAsJsonArray("boxes"),reference)),"actual inventory geometry and all face UVs equal original, with no drips "+metadata,checks);
                for(var value:geometry.getAsJsonArray("world")){
                    var row=value.getAsJsonObject();var xyz=row.getAsJsonArray("pos");var pos=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());var data=model.getModelData(null,pos,state,ModelData.EMPTY);
                    var quads=model.getQuads(state,null,RandomSource.create(999),data,cutout);
                    if(!matches(quads,expected(row.getAsJsonArray("boxes"),reference))){
                        result.addProperty("model_class",model.getClass().getName());result.addProperty("item_model_class",item.getClass().getName());result.addProperty("quad_count",quads.size());
                        var diagnostic=new JsonArray();for(var q:quads){var f=new JsonObject();f.addProperty("face",q.getDirection().get3DDataValue());f.addProperty("tint",q.getTintIndex());f.addProperty("sprite",q.getSprite().contents().name().toString());var points=new JsonArray();var vs=q.getVertices();for(int vi=0;vi<4;vi++){int vo=vi*IQuadTransformer.STRIDE;var point=new JsonArray();for(int vn=0;vn<3;vn++)point.add(Float.intBitsToFloat(vs[vo+vn]));point.add(q.getSprite().getUOffset(Float.intBitsToFloat(vs[vo+IQuadTransformer.UV0])));point.add(q.getSprite().getVOffset(Float.intBitsToFloat(vs[vo+IQuadTransformer.UV0+1])));points.add(point);}f.add("vertices",points);diagnostic.add(f);}result.add("actual_quads",diagnostic);
                    }
                    require(matches(quads,expected(row.getAsJsonArray("boxes"),reference)),"loaded block metadata"+metadata+" geometry/UV/tints match original at "+pos,checks);
                    if(pos.equals(BlockPos.ZERO)){
                        var pose=new PoseStack();pose.translate(20+metadata*16,40,0);pose.scale(16,16,16);int color=candle.color;
                        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(cutout),state,model,((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,data,cutout);
                    }
                }
                buffers.endBatch(cutout);require(org.lwjgl.opengl.GL11.glGetError()==0,"native GPU cutout submission for wax and white wick metadata"+metadata,checks);
            }
            try(var original=new JarFile(root.resolve("Thaumcraft-1.7.10-4.2.3.5.jar").toFile())){
                for(String texture:List.of("candle","candlestub")){
                    String path="textures/blocks/"+texture+".png";
                    try(var source=original.getInputStream(original.getJarEntry("assets/thaumcraft/"+path));var nativeFile=mc.getResourceManager().getResourceOrThrow(ResourceLocation.fromNamespaceAndPath("thaumcraft",path)).open()){
                        require(Arrays.equals(source.readAllBytes(),nativeFile.readAllBytes()),"native "+texture+" PNG equals owned TC4 bytes",checks);
                    }
                }
            }
            var payload=new JsonObject();for(String path:List.of("thaumcraft/blocks/TallowCandleGeometry.class","thaumcraft/blocks/TallowCandleGeometry$Box.class","thaumcraft/client/TallowCandleModels.class","thaumcraft/client/TallowCandleModels$1.class","thaumcraft/client/TallowCandleModels$2.class","thaumcraft/client/R4Client.class")){
                try(var stream=ClientCandleVerification.class.getClassLoader().getResourceAsStream(path)){if(stream!=null)payload.addProperty(path,sha(stream.readAllBytes()));}
            }
            for(String path:List.of("models/block/tallow_candle.json","textures/blocks/candle.png","textures/blocks/candlestub.png"))try(var stream=mc.getResourceManager().getResourceOrThrow(ResourceLocation.fromNamespaceAndPath("thaumcraft",path)).open()){payload.addProperty("assets/thaumcraft/"+path,sha(stream.readAllBytes()));}
            result.add("payload_sha256",payload);result.addProperty("passed",true);result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());failure.printStackTrace();}
        result.addProperty("count",checks.size());result.add("checks",new Gson().toJsonTree(checks));result.addProperty("time",java.time.Instant.now().toString());result.addProperty("manual_pixel_acceptance","pending");
        Files.writeString(root.resolve("analysis/full_port/candles-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private static List<Face> expected(JsonArray boxes,Map<String,List<JsonObject>> reference){
        var result=new ArrayList<Face>();for(var box:boxes){var b=box.getAsJsonArray();for(var f:Objects.requireNonNull(reference.get(key(b)),"Missing original face cube"))result.add(new Face(f.get("face").getAsInt(),b.get(6).getAsInt()!=0,f.getAsJsonArray("vertices")));}return result;
    }
    private static String key(JsonArray box){var b=new float[6];for(int i=0;i<6;i++)b[i]=box.get(i).getAsFloat();return Arrays.toString(b);}
    private static boolean matches(List<BakedQuad> actual,List<Face> expected){
        if(actual.size()!=expected.size())return false;var remaining=new ArrayList<>(expected);
        for(var quad:actual){int found=-1;for(int i=0;i<remaining.size();i++)if(matches(quad,remaining.get(i))){found=i;break;}if(found<0)return false;remaining.remove(found);}return remaining.isEmpty();
    }
    private static boolean matches(BakedQuad q,Face reference){
        if(q.getDirection().get3DDataValue()!=reference.side||q.getTintIndex()!=(reference.wick?-1:0))return false;
        if(!q.getSprite().contents().name().equals(ResourceLocation.fromNamespaceAndPath("thaumcraft","blocks/"+(reference.wick?"candlestub":"candle"))))return false;
        var data=q.getVertices();var vertices=new ArrayList<JsonElement>();reference.vertices.forEach(vertices::add);
        for(int i=0;i<4;i++){
            int offset=i*IQuadTransformer.STRIDE;float[] values={Float.intBitsToFloat(data[offset]),Float.intBitsToFloat(data[offset+1]),Float.intBitsToFloat(data[offset+2]),q.getSprite().getUOffset(Float.intBitsToFloat(data[offset+IQuadTransformer.UV0])),q.getSprite().getVOffset(Float.intBitsToFloat(data[offset+IQuadTransformer.UV0+1]))};
            int found=-1;for(int j=0;j<vertices.size();j++){boolean same=true;var row=vertices.get(j).getAsJsonArray();for(int n=0;n<5;n++)same&=Math.abs(values[n]-row.get(n).getAsFloat())<=(n<3?1e-7:2e-5);if(same){found=j;break;}}
            if(found<0)return false;vertices.remove(found);
        }
        return true;
    }
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static void require(boolean value,String label,List<String> checks){if(!value)throw new AssertionError(label);checks.add(label);}
}

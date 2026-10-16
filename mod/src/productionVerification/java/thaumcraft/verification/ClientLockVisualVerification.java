package thaumcraft.verification;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.*;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.blocks.*;
import thaumcraft.registry.R5Content;
import java.nio.file.*;
import java.util.*;

/** Actual GPU draw plus comparison against owned original renderer bytecode captures. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientLockVisualVerification {
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception {
        if(done||!"lock-visuals".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=net.minecraft.client.Minecraft.getInstance();if(!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)||mc.getOverlay()!=null)return;done=true;
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var result=new JsonObject();var checks=new ArrayList<String>();
        try {
            var controls=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-lock-visuals.json"))).getAsJsonObject();
            require(controls.getAsJsonArray("cases").size()==273,"273 original renderer control cases",checks);
            var type=thaumcraft.client.EldritchLockRenderer.class;
            var arm=type.getDeclaredMethod("armPose",PoseStack.class,Direction.class,EldritchLockGeometry.Cube.class);arm.setAccessible(true);
            var key=type.getDeclaredMethod("keyPose",PoseStack.class,Direction.class);key.setAccessible(true);
            for(var entry:controls.getAsJsonArray("cases")){
                var row=entry.getAsJsonObject();int count=row.get("count").getAsInt(),facing=row.get("facing").getAsInt(),ticks=row.get("ticks").getAsInt();var face=facing==6?null:Direction.from3DDataValue(facing);
                var pieces=EldritchLockGeometry.cubes(count,ticks,row.get("partial").getAsFloat());var expected=row.getAsJsonArray("cubes");
                require(pieces.size()==expected.size(),"Original integer disappearance timing: count="+count+" face="+facing+" ticks="+ticks,checks);
                for(int i=0;i<pieces.size();i++){var pose=new PoseStack();arm.invoke(null,pose,face,pieces.get(i));matrix(pose,expected.get(i).getAsJsonArray());}
                require(true,"Original cube pulse and full transform matrices: count="+count+" face="+facing+" ticks="+ticks,checks);
                if(count>=0){var pose=new PoseStack();key.invoke(null,pose,face);matrix(pose,row.getAsJsonArray("key"));require(true,"Original key placement boundary: count="+count+" face="+facing+" ticks="+ticks,checks);}
            }
            var plane=thaumcraft.client.EldritchVoidRenderer.class.getDeclaredMethod("lockPlane",PoseStack.class,MultiBufferSource.class,Direction.class,boolean.class);plane.setAccessible(true);
            for(var entry:controls.getAsJsonArray("planes")){
                var row=entry.getAsJsonObject();var face=switch(row.get("plane").getAsString()){case "ZPos"->Direction.SOUTH;case "ZNeg"->Direction.NORTH;case "XPos"->Direction.EAST;default->Direction.WEST;};boolean near=row.get("near").getAsBoolean();var layers=row.getAsJsonArray("layers");
                require(layers.size()==(near?16:1),"Original lock void layer count: "+face+" near="+near,checks);
                for(int j=0;j<layers.size();j++){
                    var layer=layers.get(j).getAsJsonObject();for(int v=0;v<4;v++){var expected=layer.getAsJsonArray("vertices").get(v).getAsJsonArray();var actual=EldritchLockGeometry.planeVertex(face,v);for(int axis=0;axis<(near?3:5);axis++)if(actual[axis]!=expected.get(axis).getAsFloat())throw new AssertionError("Lock plane vertex/UV differs: "+face+" "+v+" "+axis);}
                    if(near){var rgb=EldritchVoidGeometry.color(j);for(int c=0;c<3;c++)if(Math.abs(rgb[c]-layer.getAsJsonArray("color").get(c).getAsFloat())>1e-6)throw new AssertionError("Layer RGB differs");if(EldritchVoidGeometry.scale(j)!=layer.get("scale").getAsFloat()||EldritchVoidGeometry.rotation(j)!=layer.get("rotation").getAsFloat())throw new AssertionError("Layer transform differs");}
                    else for(int c=0;c<3;c++)if(layer.getAsJsonArray("color").get(c).getAsFloat()!=.5f)throw new AssertionError("Original far RGB is not gray");
                }
                require(true,"Original lock plane winding, UVs and all layer parameters: "+face+" near="+near,checks);
                var capture=new Capture();plane.invoke(null,new PoseStack(),(MultiBufferSource)renderType->{capture.type=renderType;return capture;},face,near);
                require(capture.vertices.size()==4&&capture.type.format()==(near?DefaultVertexFormat.POSITION:DefaultVertexFormat.POSITION_TEX_COLOR),"Actual native void submission format: "+face+" near="+near,checks);
                for(int v=0;v<4;v++){var actual=capture.vertices.get(v);var expected=EldritchLockGeometry.planeVertex(face,v);for(int c=0;c<3;c++)if(actual[c]!=expected[c])throw new AssertionError("Submitted vertex differs");if(!near&&(actual[3]!=expected[3]||actual[4]!=expected[4]||actual[5]!=127||actual[6]!=127||actual[7]!=127||actual[8]!=255))throw new AssertionError("Submitted far UV/gray differs");}
                require(true,"Actual native void vertices and original packed gray127: "+face+" near="+near,checks);
            }
            var modelControls=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-lock-model-vertices.json"))).getAsJsonArray();
            var modelTile=new EldritchLockBlockEntity(BlockPos.ZERO,R5Content.ELDRITCH_LOCK.get().defaultBlockState());var modelRenderer=mc.getBlockEntityRenderDispatcher().getRenderer(modelTile);var modelField=type.getDeclaredField("cube");modelField.setAccessible(true);var model=(net.minecraft.client.model.geom.ModelPart)modelField.get(modelRenderer);var meshCapture=new Capture();model.render(new PoseStack(),meshCapture,0xf000f0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            require(meshCapture.vertices.size()==24&&modelControls.size()==24,"Native and unchanged original MC cube each contain24 vertices",checks);
            var remaining=new ArrayList<>(meshCapture.vertices);for(var entry:modelControls){var vertex=entry.getAsJsonArray();boolean found=false;for(var iterator=remaining.iterator();iterator.hasNext();){var actual=iterator.next();boolean same=true;for(int c=0;c<5;c++)same&=Math.abs(actual[c]-vertex.get(c).getAsFloat())<1e-6;if(same){iterator.remove();found=true;break;}}if(!found)throw new AssertionError("Original cube position/UV absent from native mesh: "+vertex);}
            require(remaining.isEmpty(),"All24 native cube positions and UVs match actual unchanged Minecraft1.7.10 ModelBox",checks);
            var pose=new PoseStack();pose.translate(0,0,-100);var buffers=mc.renderBuffers().bufferSource();
            for(int facing=0;facing<=6;facing++)for(int count:new int[]{-1,20,80}){
                var state=R5Content.ELDRITCH_LOCK.get().defaultBlockState().setValue(EldritchLockBlock.FACING,facing);var tile=new EldritchLockBlockEntity(BlockPos.ZERO,state);tile.facing=(byte)facing;tile.count=count;
                var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(tile);require(renderer instanceof thaumcraft.client.EldritchLockRenderer,"Registered GPU lock renderer: face="+facing+" count="+count,checks);renderer.render(tile,.375f,pose,buffers,0xf000f0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);buffers.endBatch();
            }
            for(var face:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST})for(boolean near:new boolean[]{true,false}){plane.invoke(null,pose,buffers,face,near);buffers.endBatch();require(org.lwjgl.opengl.GL11.glGetError()==org.lwjgl.opengl.GL11.GL_NO_ERROR,"Actual lock GPU plane without GL error: "+face+" near="+near,checks);}
            mc.getTextureManager().bindForSetup(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/models/eldritch_cube.png"));require(mc.getTextureManager().getTexture(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/models/eldritch_cube.png")).getId()>0,"Original cube texture uploaded to GPU",checks);
            require(org.lwjgl.opengl.GL11.glGetError()==org.lwjgl.opengl.GL11.GL_NO_ERROR,"Actual lock cubes and shader draws leave no GL error",checks);
            result.addProperty("passed",true);result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());}
        var payload=new JsonObject();for(String path:List.of("thaumcraft/client/EldritchLockRenderer.class","thaumcraft/client/EldritchLockRenderer$1.class","thaumcraft/client/EldritchVoidRenderer.class","thaumcraft/blocks/EldritchLockGeometry.class","thaumcraft/blocks/EldritchLockGeometry$Cube.class","thaumcraft/blocks/EldritchVoidGeometry.class","thaumcraft/client/OuterRoomRenderers.class","assets/thaumcraft/textures/models/eldritch_cube.png","assets/thaumcraft/shaders/core/eldritch_void.json","assets/thaumcraft/shaders/core/eldritch_void.vsh","assets/thaumcraft/shaders/core/eldritch_void.fsh","assets/thaumcraft/textures/misc/tunnel.png","assets/thaumcraft/textures/misc/particlefield.png","assets/thaumcraft/textures/misc/particlefield32.png")){try(var stream=ClientLockVisualVerification.class.getClassLoader().getResourceAsStream(path)){if(stream==null)throw new AssertionError("Runtime payload missing "+path);payload.addProperty(path,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}
        result.add("payload_sha256",payload);result.add("checks",new Gson().toJsonTree(checks));result.addProperty("count",checks.size());result.addProperty("time",java.time.Instant.now().toString());result.addProperty("manual_acceptance","pending");result.addProperty("key_internal_item_mesh","not compared; native GROUND display transform");result.addProperty("boss_activation","pending native encounter mechanics");Files.writeString(root.resolve("analysis/full_port/lock-visuals-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private static void matrix(PoseStack pose,JsonArray expected){float[] actual=new float[16];pose.last().pose().get(actual);for(int i=0;i<16;i++)if(Math.abs(actual[i]-expected.get(i).getAsDouble())>2e-6)throw new AssertionError("Original matrix differs at "+i+": "+actual[i]+" / "+expected.get(i));}
    private static void require(boolean pass,String message,List<String> checks){if(!pass)throw new AssertionError(message);checks.add(message);}
    private static final class Capture implements VertexConsumer {
        final List<float[]> vertices=new ArrayList<>();RenderType type;
        public VertexConsumer addVertex(float x,float y,float z){vertices.add(new float[]{x,y,z,0,0,0,0,0,0});return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){var v=vertices.getLast();v[5]=r;v[6]=g;v[7]=b;v[8]=a;return this;}
        public VertexConsumer setUv(float u,float v){var p=vertices.getLast();p[3]=u;p[4]=v;return this;}
        public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
    private ClientLockVisualVerification(){}
}

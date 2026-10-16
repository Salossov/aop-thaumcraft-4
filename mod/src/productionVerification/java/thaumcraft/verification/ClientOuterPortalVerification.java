package thaumcraft.verification;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.registry.R5Content;
import thaumcraft.blocks.EldritchPortalBlockEntity;

@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientOuterPortalVerification {
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!"outer-portal-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=net.minecraft.client.Minecraft.getInstance();if(!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)||mc.getOverlay()!=null)return;done=true;
        var result=new com.google.gson.JsonObject();var checks=new ArrayList<String>();
        try{
            var tile=new EldritchPortalBlockEntity(net.minecraft.core.BlockPos.ZERO,R5Content.ELDRITCH_PORTAL.get().defaultBlockState());var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(tile);
            if(!(renderer instanceof thaumcraft.client.EldritchPortalRenderer)||renderer.getViewDistance()!=96||renderer.getRenderBoundingBox(tile).getYsize()!=3)throw new AssertionError("Original portal renderer registration/bounds/range differs");checks.add("Registered native portal renderer has96block view distance and3block bounds");
            var texture=ResourceLocation.parse("thaumcraft:textures/misc/eldritch_portal.png");mc.getTextureManager().bindForSetup(texture);if(mc.getTextureManager().getTexture(texture).getId()<=0)throw new AssertionError("Portal atlas did not upload");checks.add("Original sixteen-frame portal texture uploads to actual GPU");
            var method=thaumcraft.client.EldritchPortalRenderer.class.getDeclaredMethod("draw",EldritchPortalBlockEntity.class,float.class,PoseStack.class,net.minecraft.client.renderer.MultiBufferSource.class,org.joml.Quaternionf.class,long.class);method.setAccessible(true);
            for(int count:new int[]{0,1,5,15,30}){
                tile.openCount=count;var capture=new Capture();net.minecraft.client.renderer.MultiBufferSource buffer=type->capture.consumer;method.invoke(null,tile,.75f,new PoseStack(),buffer,new org.joml.Quaternionf(),0L);
                float width=2*Math.min(5,count)/5f,height=2*Math.min(30,count)/30f;
                if(capture.vertices.size()!=4||capture.colors.size()!=4||capture.lights.size()!=4||Math.abs(capture.range(0)-width)>.00001||Math.abs(capture.range(1)-height)>.00001||capture.colors.stream().anyMatch(c->c[3]!=255)||capture.lights.stream().anyMatch(l->l!=220))throw new AssertionError("Original integer opening scale/color/light differs at tick"+count);checks.add("Original opening tick"+count+" preserves independent5/30tick scale and white light220 vertices");
            }
            tile.openCount=30;for(int frame=0;frame<16;frame++){var capture=new Capture();net.minecraft.client.renderer.MultiBufferSource buffer=type->capture.consumer;method.invoke(null,tile,0f,new PoseStack(),buffer,new org.joml.Quaternionf(),frame*50_000_000L);float u=frame/16f;float[][] expected={{u,1},{u+.0625f,1},{u+.0625f,0},{u,0}};for(int i=0;i<4;i++)if(!Arrays.equals(expected[i],capture.uv.get(i)))throw new AssertionError("Original portal UV ordering differs at frame"+frame);checks.add("Original50ms atlas frame"+frame+" retains all four ordered UV coordinates");}
            tile.openCount=5;var pitched=new Capture();net.minecraft.client.renderer.MultiBufferSource capture=type->pitched.consumer;method.invoke(null,tile,0f,new PoseStack(),capture,new org.joml.Quaternionf().rotateX((float)Math.PI/2),0L);if(Math.abs(pitched.range(2)-2)>.00001)throw new AssertionError("Portal incorrectly scales in billboard-local axes");checks.add("Pitched camera retains original world-axis opening scale");
            var pose=new PoseStack();pose.translate(0,0,-500);var buffers=mc.renderBuffers().bufferSource();for(int count:new int[]{1,5,30}){tile.openCount=count;method.invoke(null,tile,.5f,pose,buffers,new org.joml.Quaternionf(),count*50_000_000L);buffers.endBatch();}if(org.lwjgl.opengl.GL11.glGetError()!=org.lwjgl.opengl.GL11.GL_NO_ERROR)throw new AssertionError("Portal GPU submission caused GL error");checks.add("Three opening stages draw through actual native GPU shader without OpenGL errors");
            result.addProperty("passed",true);result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
        }catch(Throwable error){result.addProperty("passed",false);result.addProperty("error",error.toString());}
        var hashes=new com.google.gson.JsonObject();for(String path:List.of("thaumcraft/client/EldritchPortalRenderer.class","thaumcraft/blocks/EldritchPortalBlock.class","thaumcraft/blocks/EldritchPortalBlockEntity.class","thaumcraft/world/dim/EldritchTeleporter.class","thaumcraft/production/EldritchAltarBlockEntity.class","thaumcraft/client/OuterRoomRenderers.class")){try(var stream=ClientOuterPortalVerification.class.getClassLoader().getResourceAsStream(path)){hashes.addProperty(path,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}
        result.add("payload_sha256",hashes);result.add("checks",new com.google.gson.Gson().toJsonTree(checks));result.addProperty("count",checks.size());result.addProperty("time",java.time.Instant.now().toString());result.addProperty("manual_acceptance","pending");java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/outer-portal-client.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private static final class Capture{
        final List<float[]> vertices=new ArrayList<>(),uv=new ArrayList<>();final List<int[]> colors=new ArrayList<>();final List<Integer> lights=new ArrayList<>();
        final VertexConsumer consumer=(VertexConsumer)java.lang.reflect.Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class[]{VertexConsumer.class},(proxy,method,args)->{
            if(method.isDefault())return java.lang.reflect.InvocationHandler.invokeDefault(proxy,method,args);
            switch(method.getName()){
                case "addVertex"->vertices.add(new float[]{(float)args[0],(float)args[1],(float)args[2]});
                case "setUv"->uv.add(new float[]{(float)args[0],(float)args[1]});
                case "setColor"->colors.add(new int[]{(int)args[0],(int)args[1],(int)args[2],(int)args[3]});
                case "setUv2"->lights.add((int)args[0]|((int)args[1]<<16));
                default->{}
            }return proxy;
        });
        float range(int axis){float low=Float.POSITIVE_INFINITY,high=Float.NEGATIVE_INFINITY;for(var vertex:vertices){low=Math.min(low,vertex[axis]);high=Math.max(high,vertex[axis]);}return high-low;}
    }
    private ClientOuterPortalVerification(){}
}

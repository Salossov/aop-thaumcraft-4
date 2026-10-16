package thaumcraft.verification;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Checks the real driver-compiled registered shader. No input or world is required. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientVoidShaderVerification {
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!"void-shader".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=net.minecraft.client.Minecraft.getInstance();if(!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)||mc.getOverlay()!=null)return;done=true;
        var result=new com.google.gson.JsonObject();try{
            var field=thaumcraft.client.EldritchVoidRenderer.class.getDeclaredField("shader");field.setAccessible(true);var shader=(net.minecraft.client.renderer.ShaderInstance)field.get(null);
            if(shader==null)throw new AssertionError("Registered void shader was not loaded");
            if(org.lwjgl.opengl.GL20.glGetProgrami(shader.getId(),org.lwjgl.opengl.GL20.GL_LINK_STATUS)!=org.lwjgl.opengl.GL11.GL_TRUE)throw new AssertionError("Driver shader link failed");
            if(shader.getUniform("Clock")==null||shader.getUniform("ModelViewMat")==null||shader.getUniform("ProjMat")==null)throw new AssertionError("Required uniforms are absent");
            for(String texture:java.util.List.of("tunnel","particlefield","particlefield32"))if(mc.getResourceManager().getResource(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/misc/"+texture+".png")).isEmpty())throw new AssertionError("Void texture absent: "+texture);
            result.addProperty("passed",true);result.addProperty("shader",shader.getName());result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));result.addProperty("check","native RegisterShadersEvent callback, actual GPU program link, required uniforms and three original textures");
            var payload=new com.google.gson.JsonObject();for(String path:java.util.List.of("thaumcraft/client/EldritchVoidRenderer.class","thaumcraft/blocks/EldritchVoidGeometry.class")){try(var stream=ClientVoidShaderVerification.class.getClassLoader().getResourceAsStream(path)){if(stream==null)throw new AssertionError("Missing class payload "+path);payload.addProperty(path,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}
            for(String path:java.util.List.of("shaders/core/eldritch_void.json","shaders/core/eldritch_void.vsh","shaders/core/eldritch_void.fsh","textures/misc/tunnel.png","textures/misc/particlefield.png","textures/misc/particlefield32.png")){try(var stream=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft",path)).open()){payload.addProperty("assets/thaumcraft/"+path,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}
            result.add("payload_sha256",payload);
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());}
        result.addProperty("time",java.time.Instant.now().toString());result.addProperty("pixel_acceptance","pending");var output=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/eldritch-void-client-shader.json");java.nio.file.Files.writeString(output,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private ClientVoidShaderVerification(){}
}

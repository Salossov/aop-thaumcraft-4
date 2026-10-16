package thaumcraft.verification;

import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.Particle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.blocks.PavingStoneBlock;

/** Real loaded particle tick/vertices and native GPU against the owned original FXBlockRunes. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientPavingVerification {
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!"paving-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)||mc.getOverlay()!=null)return;done=true;
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var result=new JsonObject();var checks=new ArrayList<String>();
        try{
            var references=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-paving-runes.json"))).getAsJsonArray();
            var type=Class.forName("thaumcraft.client.PavingRuneParticle");var constructor=type.getDeclaredConstructor(PavingStoneBlock.Rune.class);constructor.setAccessible(true);
            var camera=new Camera();int draws=0;
            for(var value:references){
                var reference=value.getAsJsonObject();int duration=reference.get("duration").getAsInt();float gravity=reference.get("gravity").getAsFloat(),red=reference.get("red").getAsFloat();
                var particle=(Particle)constructor.newInstance(new PavingStoneBlock.Rune(1456,(float)120+.7f,0,red,.2f,.9f,duration,gravity));var profile=reference.getAsJsonArray("profile");
                require((int)get(particle,"lifetime")==duration*3,"source three-times-duration lifetime "+draws,checks);
                require((float)get(particle,"rCol")== (red==0?1:red),"original zero-red fallback "+draws,checks);
                // Native entity randomness differs; replay the source's generated profile to compare its renderer exactly.
                set(particle,"rotation",(int)profile.get(0).getAsFloat());set(particle,"rune",profile.get(1).getAsInt()%16);set(particle,"offsetX",profile.get(2).getAsDouble());set(particle,"offsetY",profile.get(3).getAsDouble());set(particle,"scale",profile.get(4).getAsFloat());
                int tick=0;
                for(var frameValue:reference.getAsJsonArray("frames")){
                    var frame=frameValue.getAsJsonObject();while(tick<frame.get("tick").getAsInt()){particle.tick();tick++;}
                    var pos=frame.getAsJsonArray("position");require((int)get(particle,"age")==frame.get("age").getAsInt()&&(boolean)get(particle,"removed")==frame.get("removed").getAsBoolean(),"source expiry and age "+draws+" tick"+tick,checks);
                    require(Math.abs((double)get(particle,"x")-pos.get(0).getAsDouble())<1e-10&&Math.abs((double)get(particle,"y")-pos.get(1).getAsDouble())<1e-10&&Math.abs((double)get(particle,"z")-pos.get(2).getAsDouble())<1e-10,"source undamped gravity and position "+draws+" tick"+tick,checks);
                    var captured=new ArrayList<double[]>();double[][] current={null};
                    var consumer=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class[]{VertexConsumer.class},(proxy,method,args)->{
                        if(method.getName().equals("addVertex")){var point=new double[9];for(int i=0;i<3;i++)point[i]=((Number)args[i]).doubleValue();current[0]=point;captured.add(point);}
                        else if(method.getName().equals("setUv")){current[0][3]=((Number)args[0]).doubleValue();current[0][4]=((Number)args[1]).doubleValue();}
                        else if(method.getName().equals("setColor")){for(int i=0;i<4;i++)current[0][5+i]=((Number)args[i]).doubleValue()/(args[i] instanceof Integer?255:1);}
                        return proxy;
                    });particle.render(consumer,camera,.35f);var expected=frame.getAsJsonArray("vertices");boolean same=captured.size()==expected.size();
                    for(int i=0;i<captured.size()&&i<expected.size();i++){var row=expected.get(i).getAsJsonArray();for(int j=0;j<9;j++)same&=Math.abs(captured.get(i)[j]-row.get(j).getAsDouble())<=(j<3?1e-4:j<5?1e-7:1d/255);}
                    if(!same){result.add("actual_vertices",new Gson().toJsonTree(captured));result.add("expected_vertices",expected);}
                    require(same,"four source transformed vertices, UVs, colour and half-alpha "+draws+" tick"+tick,checks);
                }
                var builder=particle.getRenderType().begin(Tesselator.getInstance(),mc.getTextureManager());particle.render(builder,camera,.35f);BufferUploader.drawWithShader(builder.buildOrThrow());
                require(org.lwjgl.opengl.GL11.glGetError()==0,"native particle shader GPU submission "+draws,checks);draws++;
            }
            require(draws==64,"all64 source rune profiles rendered",checks);
            require(org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB)==org.lwjgl.opengl.GL11.GL_SRC_ALPHA&&org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB)==org.lwjgl.opengl.GL11.GL_ONE,"source additive rune blend state",checks);
            var payload=new JsonObject();for(String path:List.of("thaumcraft/client/PavingRuneParticle.class","thaumcraft/client/LegacyParticles.class","thaumcraft/client/R4Client.class","thaumcraft/blocks/PavingStoneBlock.class","thaumcraft/blocks/PavingStoneBlock$Rune.class"))try(var stream=ClientPavingVerification.class.getClassLoader().getResourceAsStream(path)){if(stream==null)throw new AssertionError("Missing loaded payload "+path);payload.addProperty(path,sha(stream.readAllBytes()));}
            try(var original=new java.util.jar.JarFile(root.resolve("Thaumcraft-1.7.10-4.2.3.5.jar").toFile());var texture=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/misc/particles.png")).open()){
                var bytes=texture.readAllBytes();require(Arrays.equals(bytes,original.getInputStream(original.getJarEntry("assets/thaumcraft/textures/misc/particles.png")).readAllBytes()),"actual rune/sparkle atlas equals owned original texture",checks);payload.addProperty("assets/thaumcraft/textures/misc/particles.png",sha(bytes));
            }
            result.add("payload_sha256",payload);result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));result.addProperty("passed",true);
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());failure.printStackTrace();}
        result.addProperty("count",checks.size());result.add("checks",new Gson().toJsonTree(checks));result.addProperty("time",java.time.Instant.now().toString());result.addProperty("manual_pixel_acceptance","pending");
        Files.writeString(root.resolve("analysis/full_port/paving-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private static Field field(Object object,String name)throws Exception{for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{var f=type.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(name);}
    private static Object get(Object object,String name)throws Exception{return field(object,name).get(object);}
    private static void set(Object object,String name,Object value)throws Exception{field(object,name).set(object,value);}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static void require(boolean value,String label,List<String> checks){if(!value)throw new AssertionError(label);checks.add(label);}
}

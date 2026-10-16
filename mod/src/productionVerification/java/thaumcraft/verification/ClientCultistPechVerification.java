package thaumcraft.verification;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import thaumcraft.client.PechModel;
import thaumcraft.entities.R5Entities;

/** Compares native geometry and animation with execution of the owned original model. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ClientCultistPechVerification {
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception {
        if(done||!"cultists-pech-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=net.minecraft.client.Minecraft.getInstance();
        if(!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)||mc.getOverlay()!=null)return;
        done=true;
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));
        var result=new JsonObject();var checks=new ArrayList<String>();
        try {
            var reference=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-pech-model.json"))).getAsJsonObject();
            var originals=reference.getAsJsonObject("parts");
            var model=new PechModel();
            var parts=new LinkedHashMap<String,ModelPart>();
            parts.put("Body",model.body);parts.put("RightLeg",model.rightLeg);parts.put("LeftLeg",model.leftLeg);
            parts.put("Head",model.head);parts.put("Jowls",model.jowls);parts.put("LowerPack",model.lowerPack);
            parts.put("UpperPack",model.upperPack);parts.put("RightArm",model.rightArm);parts.put("LeftArm",model.leftArm);
            require(originals.keySet().equals(parts.keySet()),"all nine original parts",checks);
            for(var entry:parts.entrySet()) {
                var original=originals.getAsJsonObject(entry.getKey());var part=entry.getValue();
                for(int i=0;i<3;i++) {
                    near(new float[]{part.x,part.y,part.z}[i],original.getAsJsonArray("pivot").get(i).getAsFloat(),"pivot "+entry.getKey());
                    near(new float[]{part.xRot,part.yRot,part.zRot}[i],original.getAsJsonArray("rotation").get(i).getAsFloat(),"rotation "+entry.getKey());
                }
                require(true,"original pivot and default rotation "+entry.getKey(),checks);
                part.x=part.y=part.z=part.xRot=part.yRot=part.zRot=0;
                var capture=new Capture();part.render(new PoseStack(),capture,240,OverlayTexture.NO_OVERLAY);
                compare(capture.vertices,original.getAsJsonArray("boxes").get(0).getAsJsonObject().getAsJsonArray("vertices"),new Matrix4f());
                part.resetPose();require(true,"original cuboid vertices, mirrored faces and UVs "+entry.getKey(),checks);
            }
            int index=0;
            for(var value:reference.getAsJsonArray("cases")) {
                var row=value.getAsJsonObject();
                model.animate(row.get("walk").getAsFloat(),row.get("amount").getAsFloat(),row.get("age").getAsFloat(),row.get("yaw").getAsFloat(),row.get("pitch").getAsFloat(),row.get("mumble").getAsFloat(),row.get("riding").getAsBoolean(),row.get("crouching").getAsBoolean(),row.get("attack").getAsFloat());
                var angles=row.getAsJsonObject("rotation");
                for(var entry:parts.entrySet()) {
                    var part=entry.getValue();var expected=angles.getAsJsonArray(entry.getKey());
                    near(part.xRot,expected.get(0).getAsFloat(),"X case"+index+" "+entry.getKey());
                    near(part.yRot,expected.get(1).getAsFloat(),"Y case"+index+" "+entry.getKey());
                    near(part.zRot,expected.get(2).getAsFloat(),"Z case"+index+" "+entry.getKey());
                }
                require(true,"original walk, idle, mumble, riding, crouching and attack case"+index,checks);
                var capture=new Capture();model.renderToBuffer(new PoseStack(),capture,240,OverlayTexture.NO_OVERLAY,-1);
                require(capture.vertices.size()==216,"nine cuboids rendered in source order case"+index,checks);
                int offset=0;
                for(var name:row.getAsJsonArray("draws")) {
                    var original=originals.getAsJsonObject(name.getAsString());var pivot=original.getAsJsonArray("pivot");var rotation=angles.getAsJsonArray(name.getAsString());
                    var matrix=new Matrix4f().translate(pivot.get(0).getAsFloat()/16,pivot.get(1).getAsFloat()/16,pivot.get(2).getAsFloat()/16).rotateZYX(rotation.get(2).getAsFloat(),rotation.get(1).getAsFloat(),rotation.get(0).getAsFloat());
                    compare(capture.vertices.subList(offset,offset+24),original.getAsJsonArray("boxes").get(0).getAsJsonObject().getAsJsonArray("vertices"),matrix);offset+=24;
                }
                require(true,"complete transformed original geometry case"+index++,checks);
            }
            require(index==432,"all 432 independently executed original animation cases",checks);
            var dispatcherField=net.minecraft.client.renderer.entity.EntityRenderDispatcher.class.getDeclaredField("renderers");dispatcherField.setAccessible(true);
            var registered=(Map<?,?>)dispatcherField.get(mc.getEntityRenderDispatcher());
            for(var pair:List.of(Map.entry(R5Entities.PECH.get(),"PechRenderer"),Map.entry(R5Entities.CULTIST_KNIGHT.get(),"CultistRenderer"),Map.entry(R5Entities.CULTIST_CLERIC.get(),"CultistRenderer")))
                require(registered.get(pair.getKey()).getClass().getName().endsWith(pair.getValue()),"native registered "+pair.getValue(),checks);
            for(String skin:List.of("pech_forage","pech_thaum","pech_stalker")) {
                var texture=ResourceLocation.parse("thaumcraft:textures/models/"+skin+".png");
                try(var stream=mc.getResourceManager().open(texture);var image=com.mojang.blaze3d.platform.NativeImage.read(stream)) {
                    require(image.getWidth()==128&&image.getHeight()==64,"original texture dimensions "+skin,checks);
                }
                var pose=new PoseStack();pose.translate(0,0,-100);
                model.renderToBuffer(pose,mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(texture)),240,OverlayTexture.NO_OVERLAY,-1);
                mc.renderBuffers().bufferSource().endBatch();require(org.lwjgl.opengl.GL11.glGetError()==0,"native Pech GPU draw "+skin,checks);
            }
            result.addProperty("passed",true);result.addProperty("driver",org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());failure.printStackTrace();}
        var hashes=new JsonObject();
        for(String resource:List.of("thaumcraft/client/PechModel.class","thaumcraft/client/PechRenderer.class","thaumcraft/client/CultistRenderer.class","assets/thaumcraft/textures/models/pech_forage.png","assets/thaumcraft/textures/models/pech_thaum.png","assets/thaumcraft/textures/models/pech_stalker.png")) {
            try(var stream=ClientCultistPechVerification.class.getClassLoader().getResourceAsStream(resource)) {
                if(stream==null)throw new IllegalStateException("Missing payload "+resource);
                hashes.addProperty(resource,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
            }
        }
        result.add("payload_sha256",hashes);result.add("checks",new Gson().toJsonTree(checks));result.addProperty("count",checks.size());result.addProperty("manual_acceptance","pending");result.addProperty("time",java.time.Instant.now().toString());
        Files.writeString(root.resolve("analysis/full_port/cultists-pech-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");mc.stop();
    }
    private static void compare(List<float[]> actual,JsonArray expected,Matrix4f matrix) {
        if(actual.size()!=24)throw new AssertionError("Expected 24 cuboid vertices");var remaining=new ArrayList<>(actual);
        for(var entry:expected) {
            var row=entry.getAsJsonArray();var point=new Vector3f(row.get(0).getAsFloat(),row.get(1).getAsFloat(),row.get(2).getAsFloat());matrix.transformPosition(point);
            float[] target={point.x,point.y,point.z,row.get(3).getAsFloat(),row.get(4).getAsFloat()};boolean found=false;
            for(var it=remaining.iterator();it.hasNext();) {var vertex=it.next();boolean same=true;for(int i=0;i<5;i++)same&=Math.abs(vertex[i]-target[i])<2e-6;if(same){it.remove();found=true;break;}}
            if(!found)throw new AssertionError("Original position/UV missing "+Arrays.toString(target));
        }
    }
    private static void near(float actual,float expected,String message){if(!Float.isFinite(actual)||Math.abs(actual-expected)>2e-6)throw new AssertionError(message+": "+actual+" / "+expected);}
    private static void require(boolean condition,String message,List<String> checks){if(!condition)throw new AssertionError(message);checks.add(message);}
    private static final class Capture implements VertexConsumer {
        final List<float[]> vertices=new ArrayList<>();
        public VertexConsumer addVertex(float x,float y,float z){vertices.add(new float[]{x,y,z,0,0});return this;}
        public VertexConsumer setUv(float u,float v){var row=vertices.getLast();row[3]=u;row[4]=v;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
    private ClientCultistPechVerification(){}
}

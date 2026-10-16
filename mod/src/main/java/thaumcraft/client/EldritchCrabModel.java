package thaumcraft.client;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import thaumcraft.entities.EldritchCrab;

/** All 21 original cuboids, pivots, mirroring and UVs; no vanilla model substitution. */
final class EldritchCrabModel extends HierarchicalModel<EldritchCrab> {
    private final ModelPart root;
    EldritchCrabModel(){root=bake();}
    private static ModelPart bake(){try(var stream=EldritchCrabModel.class.getResourceAsStream("/assets/thaumcraft/entity_models/eldritch_crab.json")){if(stream==null)throw new IOException("Missing original crab mesh");var data=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var mesh=new MeshDefinition();for(var element:data.getAsJsonArray("parts")){var part=element.getAsJsonObject();var uv=part.getAsJsonArray("uv");var cubes=CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).mirror(part.get("mirror").getAsBoolean());for(var entry:part.getAsJsonArray("boxes")){var box=entry.getAsJsonArray();cubes.addBox(f(box,0),f(box,1),f(box,2),f(box,3),f(box,4),f(box,5));}var p=part.getAsJsonArray("pivot");var r=part.getAsJsonArray("rotation");mesh.getRoot().addOrReplaceChild(part.get("name").getAsString(),cubes,PartPose.offsetAndRotation(f(p,0),f(p,1),f(p,2),f(r,0),f(r,1),f(r,2)));}return LayerDefinition.create(mesh,128,64).bakeRoot();}catch(IOException e){throw new UncheckedIOException(e);}}
    private static float f(JsonArray array,int i){return array.get(i).getAsFloat();}
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(EldritchCrab crab,float walk,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.getChild("TailHelm").visible=crab.hasHelm();root.getChild("TailBare").visible=!crab.hasHelm();float rear=-(float)Math.cos(walk*.6662f*2)*.4f*amount,front=-(float)Math.cos(walk*.6662f*2+(float)Math.PI)*.4f*amount;for(int segment=0;segment<2;segment++){leg("RRLeg"+segment,.2094395f,.4363323f,rear);leg("RFLeg"+segment,-.2094395f,.4363323f,front);leg("LRLeg"+segment,-.2094395f,-.4363323f,-rear);leg("LFLeg"+segment,.2094395f,-.4363323f,-front);}float swing=(float)Math.cos(walk*.6662f)*amount*.125f;for(String tail:java.util.List.of("TailHelm","TailBare")){root.getChild(tail).yRot=swing*2;root.getChild(tail).zRot=swing;}root.getChild("RClaw2").xRot=.3141593f-(float)Math.sin(crab.tickCount/4f)*.25f;root.getChild("LClaw2").xRot=.3141593f+(float)Math.sin(crab.tickCount/4.1f)*.25f;root.getChild("RClaw1").xRot=(float)Math.sin(crab.tickCount/4f)*.125f;root.getChild("LClaw1").xRot=-(float)Math.sin(crab.tickCount/4.1f)*.125f;}
    private void leg(String name,float yaw,float roll,float swing){var part=root.getChild(name);part.yRot=yaw+swing;part.zRot=roll+swing;}
}

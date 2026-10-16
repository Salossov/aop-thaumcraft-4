package thaumcraft.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import thaumcraft.entities.EldritchGolem;

/** Original 36 cuboids and eight child joints, including the original duplicate cloth draw. */
final class EldritchGolemModel extends EntityModel<EldritchGolem> {
    private final Map<String,ModelPart> parts=new LinkedHashMap<>();
    private final ModelPart root;
    private boolean headless;
    private static final String[] ORDER={"Frontcloth1","CollarL","CollarBlack","Cloak1","CloakCL","CloakCR","Cloak3","Cloak2","Head","Frontcloth0","CollarB","Torso","CollarR","CollarF","Frontcloth1","ArmL","ArmR","BackpanelR1","WaistR1","WaistR2","WaistR3","LegR","WaistL1","WaistL2","WaistL3","Frontcloth2","BackpanelL1","LegL"};
    EldritchGolemModel(){
        try(var input=getClass().getResourceAsStream("/assets/thaumcraft/entity_models/eldritch_golem.json")){
            if(input==null)throw new IOException("Missing original construct mesh");
            var data=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();var mesh=new MeshDefinition();var definitions=new HashMap<String,PartDefinition>();definitions.put("",mesh.getRoot());var pending=new ArrayList<JsonObject>();for(var entry:data.getAsJsonArray("parts"))pending.add(entry.getAsJsonObject());
            while(!pending.isEmpty()){boolean progress=false;for(var it=pending.iterator();it.hasNext();){var p=it.next();String parent=p.get("parent").isJsonNull()?"":p.get("parent").getAsString();if(!definitions.containsKey(parent))continue;var uv=p.getAsJsonArray("uv");var cubes=CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt());for(var entry:p.getAsJsonArray("boxes")){var box=entry.getAsJsonObject();var b=box.getAsJsonArray("bounds");cubes.mirror(box.get("mirror").getAsBoolean()).addBox(f(b,0),f(b,1),f(b,2),f(b,3),f(b,4),f(b,5));}var pivot=p.getAsJsonArray("pivot");var rotation=p.getAsJsonArray("rotation");String name=p.get("name").getAsString();definitions.put(name,definitions.get(parent).addOrReplaceChild(name,cubes,PartPose.offsetAndRotation(f(pivot,0),f(pivot,1),f(pivot,2),f(rotation,0),f(rotation,1),f(rotation,2))));it.remove();progress=true;}if(!progress)throw new IOException("Cyclic original construct joints");}
            root=LayerDefinition.create(mesh,128,64).bakeRoot();for(var entry:data.getAsJsonArray("parts")){var p=entry.getAsJsonObject();var owner=p.get("parent").isJsonNull()?root:parts.get(p.get("parent").getAsString());parts.put(p.get("name").getAsString(),owner.getChild(p.get("name").getAsString()));}
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static float f(JsonArray a,int i){return a.get(i).getAsFloat();}
    private ModelPart part(String name){return parts.get(name);}
    @Override public void prepareMobModel(EldritchGolem golem,float walk,float amount,float partial){animateArms(golem.getAttackTimer(),walk,amount,partial);}
    private void animateArms(int timer,float walk,float amount,float partial){if(timer>0){float angle=-2+1.5f*((Math.abs((timer-partial)%10-5)-2.5f)/2.5f);part("ArmR").xRot=angle;part("ArmL").xRot=angle;}else{part("ArmR").xRot=Mth.cos(walk*.4f+(float)Math.PI)*2*amount*.5f;part("ArmL").xRot=Mth.cos(walk*.4f)*2*amount*.5f;}}
    @Override public void setupAnim(EldritchGolem golem,float walk,float amount,float age,float yaw,float pitch){
        animate(golem.getSpawnTimer(),golem.isHeadless(),walk,amount,yaw,pitch);
    }
    private void animate(int spawn,boolean headless,float walk,float amount,float yaw,float pitch){
        this.headless=headless;if(spawn>0){part("Head").yRot=0;part("Head").xRot=(float)(spawn/2)/57.295776f;}else{part("Head").yRot=yaw/4/57.295776f;part("Head").xRot=pitch/2/57.295776f;part("Head2").yRot=yaw/57.295776f;part("Head2").xRot=pitch/57.295776f;}
        part("LegR").xRot=Mth.cos(walk*.4662f)*1.4f*amount;part("LegL").xRot=Mth.cos(walk*.4662f+(float)Math.PI)*1.4f*amount;
        float c=Math.min(Mth.cos(walk*.44f)*1.4f*amount,Mth.cos(walk*.44f+(float)Math.PI)*1.4f*amount);part("Frontcloth1").xRot=c-.1047198f;part("Frontcloth2").xRot=c-.3316126f;part("Cloak1").xRot=-c/3+.1396263f;part("Cloak2").xRot=-c/3+.3069452f;part("Cloak3").xRot=-c/3+.4465716f;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){
        for(String name:ORDER){var bone=part(name.equals("Head")&&headless?"Head2":name);if(!name.equals("ArmL")&&!name.equals("ArmR")){bone.render(pose,vertices,light,overlay,color);continue;}
            // Native ModelPart's child map does not retain TC4's render insertion order.
            String side=name.equals("ArmL")?"L":"R";String[] children={"Shoulder"+side,"Shoulder"+side+"0","Shoulder"+side+"1","Shoulder"+side+"2"};boolean[] visible=new boolean[4];for(int i=0;i<4;i++){visible[i]=part(children[i]).visible;part(children[i]).visible=false;}
            bone.render(pose,vertices,light,overlay,color);for(int i=0;i<4;i++)part(children[i]).visible=visible[i];if(bone.visible){pose.pushPose();bone.translateAndRotate(pose);for(String child:children)part(child).render(pose,vertices,light,overlay,color);pose.popPose();}
        }
    }
}

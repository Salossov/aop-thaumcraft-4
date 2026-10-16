package thaumcraft.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.equipment.FortressArmor;
import thaumcraft.items.ThaumicArmorItem;

/** Original TC4 cuboids and UVs, expressed as native 1.21.1 model parts. */
public final class OriginalArmorGeometry extends HumanoidModel<LivingEntity> {
    private final Map<String,ModelPart> pieces=new HashMap<>();
    private OriginalArmorGeometry(ModelPart root,JsonArray definitions){
        super(root);for(String name:List.of("head","hat","body","right_arm","left_arm","right_leg","left_leg"))pieces.put(name,root.getChild(name));
        var pending=new ArrayList<JsonObject>();for(var definition:definitions)pending.add(definition.getAsJsonObject());
        while(!pending.isEmpty()){
            int count=pending.size();var iterator=pending.iterator();while(iterator.hasNext()){
                var definition=iterator.next();var parent=pieces.get(definition.get("parent").getAsString());if(parent==null)continue;
                String name=definition.get("name").getAsString();pieces.put(name,parent.getChild(name));iterator.remove();
            }if(pending.size()==count)throw new IllegalArgumentException("Invalid baked armor hierarchy");
        }
    }
    public static OriginalArmorGeometry bake(String id,boolean inner){
        String resource="/assets/thaumcraft/armor_models/"+id+(inner?"_inner":"_outer")+".json";
        try(var stream=OriginalArmorGeometry.class.getResourceAsStream(resource)){
            if(stream==null)throw new IOException("Missing original armor geometry "+resource);
            var data=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var mesh=new MeshDefinition();var root=mesh.getRoot();var definitions=new HashMap<String,PartDefinition>();
            definitions.put("head",root.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO));
            definitions.put("hat",root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO));
            definitions.put("body",root.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO));
            definitions.put("right_arm",root.addOrReplaceChild("right_arm",CubeListBuilder.create(),PartPose.offset(-5,2,0)));
            definitions.put("left_arm",root.addOrReplaceChild("left_arm",CubeListBuilder.create(),PartPose.offset(5,2,0)));
            definitions.put("right_leg",root.addOrReplaceChild("right_leg",CubeListBuilder.create(),PartPose.offset(-1.9f,12,0)));
            definitions.put("left_leg",root.addOrReplaceChild("left_leg",CubeListBuilder.create(),PartPose.offset(1.9f,12,0)));
            var pending=new ArrayList<JsonObject>();for(var entry:data.getAsJsonArray("parts"))pending.add(entry.getAsJsonObject());
            while(!pending.isEmpty()){
                int before=pending.size();var iterator=pending.iterator();while(iterator.hasNext()){
                    var part=iterator.next();var parent=definitions.get(part.get("parent").getAsString());if(parent==null)continue;
                    var uv=part.getAsJsonArray("uv");var cubes=CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).mirror(part.get("mirror").getAsBoolean());
                    for(var value:part.getAsJsonArray("boxes")){var b=value.getAsJsonArray();cubes.addBox(f(b,0),f(b,1),f(b,2),f(b,3),f(b,4),f(b,5),new CubeDeformation(b.size()>6?f(b,6):0));}
                    var pivot=part.getAsJsonArray("pivot");var rotation=part.getAsJsonArray("rotation");String name=part.get("name").getAsString();
                    definitions.put(name,parent.addOrReplaceChild(name,cubes,PartPose.offsetAndRotation(f(pivot,0),f(pivot,1),f(pivot,2),f(rotation,0),f(rotation,1),f(rotation,2))));iterator.remove();
                }
                if(pending.size()==before)throw new IOException("Invalid armor part hierarchy "+resource);
            }
            return new OriginalArmorGeometry(LayerDefinition.create(mesh,data.get("width").getAsInt(),data.get("height").getAsInt()).bakeRoot(),data.getAsJsonArray("parts"));
        }catch(IOException error){throw new UncheckedIOException(error);}
    }
    private static float f(JsonArray a,int i){return a.get(i).getAsFloat();}
    private void visible(String name,boolean visible){var part=pieces.get(name);if(part!=null)part.visible=visible;}
    public void prepare(LivingEntity wearer,ItemStack stack){
        if(stack.getItem() instanceof ThaumicArmorItem item&&item.kind()==ThaumicArmorItem.Kind.FORTRESS){
            int set=0;for(var armor:wearer.getArmorSlots())if(armor.getItem() instanceof ThaumicArmorItem a&&a.kind()==ThaumicArmorItem.Kind.FORTRESS)set++;
            visible("Goggles",FortressArmor.goggles(wearer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)));
            int mask=FortressArmor.mask(wearer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD));for(int i=0;i<3;i++)visible("Mask"+i,mask==i);
            for(String part:List.of("Scroll","OrnamentL","OrnamentL2","OrnamentR","OrnamentR2","Gemornament","Gem","ShoulderplateL2","ShoulderplateL3","ShoulderplateR2","ShoulderplateR3","SidepanelR3","SidepanelL3"))visible(part,set>=3);
            for(String part:List.of("Book","flapL","flapR","ShoulderplateLtop","ShoulderplateL1","ShoulderplateRtop","ShoulderplateR1","SidepanelR2","SidepanelL2"))visible(part,set>=2);
        }else{
            float phase=wearer.walkAnimation.position(),speed=wearer.walkAnimation.speed(),a=(float)Math.cos(phase*.6662)*1.4f*speed,b=(float)Math.cos(phase*.6662+Math.PI)*1.4f*speed,c=Math.min(a,b);
            rotate("Frontcloth1",c-.1047198f);rotate("Frontcloth2",c-.3316126f);
            rotate("LegClothR",a-.1047198f);rotate("LegClothL",b-.1047198f);
            rotate("Cloak1",-c/2+.1396263f);rotate("Cloak2",-c/2+.3069452f);rotate("Cloak3",-c/2+.4465716f);
            for(String side:List.of("L","R")){
                rotate("Frontcloth"+side+"1",c-.1047198f);rotate("Frontcloth"+side+"2",c-.3316126f);
                rotate("ClothBack"+side+"1",-c+.1047198f);rotate("ClothBack"+side+"2",-c+.2268928f);rotate("ClothBack"+side+"3",-c+.2268928f);
            }
        }
    }
    private void rotate(String name,float rotation){var part=pieces.get(name);if(part!=null)part.xRot=rotation;}
    @Override public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer vertices,int light,int overlay,int color){if(young){super.renderToBuffer(pose,vertices,light,overlay,color);return;}pose.pushPose();pose.scale(1.01f,1.01f,1.01f);head.render(pose,vertices,light,overlay,color);pose.popPose();for(var part:bodyParts())part.render(pose,vertices,light,overlay,color);}
}

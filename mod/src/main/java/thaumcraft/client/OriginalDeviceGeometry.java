package thaumcraft.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;

/** TC4 device cuboids use their original pivots, rotations and UV rectangles. */
final class OriginalDeviceGeometry {
    static ModelPart bake(String id){String resource="/assets/thaumcraft/device_models/"+id+".json";try(var stream=OriginalDeviceGeometry.class.getResourceAsStream(resource)){if(stream==null)throw new IOException("Missing device model "+resource);var data=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var mesh=new MeshDefinition();for(var definition:data.getAsJsonArray("parts")){var part=definition.getAsJsonObject();var uv=part.getAsJsonArray("uv");var cubes=CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).mirror(part.get("mirror").getAsBoolean());for(var box:part.getAsJsonArray("boxes")){var b=box.getAsJsonArray();cubes.addBox(f(b,0),f(b,1),f(b,2),f(b,3),f(b,4),f(b,5),new CubeDeformation(b.size()>6?f(b,6):0));}var pivot=part.getAsJsonArray("pivot");var rotation=part.getAsJsonArray("rotation");mesh.getRoot().addOrReplaceChild(part.get("name").getAsString(),cubes,PartPose.offsetAndRotation(f(pivot,0),f(pivot,1),f(pivot,2),f(rotation,0),f(rotation,1),f(rotation,2)));}return LayerDefinition.create(mesh,data.get("width").getAsInt(),data.get("height").getAsInt()).bakeRoot();}catch(IOException error){throw new UncheckedIOException(error);}}
    private static float f(JsonArray values,int index){return values.get(index).getAsFloat();}
    private OriginalDeviceGeometry(){}
}

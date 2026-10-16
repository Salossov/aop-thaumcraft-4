package thaumcraft.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/** TC4's original named OBJ parts, submitted to Minecraft's normal buffered renderer. */
final class OriginalObjMesh {
    private record Vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz) {}
    private static final Map<String,OriginalObjMesh> CACHE=new HashMap<>();
    private final Map<String,List<Vertex[]>> parts=new LinkedHashMap<>();
    static void clear(){CACHE.clear();}
    static OriginalObjMesh get(String name){return CACHE.computeIfAbsent(name,OriginalObjMesh::new);}
    private OriginalObjMesh(String name) {
        var positions=new ArrayList<float[]>();var textures=new ArrayList<float[]>();var normals=new ArrayList<float[]>();String part="";
        var resource=ResourceLocation.parse("thaumcraft:textures/models/"+name+".obj");
        try(var reader=new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().open(resource),StandardCharsets.UTF_8))) {
            for(String line;(line=reader.readLine())!=null;) {
                var tokens=line.trim().split("\\s+");if(tokens.length<2)continue;
                switch(tokens[0]) {
                    case "v" -> positions.add(new float[]{Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2]),Float.parseFloat(tokens[3])});
                    case "vt" -> textures.add(new float[]{Float.parseFloat(tokens[1]),1-Float.parseFloat(tokens[2])});
                    case "vn" -> normals.add(new float[]{Float.parseFloat(tokens[1]),Float.parseFloat(tokens[2]),Float.parseFloat(tokens[3])});
                    case "g","o" -> {part=tokens[1];parts.computeIfAbsent(part,ignored->new ArrayList<>());}
                    case "f" -> {
                        var face=new ArrayList<Vertex>();
                        for(int i=1;i<tokens.length;i++) {
                            var indices=tokens[i].split("/",-1);var p=positions.get(index(indices[0],positions.size()));
                            var uv=indices.length>1&&!indices[1].isEmpty()?textures.get(index(indices[1],textures.size())):new float[]{0,0};
                            var n=indices.length>2&&!indices[2].isEmpty()?normals.get(index(indices[2],normals.size())):new float[]{0,1,0};
                            face.add(new Vertex(p[0],p[1],p[2],uv[0],uv[1],n[0],n[1],n[2]));
                        }
                        var list=parts.computeIfAbsent(part,ignored->new ArrayList<>());
                        if(face.size()==4)list.add(face.toArray(Vertex[]::new));
                        else for(int i=1;i+1<face.size();i++)list.add(new Vertex[]{face.getFirst(),face.get(i),face.get(i+1),face.get(i+1)});
                    }
                    default -> {}
                }
            }
        } catch(IOException|RuntimeException error){throw new IllegalStateException("Cannot load original TC4 model "+resource,error);}
    }
    private static int index(String text,int size){int value=Integer.parseInt(text);return value>0?value-1:size+value;}
    /** Original sonic hemisphere, with unchanged frame pixels packed vertically for native batching. */
    void renderParticles(PoseStack pose,VertexConsumer vertices,int frame,int light){for(var faces:parts.values())for(var face:faces)for(int side=0;side<2;side++)for(int i=0;i<face.length;i++){var p=face[side==0?i:face.length-i-1];vertices.addVertex(pose.last().pose(),p.x,p.y,p.z).setColor(.5f,.5f,.5f,1).setUv(p.u,(p.v+frame-1)/15f).setLight(light);}}
    void renderAll(PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,int light,int overlay,int color,boolean glow){for(var part:parts.keySet())render(part,pose,buffers,texture,light,overlay,color,glow);}
    void render(String part,PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,int light,int overlay,int color,boolean glow) {
        render(part,pose,buffers,glow?ThaumcraftRenderTypes.glow(texture):RenderType.entityCutoutNoCull(texture),light,overlay,color);
    }
    void render(String part,PoseStack pose,MultiBufferSource buffers,RenderType type,int light,int overlay,int color) {
        var vertex=buffers.getBuffer(type);
        for(var face:parts.getOrDefault(part,List.of()))for(var p:face)vertex.addVertex(pose.last().pose(),p.x,p.y,p.z).setColor(color).setUv(p.u,p.v).setOverlay(overlay).setLight(light).setNormal(pose.last(),p.nx,p.ny,p.nz);
    }
}

package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blocks.FocusMemoryBlockEntity;
import thaumcraft.registry.R3Content;

/** TC4 TileHoleRenderer: solid boundary faces, sixteen projected star layers, and exposed-edge sparkles. */
final class PortableHoleEffects {
    private static net.minecraft.client.renderer.ShaderInstance shader;
    static net.minecraft.client.renderer.ShaderInstance shader(){return shader;}
    static void registerShader(net.neoforged.neoforge.client.event.RegisterShadersEvent event)throws java.io.IOException{
        event.registerShader(new net.minecraft.client.renderer.ShaderInstance(event.getResourceProvider(),net.minecraft.resources.ResourceLocation.parse("thaumcraft:portable_hole"),com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION){
            @Override public void apply(){var time=getUniform("HoleTime");if(time!=null)time.set((System.currentTimeMillis()%700000L)/250000f);super.apply();}
        },value->shader=value);
    }
    static void install(){FocusMemoryBlockEntity.clientTick=PortableHoleEffects::tick;}
    static void tick(FocusMemoryBlockEntity tile){
        var level=tile.getLevel();if(level==null)return;var pos=tile.getBlockPos();
        for(var wall:Direction.values()){
            var solid=pos.relative(wall);if(!level.getBlockState(solid).isSolidRender(level,solid))continue;
            for(var edge:Direction.values())if(edge.getAxis()!=wall.getAxis()){
                var next=pos.relative(edge);var state=level.getBlockState(next);if(state.is(R3Content.PORTABLE_HOLE.get())||state.isSolidRender(level,next))continue;
                double[] p={level.random.nextFloat(),level.random.nextFloat(),level.random.nextFloat()};p[wall.getAxis().ordinal()]=wall.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;p[edge.getAxis().ordinal()]=edge.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
                FollowingItemEffects.sparkle(new Vec3(pos.getX()+p[0],pos.getY()+p[1],pos.getZ()+p[2]),1.5f,2,0);
            }
        }
    }
    static void render(FocusMemoryBlockEntity tile,PoseStack pose,MultiBufferSource buffers){
        var level=tile.getLevel();if(level==null)return;var origin=Vec3.atLowerCornerOf(tile.getBlockPos());var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();boolean near=camera.distanceToSqr(origin.add(.5,.5,.5))<512;
        for(var face:Direction.values()){
            var neighbor=tile.getBlockPos().relative(face);if(!level.getBlockState(neighbor).isSolidRender(level,neighbor)||level.getBlockState(neighbor).is(R3Content.PORTABLE_HOLE.get()))continue;
            int axis=face.getAxis().ordinal(),uAxis=(axis+1)%3,vAxis=(axis+2)%3;float boundary=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?.999f:.001f;
            var out=buffers.getBuffer(near?ThaumcraftRenderTypes.portableHole():ThaumcraftRenderTypes.unlit(ResearchGraphics.texture("misc/particlefield32"),false,false));
            for(int corner=0;corner<4;corner++){
                float[] p=new float[3];p[axis]=boundary;p[uAxis]=corner==1||corner==2?1:0;p[vAxis]=corner>=2?1:0;
                var vertex=out.addVertex(pose.last().pose(),p[0],p[1],p[2]);if(!near)vertex.setUv(p[uAxis],p[vAxis]).setColor(.5f,.5f,.5f,1);
            }
        }
    }
    private PortableHoleEffects(){}
}

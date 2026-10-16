package thaumcraft.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

/** TC4 gas is a full free cloud; against a ceiling its surface hangs downward. */
final class FluxGasGeometry {
    static boolean render(FluidState fluid,BlockAndTintGetter level,BlockPos pos,VertexConsumer out){
        var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.parse("thaumcraft:blocks/fluxgas"));
        float bottom=level.getBlockState(pos.above()).isFaceSturdy(level,pos.above(),Direction.DOWN)?1-Math.min(1,fluid.getAmount()/8f*.875f):0;
        float x=pos.getX()&15,y=pos.getY()&15,z=pos.getZ()&15;
        float[][][] faces={{{0,bottom,0},{1,bottom,0},{1,bottom,1},{0,bottom,1}},{{0,1,1},{1,1,1},{1,1,0},{0,1,0}},{{0,bottom,0},{0,1,0},{1,1,0},{1,bottom,0}},{{1,bottom,1},{1,1,1},{0,1,1},{0,bottom,1}},{{0,bottom,1},{0,1,1},{0,1,0},{0,bottom,0}},{{1,bottom,0},{1,1,0},{1,1,1},{1,bottom,1}}};
        Direction[] directions={Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
        int light=LevelRenderer.getLightColor(level,pos);
        for(int face=0;face<6;face++){
            var direction=directions[face];var next=pos.relative(direction);
            if(level.getFluidState(next).is(fluid.getType())||level.getBlockState(next).isFaceSturdy(level,next,direction.getOpposite()))continue;
            float shade=face==0?.5f:face==1?1:face<4?.8f:.6f;
            for(int pass=0;pass<2;pass++)for(int index=0;index<4;index++){
                int i=pass==0?index:3-index;var p=faces[face][i];float u=(i<2?0:1),v=(i==0||i==3?1:0);
                out.addVertex(x+p[0],y+p[1],z+p[2]).setColor(shade,shade,shade,1).setUv(sprite.getU(u),sprite.getV(v)).setLight(light).setNormal(direction.getStepX(),direction.getStepY(),direction.getStepZ());
            }
        }return true;
    }
    private FluxGasGeometry(){}
}

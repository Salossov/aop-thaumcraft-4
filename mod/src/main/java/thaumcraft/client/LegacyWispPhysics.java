package thaumcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.util.*;
import net.minecraft.world.phys.*;

/** FXWisp escapes toward the closest non-normal neighboring cube unless touching liquid. */
final class LegacyWispPhysics {
    private static final Direction[] FACES={Direction.WEST,Direction.EAST,Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH};
    private LegacyWispPhysics(){}
    static Vec3 pushOut(ClientLevel level,RandomSource random,Vec3 position,AABB bounds,Vec3 velocity){
        if(level==null)return velocity;var pos=BlockPos.containing(position);
        if(level.isEmptyBlock(pos)||!normal(level,pos)||liquid(level,bounds))return velocity;
        double[] distance={position.x-pos.getX(),1-(position.x-pos.getX()),position.y-pos.getY(),1-(position.y-pos.getY()),position.z-pos.getZ(),1-(position.z-pos.getZ())};
        int face=-1;double nearest=9999;
        for(int i=0;i<6;i++)if(!normal(level,pos.relative(FACES[i]))&&distance[i]<nearest){nearest=distance[i];face=i;}
        float outward=random.nextFloat()*.05f+.025f,sideways=(random.nextFloat()-random.nextFloat())*.1f;
        return switch(face){case 0->new Vec3(-outward,sideways,sideways);case 1->new Vec3(outward,sideways,sideways);case 2->new Vec3(sideways,-outward,sideways);case 3->new Vec3(sideways,outward,sideways);case 4->new Vec3(sideways,sideways,-outward);case 5->new Vec3(sideways,sideways,outward);default->velocity;};
    }
    private static boolean normal(ClientLevel level,BlockPos pos){return !level.hasChunkAt(pos)||level.getBlockState(pos).isSolidRender(level,pos);}
    private static boolean liquid(ClientLevel level,AABB bounds){
        // Preserve the old extra negative-coordinate cell and floor(max+1) bounds.
        int minX=Mth.floor(bounds.minX)-(bounds.minX<0?1:0),minY=Mth.floor(bounds.minY)-(bounds.minY<0?1:0),minZ=Mth.floor(bounds.minZ)-(bounds.minZ<0?1:0);
        var pos=new BlockPos.MutableBlockPos();for(int x=minX;x<Mth.floor(bounds.maxX+1);x++)for(int y=minY;y<Mth.floor(bounds.maxY+1);y++)for(int z=minZ;z<Mth.floor(bounds.maxZ+1);z++)if(!level.getFluidState(pos.set(x,y,z)).isEmpty())return true;
        return false;
    }
}

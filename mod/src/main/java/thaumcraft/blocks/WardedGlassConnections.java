package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import thaumcraft.registry.R4Content;

/** Source-derived 256-neighbour-mask lookup into TC4's 47 connected glass textures. */
public final class WardedGlassConnections {
    private static final int[] LOOKUP={0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,4,4,5,5,4,4,5,5,17,17,22,26,17,17,22,26,16,16,20,20,16,16,28,28,21,21,46,42,21,21,43,38,4,4,5,5,4,4,5,5,9,9,30,12,9,9,30,12,16,16,20,20,16,16,28,28,25,25,45,37,25,25,40,32,0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,4,4,5,5,4,4,5,5,17,17,22,26,17,17,22,26,7,7,24,24,7,7,10,10,29,29,44,41,29,29,39,33,4,4,5,5,4,4,5,5,9,9,30,12,9,9,30,12,7,7,24,24,7,7,10,10,8,8,36,35,8,8,34,11};
    public static int index(int mask){return LOOKUP[mask&255];}
    public static int[] tiles(BlockGetter l,BlockPos p){var result=new int[6];for(var side:Direction.values()){int mask=0,i=0;for(int row=-1;row<=1;row++)for(int col=-1;col<=1;col++){if(row==0&&col==0)continue;var at=switch(side){case DOWN,UP->p.offset(col,0,row);case NORTH->p.offset(-col,-row,0);case SOUTH->p.offset(col,-row,0);case WEST->p.offset(0,-row,col);case EAST->p.offset(0,-row,-col);};if(l.getBlockState(at).is(R4Content.WARDED_GLASS.get()))mask|=1<<i;i++;}result[side.get3DDataValue()]=index(mask);}return result;}
    private WardedGlassConnections(){}
}

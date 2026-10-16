package thaumcraft.world.dim.rooms;
import java.util.Random;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
class RoomLayoutCommon {
    static final int[][] PAT_CONNECT = new int[][]{{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1}, {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1}, {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1}, {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1}, {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1}, {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1}, {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}};
    static void placeBlock(RoomLayoutSink sink,int x,int y,int z,int code,MazeCell cell){sink.place(x,y,z,code,null);}
    static void placeBlock(RoomLayoutSink sink,int x,int y,int z,int code,Direction direction,MazeCell cell){sink.place(x,y,z,code,direction);}
    static void genObelisk(RoomLayoutSink sink,int x,int y,int z){sink.raw(x,y,z,1,1,3);for(int i=1;i<=4;i++)sink.raw(x,y+i,z,1,2,3);}
    static void generateConnections(RoomLayoutSink sink,Random random,int cx,int cz,int y,MazeCell cell,int depth,boolean tip){
        int x=cx*16,z=cz*16;
        for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
            boolean connected=switch(direction){case NORTH->cell.north;case SOUTH->cell.south;case EAST->cell.east;default->cell.west;};
            if(!connected)continue;
            for(int d=0;d<=depth;d++){
                int begin=tip&&d==depth?2:tip&&d==depth-1?1:0,end=tip&&d==depth?9:tip&&d==depth-1?10:11;
                for(int w=begin;w<end;w++)for(int h=begin;h<end;h++){
                    int code=PAT_CONNECT[h][w];if(tip&&d==depth&&code==8)continue;
                    int px=switch(direction){case EAST->x+16-d;case WEST->x+d;default->x+3+w;};
                    int pz=switch(direction){case NORTH->z+d;case SOUTH->z+16-d;default->z+3+w;};
                    sink.place(px,y+10-h,pz,code,direction);
                }
            }
        }
    }
}

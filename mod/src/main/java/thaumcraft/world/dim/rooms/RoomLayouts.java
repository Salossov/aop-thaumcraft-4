package thaumcraft.world.dim.rooms;
import java.util.*;
import thaumcraft.world.dim.MazeCell;
public final class RoomLayouts {
    /** Original MazeHandler dispatch; passage selection consumes a nextInt(1). */
    public static void generateCell(RoomLayoutSink sink,Random random,int cx,int cz,MazeCell cell){
        switch(cell.feature){
            case 1->PortalRoomLayout.generatePortal(sink,random,cx,cz,50,cell);
            case 2,3,4,5->BossRoomLayout.generateRoom(sink,random,cx,cz,50,cell);
            case 6->KeyRoomLayout.generateRoom(sink,random,cx,cz,50,cell);
            case 7->NestRoomLayout.generateRoom(sink,random,cx,cz,50,cell);
            case 8->LibraryRoomLayout.generateRoom(sink,random,cx,cz,50,cell);
            default->{random.nextInt(1);PassageRoomLayout.generateDefaultPassage(sink,random,cx,cz,50,cell);}
        }
    }
    public enum Kind {PORTAL,UPPER_LEFT,UPPER_RIGHT,LOWER_LEFT,LOWER_RIGHT}
    public static void generate(Kind kind,RoomLayoutSink sink,Random random,int cx,int cz,int y,MazeCell cell){
        switch(kind){case PORTAL->PortalRoomLayout.generatePortal(sink,random,cx,cz,y,cell);case UPPER_LEFT->LargeRoomLayout.generateUpperLeft(sink,random,cx,cz,y,cell);case UPPER_RIGHT->LargeRoomLayout.generateUpperRight(sink,random,cx,cz,y,cell);case LOWER_LEFT->LargeRoomLayout.generateLowerLeft(sink,random,cx,cz,y,cell);case LOWER_RIGHT->LargeRoomLayout.generateLowerRight(sink,random,cx,cz,y,cell);}
    }
    private RoomLayouts(){}
}

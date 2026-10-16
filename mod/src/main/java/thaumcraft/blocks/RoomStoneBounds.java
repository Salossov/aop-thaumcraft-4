package thaumcraft.blocks;

import net.minecraft.core.Direction;

/** Render-only insets. Selection and collision remain the original full cube. */
public final class RoomStoneBounds {
    public static float min(int mask,int axis){return (mask&(1<<switch(axis){case 0->Direction.WEST.get3DDataValue();case 1->Direction.DOWN.get3DDataValue();default->Direction.NORTH.get3DDataValue();}))!=0?.125f:0;}
    public static float max(int mask,int axis){return (mask&(1<<switch(axis){case 0->Direction.EAST.get3DDataValue();case 1->Direction.UP.get3DDataValue();default->Direction.SOUTH.get3DDataValue();}))!=0?.875f:1;}
    private RoomStoneBounds(){}
}

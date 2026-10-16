// Original TC4 geometry and local RNG ordering; native world operations are supplied by RoomLayoutSink.
package thaumcraft.world.dim.rooms;
import java.util.Random;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
public class BossRoomLayout
extends RoomLayoutCommon {
    static final int[][] PAT_DOORWAY = new int[][]{{0, 2, 2, 2, 2, 2, 0}, {2, 2, 9, 9, 9, 2, 2}, {2, 9, 9, 9, 9, 9, 2}, {2, 9, 9, 1, 9, 9, 2}, {2, 9, 9, 9, 9, 9, 2}, {2, 2, 9, 9, 9, 2, 2}, {0, 2, 2, 2, 2, 2, 0}};

    static void generateRoom(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        switch (cell.feature) {
            case 2: {
                LargeRoomLayout.generateUpperLeft(world, random, cx, cz, 50, cell);
                break;
            }
            case 3: {
                LargeRoomLayout.generateUpperRight(world, random, cx, cz, 50, cell);
                break;
            }
            case 4: {
                LargeRoomLayout.generateLowerLeft(world, random, cx, cz, 50, cell);
                break;
            }
            case 5: {
                LargeRoomLayout.generateLowerRight(world, random, cx, cz, 50, cell);
            }
        }
        // Only the quadrant with an external exit has a door. TC4 otherwise wrote it at world0,0.
        if (!cell.north && !cell.south && !cell.east && !cell.west) return;
        for (int a = 0; a < 7; ++a) {
            block12: for (int b = 0; b < 7; ++b) {
                int xx = 0;
                int zz = 0;
                Direction dir = null;
                if (cell.north) {
                    xx = x + 5 + a;
                    zz = z + 3;
                    dir = Direction.NORTH;
                }
                if (cell.south) {
                    xx = x + 5 + a;
                    zz = z + 13;
                    dir = Direction.SOUTH;
                }
                if (cell.east) {
                    xx = x + 13;
                    zz = z + 5 + a;
                    dir = Direction.EAST;
                }
                if (cell.west) {
                    xx = x + 3;
                    zz = z + 5 + a;
                    dir = Direction.WEST;
                }
                switch (PAT_DOORWAY[a][b]) {
                    case 1: {
                        BossRoomLayout.placeBlock(world, xx, y + 2 + b, zz, 16, cell);
                        world.lock(xx, y + 2 + b, zz, dir);
                        continue block12;
                    }
                    case 2: {
                        BossRoomLayout.placeBlock(world, xx, y + 2 + b, zz, 15, cell);
                        continue block12;
                    }
                    case 9: {
                        BossRoomLayout.placeBlock(world, xx, y + 2 + b, zz, 17, cell);
                    }
                }
            }
        }
    }
}



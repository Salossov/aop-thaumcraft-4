// Original TC4 geometry and local RNG ordering; native world operations are supplied by RoomLayoutSink.
package thaumcraft.world.dim.rooms;
import java.util.Random;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
public class KeyRoomLayout
extends RoomLayoutCommon {
    static void generateRoom(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 1; a <= 15; ++a) {
            for (b = 1; b <= 15; ++b) {
                for (c = 0; c < 13; ++c) {
                    if (a != 1 && a != 15 && b != 1 && b != 15) continue;
                    KeyRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 2; a <= 14; ++a) {
            for (b = 2; b <= 14; ++b) {
                for (c = 1; c < 12; ++c) {
                    if (a != 2 && a != 14 && b != 2 && b != 14 || a == 2 && b > 3 && b < 12 && cell.west && c < 10 || a == 14 && b > 3 && b < 12 && cell.east && c < 10 || b == 2 && a > 3 && a < 12 && cell.north && c < 10 || b == 14 && a > 3 && a < 12 && cell.south && c < 10) continue;
                    KeyRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 3; a <= 13; ++a) {
            for (b = 3; b <= 13; ++b) {
                for (c = 2; c < 11; ++c) {
                    if (a != 3 && a != 13 && b != 3 && b != 13) continue;
                    if (c > 3 && c < 9 && (a == 8 || b == 8) || c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
                        if ((a == 8 || b == 8) && c == 6) continue;
                        KeyRoomLayout.placeBlock(world, x + a, y + c, z + b, 19, cell);
                        continue;
                    }
                    KeyRoomLayout.placeBlock(world, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (a = 2; a <= 14; ++a) {
            for (b = 2; b <= 14; ++b) {
                int g;
                int q;
                KeyRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                KeyRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                KeyRoomLayout.placeBlock(world, x + a, y + 1, z + b, 2, cell);
                KeyRoomLayout.placeBlock(world, x + a, y + 13, z + b, 1, cell);
                KeyRoomLayout.placeBlock(world, x + a, y + 12, z + b, 8, cell);
                KeyRoomLayout.placeBlock(world, x + a, y + 11, z + b, 2, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (g = 0; g < q - 1; ++g) {
                        KeyRoomLayout.placeBlock(world, x + a, y + 1 + g, z + b, 2, cell);
                    }
                }
                if (a <= 3 || a >= 13 || b <= 3 || b >= 13) continue;
                q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                for (g = 0; g < q; ++g) {
                    KeyRoomLayout.placeBlock(world, x + a, y + 11 - g, z + b, 2, cell);
                }
            }
        }
        for (int g = 0; g < 5; ++g) {
            KeyRoomLayout.placeBlock(world, x + 6 + g, y + 2, z + 4, 10, Direction.NORTH, cell);
            KeyRoomLayout.placeBlock(world, x + 6 + g, y + 2, z + 12, 10, Direction.SOUTH, cell);
            KeyRoomLayout.placeBlock(world, x + 12, y + 2, z + 6 + g, 10, Direction.EAST, cell);
            KeyRoomLayout.placeBlock(world, x + 4, y + 2, z + 6 + g, 10, Direction.WEST, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
        world.raw(x + 8, y + 2, z + 8, 1, 3, 3);
        world.keyRoom(x + 8, y + 2, z + 8);
    }
}


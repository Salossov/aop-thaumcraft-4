// Original TC4 geometry and local RNG ordering; native world operations are supplied by RoomLayoutSink.
package thaumcraft.world.dim.rooms;
import java.util.Random;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
public class NestRoomLayout
extends RoomLayoutCommon {
    static void generateRoom(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 1; a <= 15; ++a) {
            for (b = 1; b <= 15; ++b) {
                for (c = 0; c < 11; ++c) {
                    if (a != 1 && a != 15 && b != 1 && b != 15) continue;
                    NestRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 2; a <= 14; ++a) {
            for (b = 2; b <= 14; ++b) {
                for (c = 1; c < 10; ++c) {
                    if (a != 2 && a != 14 && b != 2 && b != 14 || a == 2 && b > 3 && b < 12 && cell.west && c < 10 || a == 14 && b > 3 && b < 12 && cell.east && c < 10 || b == 2 && a > 3 && a < 12 && cell.north && c < 10 || b == 14 && a > 3 && a < 12 && cell.south && c < 10) continue;
                    NestRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 3; a <= 13; ++a) {
            for (b = 3; b <= 13; ++b) {
                for (c = 2; c < 9; ++c) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        NestRoomLayout.placeBlock(world, x + a, y + c, z + b, 21, cell);
                    }
                    if (!(a == 4 && !cell.west || a == 12 && !cell.east || b == 4 && !cell.north) && (b != 12 || cell.south) || !random.nextBoolean()) continue;
                    NestRoomLayout.placeBlock(world, x + a, y + c, z + b, 21, cell);
                }
            }
        }
        for (a = 2; a <= 14; ++a) {
            for (b = 2; b <= 14; ++b) {
                NestRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                NestRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                NestRoomLayout.placeBlock(world, x + a, y + 1, z + b, 21, cell);
                NestRoomLayout.placeBlock(world, x + a, y + 11, z + b, 1, cell);
                NestRoomLayout.placeBlock(world, x + a, y + 10, z + b, 8, cell);
                NestRoomLayout.placeBlock(world, x + a, y + 9, z + b, 21, cell);
                if (random.nextBoolean()) {
                    NestRoomLayout.placeBlock(world, x + a, y + 8, z + b, 21, cell);
                    continue;
                }
                if (!random.nextBoolean() || !world.isAir(x + a, y + 8, z + b)) continue;
                world.crystal(x + a, y + 8, z + b, Direction.DOWN);
            }
        }
        NestRoomLayout.placeBlock(world, x + 8, y + 2, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 3, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 4, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 7, y + 2, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 2, z + 7, 21, cell);
        NestRoomLayout.placeBlock(world, x + 9, y + 2, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 2, z + 9, 21, cell);
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 7, y + 3, z + 8, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 8, y + 3, z + 7, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 9, y + 3, z + 8, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 8, y + 3, z + 9, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 8, y + 5, z + 8, 7, cell);
        }
        NestRoomLayout.placeBlock(world, x + 8, y + 8, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 7, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 6, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 7, y + 8, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 8, z + 7, 21, cell);
        NestRoomLayout.placeBlock(world, x + 9, y + 8, z + 8, 21, cell);
        NestRoomLayout.placeBlock(world, x + 8, y + 8, z + 9, 21, cell);
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 7, y + 7, z + 8, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 8, y + 7, z + 7, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 9, y + 7, z + 8, 21, cell);
        }
        if (random.nextBoolean()) {
            NestRoomLayout.placeBlock(world, x + 8, y + 7, z + 9, 21, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
        for (a = -5; a <= 5; ++a) {
            for (b = -5; b <= 5; ++b) {
                if (!(random.nextFloat() < 0.15f) || !world.isAir(x + 8 + a, y + 2, z + 8 + b)) continue;
                float rr = random.nextFloat();
                int md = rr < 0.15f ? 2 : (rr < 0.4f ? 1 : 0);
                world.raw(x + 8 + a, y + 2, z + 8 + b, random.nextFloat() < 0.2f ? 7 : 6, md, 3);
            }
        }
    }
}



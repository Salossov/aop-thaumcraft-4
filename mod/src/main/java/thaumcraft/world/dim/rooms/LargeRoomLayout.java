// Original TC4 geometry; block rules are applied by the caller through RoomLayoutSink.
package thaumcraft.world.dim.rooms;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;

import java.util.Random;

public class LargeRoomLayout
extends RoomLayoutCommon {
    static void generateUpperLeft(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int g;
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 1; a <= 15; ++a) {
            for (b = 1; b <= 15; ++b) {
                for (c = 0; c < 13; ++c) {
                    if (a != 1 && b != 1) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 2; a <= 15; ++a) {
            for (b = 2; b <= 15; ++b) {
                for (c = 1; c < 12; ++c) {
                    if (a != 2 && b != 2 || a == 2 && b > 4 && b < 12 && cell.west && c < 10 || b == 2 && a > 4 && a < 12 && cell.north && c < 10) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 3; a <= 15; ++a) {
            for (b = 3; b <= 15; ++b) {
                for (c = 2; c < 11; ++c) {
                    if (a != 3 && b != 3) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (a = 2; a <= 15; ++a) {
            for (b = 2; b <= 15; ++b) {
                LargeRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 1, z + b, 19, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 13, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 12, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 11, z + b, 2, cell);
            }
        }
        for (g = 4; g <= 15; ++g) {
            LargeRoomLayout.placeBlock(world, x + g, y + 2, z + 4, 10, Direction.NORTH, cell);
            LargeRoomLayout.placeBlock(world, x + g, y + 10, z + 4, 11, Direction.NORTH, cell);
        }
        for (g = 4; g <= 15; ++g) {
            LargeRoomLayout.placeBlock(world, x + 4, y + 2, z + g, 10, Direction.WEST, cell);
            LargeRoomLayout.placeBlock(world, x + 4, y + 10, z + g, 11, Direction.WEST, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
    }

    static void generateUpperRight(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int g;
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 0; a <= 15; ++a) {
            for (b = 1; b <= 15; ++b) {
                for (c = 0; c < 13; ++c) {
                    if (a != 15 && b != 1) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 0; a <= 14; ++a) {
            for (b = 2; b <= 15; ++b) {
                for (c = 1; c < 12; ++c) {
                    if (a != 14 && b != 2 || a == 14 && b > 4 && b < 12 && cell.east && c < 10 || b == 2 && a > 4 && a < 12 && cell.north && c < 10) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 0; a <= 13; ++a) {
            for (b = 3; b <= 15; ++b) {
                for (c = 2; c < 11; ++c) {
                    if (a != 13 && b != 3) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (a = 0; a <= 14; ++a) {
            for (b = 2; b <= 15; ++b) {
                LargeRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 1, z + b, 19, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 13, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 12, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 11, z + b, 2, cell);
            }
        }
        for (g = 0; g <= 11; ++g) {
            LargeRoomLayout.placeBlock(world, x + g, y + 2, z + 4, 10, Direction.NORTH, cell);
            LargeRoomLayout.placeBlock(world, x + g, y + 10, z + 4, 11, Direction.NORTH, cell);
        }
        for (g = 4; g <= 15; ++g) {
            LargeRoomLayout.placeBlock(world, x + 12, y + 2, z + g, 10, Direction.EAST, cell);
            LargeRoomLayout.placeBlock(world, x + 12, y + 10, z + g, 11, Direction.EAST, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
    }

    static void generateLowerLeft(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int g;
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 1; a <= 15; ++a) {
            for (b = 0; b <= 15; ++b) {
                for (c = 0; c < 13; ++c) {
                    if (a != 1 && b != 15) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 2; a <= 15; ++a) {
            for (b = 0; b <= 14; ++b) {
                for (c = 1; c < 12; ++c) {
                    if (a != 2 && b != 14 || a == 2 && b > 4 && b < 12 && cell.west && c < 10 || b == 14 && a > 4 && a < 12 && cell.south && c < 10) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 3; a <= 15; ++a) {
            for (b = 0; b <= 13; ++b) {
                for (c = 2; c < 11; ++c) {
                    if (a != 3 && b != 13) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (a = 2; a <= 15; ++a) {
            for (b = 0; b <= 14; ++b) {
                LargeRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 1, z + b, 19, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 13, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 12, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 11, z + b, 2, cell);
            }
        }
        for (g = 4; g <= 15; ++g) {
            LargeRoomLayout.placeBlock(world, x + g, y + 2, z + 12, 10, Direction.SOUTH, cell);
            LargeRoomLayout.placeBlock(world, x + g, y + 10, z + 12, 11, Direction.SOUTH, cell);
        }
        for (g = 0; g <= 11; ++g) {
            LargeRoomLayout.placeBlock(world, x + 4, y + 2, z + g, 10, Direction.WEST, cell);
            LargeRoomLayout.placeBlock(world, x + 4, y + 10, z + g, 11, Direction.WEST, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
    }

    static void generateLowerRight(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int g;
        int c;
        int b;
        int a;
        int x = cx * 16;
        int z = cz * 16;
        for (a = 0; a <= 15; ++a) {
            for (b = 0; b <= 15; ++b) {
                for (c = 0; c < 13; ++c) {
                    if (a != 15 && b != 15) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 1, cell);
                }
            }
        }
        for (a = 0; a <= 14; ++a) {
            for (b = 0; b <= 14; ++b) {
                for (c = 1; c < 12; ++c) {
                    if (a != 14 && b != 14 || a == 14 && b > 4 && b < 12 && cell.east && c < 10 || b == 14 && a > 4 && a < 12 && cell.south && c < 10) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 8, cell);
                }
            }
        }
        for (a = 0; a <= 13; ++a) {
            for (b = 0; b <= 13; ++b) {
                for (c = 2; c < 11; ++c) {
                    if (a != 13 && b != 13) continue;
                    LargeRoomLayout.placeBlock(world, x + a, y + c, z + b, 18, cell);
                }
            }
        }
        for (a = 0; a <= 14; ++a) {
            for (b = 0; b <= 14; ++b) {
                LargeRoomLayout.placeBlock(world, x + a, y - 1, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 1, z + b, 19, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 13, z + b, 1, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 12, z + b, 8, cell);
                LargeRoomLayout.placeBlock(world, x + a, y + 11, z + b, 2, cell);
            }
        }
        for (g = 0; g <= 11; ++g) {
            LargeRoomLayout.placeBlock(world, x + g, y + 2, z + 12, 10, Direction.SOUTH, cell);
            LargeRoomLayout.placeBlock(world, x + g, y + 10, z + 12, 11, Direction.SOUTH, cell);
        }
        for (g = 0; g <= 12; ++g) {
            LargeRoomLayout.placeBlock(world, x + 12, y + 2, z + g, 10, Direction.EAST, cell);
            LargeRoomLayout.placeBlock(world, x + 12, y + 10, z + g, 11, Direction.EAST, cell);
        }
        RoomLayoutCommon.generateConnections(world, random, cx, cz, y, cell, 3, true);
    }
}


// Original TC4 geometry and local RNG ordering; native world operations are supplied by RoomLayoutSink.
package thaumcraft.world.dim.rooms;
import java.util.Random;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
public class PassageRoomLayout
extends RoomLayoutCommon {
    static void generateDefaultPassage(RoomLayoutSink world, Random random, int cx, int cz, int y, MazeCell cell) {
        int j;
        int h;
        int w;
        int mod;
        int z;
        int x;
        block55: {
            block54: {
                block53: {
                    block52: {
                        block51: {
                            block50: {
                                block49: {
                                    block48: {
                                        x = cx * 16;
                                        z = cz * 16;
                                        PassageRoomLayout.generateConnections(world, random, cx, cz, y, cell, 4, false);
                                        mod = 0;
                                        if (cell.north && cell.south && cell.west && cell.east && random.nextBoolean()) {
                                            mod = 1;
                                        }
                                        for (w = 1; w < 8; ++w) {
                                            for (h = 1; h < 8; ++h) {
                                                if (w == 4 && h == 4 && mod == 1) {
                                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 2, z + 4 + h, 7, cell);
                                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 8, z + 4 + h, 7, cell);
                                                } else {
                                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 2, z + 4 + h, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 8, z + 4 + h, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                                                }
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y, z + 4 + h, 1, cell);
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 10, z + 4 + h, 1, cell);
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 1, z + 4 + h, 8, cell);
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9, z + 4 + h, 8, cell);
                                            }
                                        }
                                        if (!cell.north) break block48;
                                        for (w = 2 + mod; w < 9 - mod; ++w) {
                                            for (h = 2 + mod; h < 9 - mod; ++h) {
                                                PassageRoomLayout.placeBlock(world, x + 3 + w, y + 10 - h, z + 5, PAT_CONNECT[h][w], Direction.NORTH, cell);
                                            }
                                        }
                                        if (mod != 0) break block49;
                                        if (cell.west) {
                                            PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 6, 3, Direction.EAST, cell);
                                            PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 6, 5, Direction.EAST, cell);
                                        }
                                        if (!cell.east) break block49;
                                        PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 6, 3, Direction.EAST, cell);
                                        PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 6, 5, Direction.EAST, cell);
                                        break block49;
                                    }
                                    for (w = 1; w < 8; ++w) {
                                        for (h = 1; h < 8; ++h) {
                                            PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 5, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                                            PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 4, 8, cell);
                                            PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 3, 1, cell);
                                            if (h == 7) {
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 1, z + 4, 1, cell);
                                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9, z + 4, 1, cell);
                                            }
                                            if (w != 7) continue;
                                            PassageRoomLayout.placeBlock(world, x + 4, y + 9 - h, z + 4, 1, cell);
                                            PassageRoomLayout.placeBlock(world, x + 12, y + 9 - h, z + 4, 1, cell);
                                        }
                                    }
                                    for (w = 2; w < 7; ++w) {
                                        PassageRoomLayout.placeBlock(world, x + 4 + w, y + 3, z + 6, 3, Direction.EAST, cell);
                                        PassageRoomLayout.placeBlock(world, x + 4 + w, y + 7, z + 6, 5, Direction.EAST, cell);
                                    }
                                }
                                if (!cell.south) break block50;
                                for (w = 2 + mod; w < 9 - mod; ++w) {
                                    for (h = 2 + mod; h < 9 - mod; ++h) {
                                        PassageRoomLayout.placeBlock(world, x + 3 + w, y + 10 - h, z + 11, PAT_CONNECT[h][w], Direction.SOUTH, cell);
                                    }
                                }
                                if (mod != 0) break block51;
                                if (cell.west) {
                                    PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 10, 4, Direction.EAST, cell);
                                    PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 10, 6, Direction.EAST, cell);
                                }
                                if (!cell.east) break block51;
                                PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 10, 4, Direction.EAST, cell);
                                PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 10, 6, Direction.EAST, cell);
                                break block51;
                            }
                            for (w = 1; w < 8; ++w) {
                                for (h = 1; h < 8; ++h) {
                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 11, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 12, 8, cell);
                                    PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9 - h, z + 13, 1, cell);
                                    if (h == 7) {
                                        PassageRoomLayout.placeBlock(world, x + 4 + w, y + 1, z + 12, 1, cell);
                                        PassageRoomLayout.placeBlock(world, x + 4 + w, y + 9, z + 12, 1, cell);
                                    }
                                    if (w != 7) continue;
                                    PassageRoomLayout.placeBlock(world, x + 4, y + 9 - h, z + 12, 1, cell);
                                    PassageRoomLayout.placeBlock(world, x + 12, y + 9 - h, z + 12, 1, cell);
                                }
                            }
                            for (w = 2; w < 7; ++w) {
                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 3, z + 10, 4, Direction.EAST, cell);
                                PassageRoomLayout.placeBlock(world, x + 4 + w, y + 7, z + 10, 6, Direction.EAST, cell);
                            }
                        }
                        if (!cell.east) break block52;
                        for (w = 2 + mod; w < 9 - mod; ++w) {
                            for (h = 2 + mod; h < 9 - mod; ++h) {
                                PassageRoomLayout.placeBlock(world, x + 11, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.EAST, cell);
                            }
                        }
                        if (mod != 0) break block53;
                        if (cell.north) {
                            PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 6, 4, Direction.NORTH, cell);
                            PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 6, 6, Direction.NORTH, cell);
                        }
                        if (!cell.south) break block53;
                        PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 10, 4, Direction.NORTH, cell);
                        PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 10, 6, Direction.NORTH, cell);
                        break block53;
                    }
                    for (w = 1; w < 8; ++w) {
                        for (h = 1; h < 8; ++h) {
                            PassageRoomLayout.placeBlock(world, x + 11, y + 9 - h, z + 4 + w, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                            PassageRoomLayout.placeBlock(world, x + 12, y + 9 - h, z + 4 + w, 8, cell);
                            PassageRoomLayout.placeBlock(world, x + 13, y + 9 - h, z + 4 + w, 1, cell);
                            if (h == 7) {
                                PassageRoomLayout.placeBlock(world, x + 12, y + 1, z + 4 + w, 1, cell);
                                PassageRoomLayout.placeBlock(world, x + 12, y + 9, z + 4 + w, 1, cell);
                            }
                            if (w != 7) continue;
                            PassageRoomLayout.placeBlock(world, x + 12, y + 9 - h, z + 4, 1, cell);
                            PassageRoomLayout.placeBlock(world, x + 12, y + 9 - h, z + 12, 1, cell);
                        }
                    }
                    for (w = 2; w < 7; ++w) {
                        PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 4 + w, 4, Direction.NORTH, cell);
                        PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 4 + w, 6, Direction.NORTH, cell);
                    }
                }
                if (!cell.west) break block54;
                for (w = 2 + mod; w < 9 - mod; ++w) {
                    for (h = 2 + mod; h < 9 - mod; ++h) {
                        PassageRoomLayout.placeBlock(world, x + 5, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.WEST, cell);
                    }
                }
                if (mod != 0) break block55;
                if (cell.north) {
                    PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 6, 3, Direction.NORTH, cell);
                    PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 6, 5, Direction.NORTH, cell);
                }
                if (!cell.south) break block55;
                PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 10, 3, Direction.NORTH, cell);
                PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 10, 5, Direction.NORTH, cell);
                break block55;
            }
            for (w = 1; w < 8; ++w) {
                for (h = 1; h < 8; ++h) {
                    PassageRoomLayout.placeBlock(world, x + 5, y + 9 - h, z + 4 + w, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    PassageRoomLayout.placeBlock(world, x + 4, y + 9 - h, z + 4 + w, 8, cell);
                    PassageRoomLayout.placeBlock(world, x + 3, y + 9 - h, z + 4 + w, 1, cell);
                    if (h == 7) {
                        PassageRoomLayout.placeBlock(world, x + 4, y + 1, z + 4 + w, 1, cell);
                        PassageRoomLayout.placeBlock(world, x + 4, y + 9, z + 4 + w, 1, cell);
                    }
                    if (w != 7) continue;
                    PassageRoomLayout.placeBlock(world, x + 4, y + 9 - h, z + 4, 1, cell);
                    PassageRoomLayout.placeBlock(world, x + 4, y + 9 - h, z + 12, 1, cell);
                }
            }
            for (w = 2; w < 7; ++w) {
                PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 4 + w, 3, Direction.NORTH, cell);
                PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 4 + w, 5, Direction.NORTH, cell);
            }
        }
        if (mod == 1) {
            PassageRoomLayout.placeBlock(world, x + 5, y + 3, z + 5, 3, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 5, y + 7, z + 5, 5, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 5, y + 3, z + 6, 3, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 5, y + 7, z + 6, 5, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 3, z + 5, 3, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 7, z + 5, 5, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 3, z + 6, 4, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 7, z + 6, 6, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 5, y + 3, z + 11, 3, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 5, y + 7, z + 11, 5, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 6, y + 3, z + 11, 4, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 6, y + 7, z + 11, 6, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 3, z + 11, 4, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 11, y + 7, z + 11, 6, Direction.NORTH, cell);
            PassageRoomLayout.placeBlock(world, x + 10, y + 3, z + 11, 4, Direction.EAST, cell);
            PassageRoomLayout.placeBlock(world, x + 10, y + 7, z + 11, 6, Direction.EAST, cell);
        }
        if (cell.feature == 12) {
            for (w = -4; w <= 4; ++w) {
                for (h = -4; h < 5; ++h) {
                    for (j = -4; j <= 4; ++j) {
                        if (!world.crustCandidate(x + 8 + w, y + 4 + h, z + 8 + j) || !random.nextBoolean()) continue;
                        PassageRoomLayout.placeBlock(world, x + 8 + w, y + 4 + h, z + 8 + j, 21, cell);
                    }
                }
            }
        }
        if (cell.feature == 13) {
            for (w = -4; w <= 4; ++w) {
                for (h = -3; h <= 3; ++h) {
                    for (j = -4; j <= 4; ++j) {
                        if (!world.isAir(x + 8 + w, y + 4 + h, z + 8 + j) || !world.adjacentSolid( x + 8 + w, y + 4 + h, z + 8 + j)) continue;
                        if (random.nextInt(3) != 0) {
                            world.raw(x + 8 + w, y + 4 + h, z + 8 + j, 8, random.nextInt(4) == 0 ? 1 : 0, 3);
                        }
                        world.taintBiome(x + 8 + w, z + 8 + j);
                    }
                }
            }
        }
        if (cell.feature == 14) {
            for (w = -3; w <= 3; ++w) {
                for (h = -3; h <= 3; ++h) {
                    for (j = -3; j <= 3; ++j) {
                        if (world.isAir(x + 8 + w, y + 4 + h, z + 8 + j) && random.nextFloat() < 0.35f) {
                            world.raw(x + 8 + w, y + 4 + h, z + 8 + j, 9, 0, 3);
                        }
                        world.mindSpiderSpawner(x + 8, y + 4, z + 8);

                    }
                }
            }
        }
    }
}



package thaumcraft.world.dim.rooms;

/** Original lock encounter layouts and RNG order; engine state lives in NativeArenaSink. */
public final class ArenaRoomLayout {
    private static final int[][] PED={{2,2,2},{0,-1,1},{3,3,3}};
    public interface ArenaSink {
        void raw(int x,int y,int z,int block,int metadata,int flags);
        void obelisk(int x,int y,int z);
        boolean isAir(int x,int y,int z);
        boolean adjacentSolid(int x,int y,int z);
        void taintBiome(int x,int z);
        void boss(int kind,int x,int y,int z);
    }
    public static void generate(ArenaSink sink,net.minecraft.util.RandomSource random,int cx,int cz,int exit,int kind){switch(kind){case 0->golem(sink,random,cx,cz,exit);case 1->warden(sink,random,cx,cz,exit);case 2->cultist(sink,random,cx,cz,exit);case 3->taint(sink,random,cx,cz,exit);default->throw new IllegalArgumentException("Original arena kind "+kind);}}
    public static void warden(ArenaSink sink, net.minecraft.util.RandomSource random,int cx,int cz,int exit){
        int a,b;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = x;
        int z2 = z;
        switch (exit) {
            case 2: {
                x2 += 8;
                z2 += 8;
                break;
            }
            case 3: {
                x2 -= 8;
                z2 += 8;
                break;
            }
            case 4: {
                x2 += 8;
                z2 -= 8;
                break;
            }
            case 5: {
                x2 -= 8;
                z2 -= 8;
            }
        }
        sink.obelisk( x2, y + 4, z);
        sink.obelisk( x, y + 4, z2);
        sink.raw(x2, y + 2, z, 1, 3, 3);
        sink.raw(x, y + 2, z2, 1, 3, 3);
        for (a = -1; a <= 1; ++a) {
            for (b = -1; b <= 1; ++b) {
                int md;
                float rr;
                if (a != 0 && b != 0 && random.nextFloat() < 0.9f) {
                    rr = random.nextFloat();
                    md = rr < 0.1f ? 2 : (rr < 0.3f ? 1 : 0);
                    sink.raw(x2 + a, y + 2, z + b, 6, md, 3);
                }
                if (a == 0 || b == 0 || !(random.nextFloat() < 0.9f)) continue;
                rr = random.nextFloat();
                md = rr < 0.1f ? 2 : (rr < 0.3f ? 1 : 0);
                sink.raw(x + a, y + 2, z2 + b, 6, md, 3);
            }
        }
        sink.raw(x - 2, y + 3, z - 2, 1, 10, 3);
        sink.raw(x - 2, y + 3, z + 2, 1, 10, 3);
        sink.raw(x + 2, y + 3, z + 2, 1, 10, 3);
        sink.raw(x + 2, y + 3, z - 2, 1, 10, 3);
        sink.raw(x - 2, y + 2, z - 2, 3, 15, 3);
        sink.raw(x - 2, y + 2, z + 2, 3, 15, 3);
        sink.raw(x + 2, y + 2, z + 2, 3, 15, 3);
        sink.raw(x + 2, y + 2, z - 2, 3, 15, 3);
        for (a = 0; a < 3; ++a) {
            for (b = 0; b < 3; ++b) {
                if (PED[a][b] < 0) {
                    sink.raw(x2 - 1 + b, y + 2, z2 - 1 + a, 1, 4, 3);
                    continue;
                }
                sink.raw(x2 - 1 + b, y + 2, z2 - 1 + a, 11, PED[a][b], 3);
            }
        }
        sink.boss(1, x2,y+3,z2);
    }
    public static void golem(ArenaSink sink, net.minecraft.util.RandomSource random,int cx,int cz,int exit){
        int a,b;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = 0;
        int z2 = 0;
        switch (exit) {
            case 2: {
                x2 = 8;
                z2 = 8;
                break;
            }
            case 3: {
                x2 = -8;
                z2 = 8;
                break;
            }
            case 4: {
                x2 = 8;
                z2 = -8;
                break;
            }
            case 5: {
                x2 = -8;
                z2 = -8;
            }
        }
        sink.obelisk( x + x2, y + 4, z + z2);
        sink.obelisk( x - x2, y + 4, z + z2);
        sink.obelisk( x + x2, y + 4, z - z2);
        sink.raw(x + x2, y + 2, z + z2, 1, 3, 3);
        sink.raw(x - x2, y + 2, z + z2, 1, 3, 3);
        sink.raw(x + x2, y + 2, z - z2, 1, 3, 3);
        for (a = 0; a < 3; ++a) {
            for (b = 0; b < 3; ++b) {
                if (PED[a][b] < 0) {
                    sink.raw(x - 1 + b, y + 2, z - 1 + a, 1, 4, 3);
                    continue;
                }
                sink.raw(x - 1 + b, y + 2, z - 1 + a, 11, PED[a][b], 3);
            }
        }
        for (a = -10; a <= 10; ++a) {
            for (b = -10; b <= 10; ++b) {
                if (!(a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2) && (a <= 2 || b >= -2) || !(random.nextFloat() < 0.15f) || !sink.isAir(x + a, y + 2, z + b)) continue;
                float rr = random.nextFloat();
                int md = rr < 0.05f ? 2 : (rr < 0.2f ? 1 : 0);
                sink.raw(x + a, y + 2, z + b, random.nextFloat() < 0.3f ? 7 : 6, md, 3);
            }
        }
        sink.boss(0, x,y+3,z);
    }
    public static void cultist(ArenaSink sink, net.minecraft.util.RandomSource random,int cx,int cz,int exit){
        int a,b;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        for (a = -4; a <= 4; ++a) {
            for (b = -4; b <= 4; ++b) {
                if ((Math.abs(a) == 2 || Math.abs(b) == 2) && random.nextBoolean() || (Math.abs(a) == 3 || Math.abs(b) == 3) && random.nextFloat() > 0.33f || (Math.abs(a) == 4 || Math.abs(b) == 4) && random.nextFloat() > 0.25f) continue;
                sink.raw(x + b, y + 1, z + a, 1, 7, 3);
            }
        }
        for (a = 0; a < 5; ++a) {
            for (b = 0; b < 5; ++b) {
                if (a != 0 && a != 4 && b != 0 && b != 4) continue;
                sink.raw(x - 8 + b * 4, y + 2, z - 8 + a * 4, 3, 11, 3);
                sink.raw(x - 8 + b * 4, y + 3, z - 8 + a * 4, 1, 5, 3);
                sink.raw(x - 8 + b * 4, y + 4, z - 8 + a * 4, 4, 1, 3);
                sink.raw(x - 8 + b * 4, y + 10, z - 8 + a * 4, 3, 11, 3);
                sink.raw(x - 8 + b * 4, y + 9, z - 8 + a * 4, 1, 5, 3);
                sink.raw(x - 8 + b * 4, y + 8, z - 8 + a * 4, 4, 9, 3);
            }
        }
        sink.boss(2, x,y+2,z);
    }
    public static void taint(ArenaSink sink, net.minecraft.util.RandomSource random,int cx,int cz,int exit){
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        for (int a = -12; a <= 12; ++a) {
            for (int b = -12; b <= 12; ++b) {
                sink.taintBiome(x + b,z + a);
                for (int c = 0; c < 9; ++c) {
                    if (!sink.isAir(x + b, y + 2 + c, z + a) || !sink.adjacentSolid( x + b, y + 2 + c, z + a) || random.nextInt(3) == 0) continue;
                    sink.raw(x + b, y + 2 + c, z + a, 8, random.nextInt(4) == 0 ? 1 : 0, 3);
                }
                if ((double)random.nextFloat() < 0.15) {
                    sink.raw(x + b, y + 2, z + a, 12, 0, 3);
                    if ((double)random.nextFloat() < 0.2) {
                        sink.raw(x + b, y + 3, z + a, 12, 0, 3);
                    }
                }
                if ((Math.abs(a) == 4 || Math.abs(b) == 4) && random.nextBoolean() || (Math.abs(a) >= 5 || Math.abs(b) >= 5) && random.nextFloat() > 0.33f || (Math.abs(a) >= 7 || Math.abs(b) >= 7) && random.nextFloat() > 0.25f) continue;
                sink.raw(x + b, y + 1, z + a, 12, 1, 3);
            }
        }
        sink.boss(3, x,y+3,z);
    }
    private ArenaRoomLayout(){}
}

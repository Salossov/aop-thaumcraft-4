package thaumcraft.world.dim.rooms;
import net.minecraft.core.Direction;
/** Placement boundary; original codes and raw block metadata keep layout independent of decorations. */
public interface RoomLayoutSink {
    void place(int x,int y,int z,int originalCode,Direction direction);
    void raw(int x,int y,int z,int originalBlock,int metadata,int flags);
    default boolean isAir(int x,int y,int z){throw new UnsupportedOperationException("Room needs world queries");}
    default boolean crustCandidate(int x,int y,int z){return isAir(x,y,z);}
    default boolean adjacentSolid(int x,int y,int z){throw new UnsupportedOperationException("Room needs adjacency queries");}
    default void crystal(int x,int y,int z,Direction facing){throw new UnsupportedOperationException("Room needs crystal placement");}
    default void lock(int x,int y,int z,Direction facing){throw new UnsupportedOperationException("Room needs lock orientation");}
    default void keyRoom(int x,int y,int z){throw new UnsupportedOperationException("Room needs original key and guardian population");}
    default void taintBiome(int x,int z){throw new UnsupportedOperationException("Room needs biome writes");}
    default void mindSpiderSpawner(int x,int y,int z){throw new UnsupportedOperationException("Room needs mind spider spawner");}
}

package thaumcraft.world.dim.rooms;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Server-side encounter placement, preserving the original lock's spawn order. */
public final class NativeArenaSink implements ArenaRoomLayout.ArenaSink {
    private final ServerLevel level;
    private final BlockPos lock,center;
    private final RoomWorldSink blocks;
    private final List<Mob> spawned=new ArrayList<>();
    private int tentacles;
    private boolean secondGiant,fourthGiant;
    public NativeArenaSink(ServerLevel level,BlockPos lock,int cx,int cz){this.level=level;this.lock=lock;center=new BlockPos(cx*16+16,52,cz*16+16);blocks=new RoomWorldSink(level,new thaumcraft.world.dim.MazeCell((short)1),new Random(0));}
    public List<Mob> spawned(){return List.copyOf(spawned);}
    public void finish(){blocks.finish();}
    @Override public void raw(int x,int y,int z,int block,int metadata,int flags){
        var pos=new BlockPos(x,y,z);
        if(block==11){level.setBlock(pos,R5Content.ELDRITCH_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,new Direction[]{Direction.EAST,Direction.WEST,Direction.SOUTH,Direction.NORTH}[metadata&3]).setValue(StairBlock.HALF,(metadata&4)==0?Half.BOTTOM:Half.TOP),flags);return;}
        if(block==12){level.setBlock(pos,(metadata==0?R2Content.FIBROUS_TAINT.get():R2Content.TAINTED_SOIL.get()).defaultBlockState(),flags);return;}
        blocks.raw(x,y,z,block,metadata,flags);
    }
    @Override public void obelisk(int x,int y,int z){raw(x,y,z,1,1,3);for(int n=1;n<=4;n++)raw(x,y+n,z,1,2,3);}
    @Override public boolean isAir(int x,int y,int z){var state=level.getBlockState(new BlockPos(x,y,z));return state.isAir()||state.is(R5Content.ELDRITCH_FIELD.get())||state.is(R5Content.VOID_DOOR.get())||state.is(R5Content.ELDRITCH_PORTAL.get());}
    @Override public boolean adjacentSolid(int x,int y,int z){var pos=new BlockPos(x,y,z);for(var d:Direction.values()){var p=pos.relative(d);if(level.getBlockState(p).isFaceSturdy(level,p,d.getOpposite()))return true;}return false;}
    @Override public void taintBiome(int x,int z){RoomBiomeWrites.put(level,x,z);}
    @Override public void boss(int kind,int x,int y,int z){
        if(kind==3&&tentacles==0){spawnTentacle(x,y,z);spawnTentacle(x+3,y,z+3);spawnTentacle(x-3,y,z+3);spawnTentacle(x+3,y,z-3);spawnTentacle(x-3,y,z-3);return;}
        Mob mob;
        if(kind==0)mob=R5Entities.ELDRITCH_GOLEM.get().create(level);
        else if(kind==1)mob=R5Entities.ELDRITCH_WARDEN.get().create(level);
        else if(kind==2)mob=R5Entities.CULTIST_PORTAL.get().create(level);
        else throw new IllegalArgumentException("Arena kind "+kind);
        if(mob==null)throw new IllegalStateException("Missing native arena entity "+kind);
        mob.moveTo(x+.5,y,z+.5,0,0);
        if(kind<2){double dx=lock.getX()-mob.getX(),dz=lock.getZ()-mob.getZ(),dy=lock.getY()+.5-mob.getY();mob.setYRot((float)(Math.atan2(dz,dx)*180/Math.PI)-90);mob.setXRot(-(float)(Math.atan2(dy,Math.sqrt(dx*dx+dz*dz))*180/Math.PI));mob.finalizeSpawn(level,level.getCurrentDifficultyAt(mob.blockPosition()),MobSpawnType.STRUCTURE,null);if(kind==1)mob.restrictTo(center,32);}
        level.addFreshEntity(mob);spawned.add(mob);
    }
    private void spawnTentacle(int x,int y,int z){boolean giant=switch(tentacles++){case 0->level.getDifficulty()==Difficulty.HARD;case 1->secondGiant=!level.random.nextBoolean();case 2->!secondGiant;case 3->fourthGiant=!level.random.nextBoolean();default->!fourthGiant;};Mob mob=giant?R5Entities.TAINTACLE_GIANT.get().create(level):R2Entities.TAINTACLE.get().create(level);if(mob==null)throw new IllegalStateException("Missing taint arena entity");mob.moveTo(x+.5,y,z+.5,0,0);ChampionSystem.makeChampion(mob,true);level.addFreshEntity(mob);spawned.add(mob);}
}

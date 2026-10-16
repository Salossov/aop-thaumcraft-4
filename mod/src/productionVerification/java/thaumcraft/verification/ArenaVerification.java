package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.blocks.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;
import thaumcraft.production.EldritchAltarBlockEntity;
import thaumcraft.world.dim.*;
import thaumcraft.world.dim.rooms.*;

/** Native keyed encounter, four real arena rosters, cultist altar and demolition. */
final class ArenaVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        for(int kind=0;kind<4;kind++){
            int cx=360+kind*8,cz=180;var center=new BlockPos(cx*16+16,52,cz*16+16);floor(level,center);
            var sink=new NativeArenaSink(level,center,cx,cz);ArenaRoomLayout.generate(sink,net.minecraft.util.RandomSource.create(481),cx,cz,2,kind);sink.finish();var roster=sink.spawned();
            try{
                check.accept(roster.size()==(kind==3?5:1),"source arena roster count for kind "+kind);
                for(var entity:roster)check.accept(entity.isAddedToLevel()&&entity.level()==level,"arena creature enters native server "+entity.getType());
                if(kind==0){check.accept(roster.getFirst() instanceof EldritchGolem&&roster.getFirst().getX()==center.getX()+.5&&roster.getFirst().getY()==53,"Golem enters central platform at source coordinates");check.accept(level.getBlockState(center).is(R5Content.CARVED_STONE.get()),"Golem central carved pedestal");}
                if(kind==1){var warden=roster.getFirst();check.accept(warden instanceof EldritchWarden&&warden.getX()==center.getX()+8.5&&warden.getZ()==center.getZ()+8.5,"Warden entrance-dependent offset");check.accept(warden.getRestrictCenter().equals(center)&&warden.getRestrictRadius()==32,"Warden source arena home radius32");check.accept(level.getBlockEntity(center.offset(8,2,0)) instanceof EldritchObeliskBlockEntity,"Warden obelisk retains native shield tile");}
                if(kind==2){var portal=(CultistPortal)roster.getFirst();check.accept(portal.getY()==52&&portal.getYRot()==0,"Crimson portal at source floor height without boss rotation");check.accept(level.getBlockState(center.offset(-8,0,-8)).is(R5Content.ELDRITCH_STONE.get())&&level.getBlockState(center.offset(-8,8,-8)).is(R5Content.ELDRITCH_STONE.get()),"Crimson lower and upper pillar rings");}
                if(kind==3){check.accept((roster.get(1) instanceof TaintacleGiant)!=(roster.get(2) instanceof TaintacleGiant)&&(roster.get(3) instanceof TaintacleGiant)!=(roster.get(4) instanceof TaintacleGiant),"both diagonal taint pairs contain exactly one giant");for(var mob:roster)check.accept(ChampionSystem.modifier(mob)>=0&&mob.isPersistenceRequired(),"all five arena tentacles forced persistent champions");check.accept(roster.get(4).getX()==center.getX()-2.5&&roster.get(4).getZ()==center.getZ()-2.5,"last tentacle source negative diagonal");}
            }finally{roster.forEach(Entity::discard);}
        }
        var pos=new BlockPos(6120,54,3020);level.getChunkAt(pos);var maze=LabyrinthData.get(level);int cx=pos.getX()>>4,cz=pos.getZ()>>4;var cell=new MazeCell((short)1);cell.feature=2;maze.put(cx-1,cz-1,cell.pack());
        level.setBlock(pos,R5Content.ELDRITCH_LOCK.get().defaultBlockState(),3);var lock=(EldritchLockBlockEntity)level.getBlockEntity(pos);check.accept(lock.count==-1,"unkeyed lock default inert");
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"ArenaQA"));var key=new net.minecraft.world.item.ItemStack(R5Content.RUNIC_KEY.get(),2);var hit=new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false);player.setItemInHand(InteractionHand.MAIN_HAND,key);
        var result=level.getBlockState(pos).useItemOn(key,level,player,InteractionHand.MAIN_HAND,hit);check.accept(result.consumesAction()&&key.getCount()==1&&lock.count==0,"actual runic key interaction consumes one and starts countdown");
        level.getBlockState(pos).useItemOn(key,level,player,InteractionHand.MAIN_HAND,hit);check.accept(key.getCount()==1&&lock.count==0,"activated lock rejects repeat key consumption");
        var saved=lock.saveWithoutMetadata(level.registryAccess());var restored=new EldritchLockBlockEntity(pos,lock.getBlockState());restored.loadWithComponents(saved,level.registryAccess());check.accept(restored.count==0,"activated lock countdown survives native NBT");
        var door=pos.east();level.setBlock(door,R5Content.VOID_DOOR.get().defaultBlockState(),3);int before=BossCycleData.get(level).count();
        for(int n=0;n<100;n++)EldritchLockBlockEntity.tick(level,pos,lock.getBlockState(),lock);check.accept(level.getBlockEntity(pos)==lock&&lock.count==100,"lock remains intact through tick100");
        EldritchLockBlockEntity.tick(level,pos,lock.getBlockState(),lock);check.accept(level.getBlockState(pos).isAir()&&level.getBlockState(door).isAir(),"tick101 removes lock and native airy door");check.accept(BossCycleData.get(level).count()>before,"lock activation advances persisted encounter cycle");
        for(var e:level.getEntitiesOfClass(Mob.class,new AABB(pos).inflate(40)))e.discard();
        var altarPos=new BlockPos(6336,80,3024);level.getEntitiesOfClass(Cultist.class,new AABB(altarPos).inflate(24,16,24)).forEach(Entity::discard);floorAt(level,altarPos);level.setBlock(altarPos,Blocks.AIR.defaultBlockState(),3);level.setBlock(altarPos,R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),3);var altar=(EldritchAltarBlockEntity)level.getBlockEntity(altarPos);altar.setSpawner(true);altar.setSpawnType((byte)0);altar.counter=119;EldritchAltarBlockEntity.tick(level,altarPos,altar.getBlockState(),altar);
        var clerics=level.getEntitiesOfClass(CultistCleric.class,new AABB(altarPos).inflate(4));check.accept(clerics.size()==4&&altar.spawnedClerics,"ritual altar creates four clerics and marks only successful wave: count="+clerics.size()+", flag="+altar.spawnedClerics+", counter="+altar.counter);for(var cleric:clerics)check.accept(cleric.isRitualist()&&cleric.getRestrictRadius()==8,"native cleric ritualist home radius8");
        var tag=altar.saveWithoutMetadata(level.registryAccess());var copy=new EldritchAltarBlockEntity(altarPos,altar.getBlockState());copy.loadWithComponents(tag,level.registryAccess());check.accept(copy.guardianSpawner&&copy.spawnedClerics&&copy.spawnType==0,"ritual spawn mode and successful clerics survive NBT");clerics.forEach(Entity::discard);altar.counter=159;EldritchAltarBlockEntity.tick(level,altarPos,altar.getBlockState(),altar);check.accept(!altar.guardianSpawner,"altar stops guards after all clerics disappear");
        altar.eyes=2;altar.installEye();check.accept(altar.guardianSpawner&&altar.spawnType==1&&altar.eyes==3,"third eye switches altar to guardian mode");
        level.setBlock(altarPos.above(2),R5Content.OBELISK.get().defaultBlockState(),3);level.setBlock(altarPos.above(3),R5Content.ELDRITCH_BODY.get().defaultBlockState(),3);level.setBlock(altarPos.east(2),R5Content.CARVED_STONE.get().defaultBlockState(),3);level.removeBlock(altarPos.above(2),false);check.accept(level.getBlockState(altarPos).isAir()&&level.getBlockState(altarPos.above(3)).isAir()&&level.getBlockState(altarPos.east(2)).is(R5Content.CARVED_STONE.get()),"source obelisk destruction cascades only metadata0..3");
    }
    private static void floor(ServerLevel level,BlockPos center){for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)level.setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);}
    private static void floorAt(ServerLevel level,BlockPos center){for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){level.setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);for(int y=0;y<=5;y++)level.setBlock(center.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}}
}

package thaumcraft.world.dim.rooms;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import thaumcraft.blocks.*;
import thaumcraft.registry.*;
import thaumcraft.entities.*;

/** GenCommon's conditional placement and ordered decoration queues on native blocks. */
public final class RoomWorldSink implements RoomLayoutSink {
    private static final net.minecraft.tags.TagKey<Block> ORIGINAL_ELDRITCH=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft","original_eldritch"));
    private static final Direction[] DIRECTIONS={Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
    private final WorldGenLevel world;
    private final thaumcraft.world.dim.MazeCell cell;
    private final Random random;
    private final RoomPlan plan;
    private final List<BlockPos> common=new ArrayList<>(),crabs=new ArrayList<>(),urns=new ArrayList<>();
    private final Set<BlockPos> stairs=new LinkedHashSet<>(),taint=new LinkedHashSet<>(),fibres=new LinkedHashSet<>();
    public RoomWorldSink(WorldGenLevel world,thaumcraft.world.dim.MazeCell cell,Random decorationRandom){this(world,cell,decorationRandom,null);}
    RoomWorldSink(WorldGenLevel world,thaumcraft.world.dim.MazeCell cell,Random decorationRandom,RoomPlan plan){this.world=world;this.cell=cell;random=decorationRandom;this.plan=plan;}
    private BlockState state(BlockPos pos){return plan==null?world.getBlockState(pos):plan.getBlockState(pos);}
    private net.minecraft.world.level.BlockGetter terrain(){return plan==null?world:plan;}
    private net.minecraft.world.level.LevelAccessor shapes(){return plan==null?world:plan.shapeAccess();}
    @Override public boolean isAir(int x,int y,int z){var s=state(new BlockPos(x,y,z));return s.isAir()||s.is(R5Content.VOID_DOOR.get())||s.is(R5Content.ELDRITCH_PORTAL.get());}
    private boolean isAir(BlockPos p){return isAir(p.getX(),p.getY(),p.getZ());}
    private void put(BlockPos pos,BlockState state,int flags){if(plan==null)world.setBlock(pos,state,flags);else plan.put(pos,state,flags);if(state.is(R5Content.ELDRITCH_STAIRS.get()))stairs.add(pos);if(state.getBlock() instanceof TaintFibresBlock)fibres.add(pos);}
    private void cancel(BlockPos pos){common.remove(pos);crabs.remove(pos);urns.remove(pos);}
    private void dirty(BlockPos pos,net.minecraft.world.level.block.entity.BlockEntity tile){if(world instanceof net.minecraft.server.level.ServerLevel)tile.setChanged();else world.getChunk(pos).setUnsaved(true);}
    private boolean isVoid(BlockPos p){return state(p).is(R5Content.ELDRITCH_VOID.get());}
    @Override public void place(int x,int y,int z,int code,Direction direction){
        var pos=new BlockPos(x,y,z);BlockState result=null;
        switch(code){
            case 0,12->{return;} // SLAB was defined but had no original switch branch.
            case 1->{if(isAir(pos))result=Blocks.BEDROCK.defaultBlockState();}
            case 99->result=Blocks.BEDROCK.defaultBlockState();
            case 8->result=R5Content.ELDRITCH_VOID.get().defaultBlockState();
            case 9->{cancel(pos);result=Blocks.AIR.defaultBlockState();}
            case 15->{cancel(pos);result=R5Content.ELDRITCH_DOOR_STONE.get().defaultBlockState();}
            case 16->{cancel(pos);result=R5Content.ELDRITCH_LOCK.get().defaultBlockState();}
            case 17->result=R5Content.VOID_DOOR.get().defaultBlockState();
            case 2->{
                boolean crust=cell.feature==7&&random.nextInt(3)==0;
                if(isVoid(pos))return;
                if(crust){result=crust(pos);}else{
                    if(random.nextInt(25)==0){boolean crab=cell.feature==7||random.nextInt(50)==0;if(crab&&(cell.feature==0||cell.feature==7))crabs.add(pos);else common.add(pos);}
                    result=R5Content.ELDRITCH_STONE.get().defaultBlockState();
                }
            }
            case 21->{if(!isVoid(pos))result=crust(pos);}
            case 18->{if(!isVoid(pos))result=R5Content.ELDRITCH_ROCK.get().defaultBlockState();}
            case 19->{if(!isVoid(pos))result=R5Content.ELDRITCH_STONE_NOSPAWN.get().defaultBlockState();}
            case 20->{if(!isVoid(pos))result=R5Content.ELDRITCH_TRAP.get().defaultBlockState();}
            case 7->result=R5Content.CARVED_STONE.get().defaultBlockState();
            case 3,4,5,6,10,11->{
                int meta=0,ordinal=direction==null?6:direction.ordinal();
                if(code==3||code==4){if((double)random.nextFloat()<.005)urns.add(pos);}
                if(code==10||code==11){meta=switch(ordinal){case 2->3;case 3->2;case 4->1;default->0;};if(ordinal>=2&&ordinal<=5&&code==11)meta+=4;}
                else if(ordinal==2||ordinal==3)meta=switch(code){case 3->1;case 4->0;case 5->5;default->4;};
                else if(ordinal==4||ordinal==5)meta=switch(code){case 3->3;case 4->2;case 5->7;default->6;};
                result=stair(meta);
            }
            default->throw new IllegalArgumentException("Unknown original room code "+code);
        }
        if(result!=null)put(pos,result,result.isAir()||result.is(Blocks.BEDROCK)||result.is(R5Content.ELDRITCH_VOID.get())?0:3);
    }
    private BlockState crust(BlockPos pos){
        if(random.nextInt(25)==0)return R5Content.CARVED_STONE.get().defaultBlockState();
        if(random.nextInt(25)==0){boolean crab=cell.feature==7||(cell.feature==12&&random.nextBoolean())||random.nextInt(25)==0;if(crab&&(cell.feature==0||cell.feature==7||cell.feature==12))crabs.add(pos);}
        return R5Content.ANCIENT_STONE.get().defaultBlockState();
    }
    private static BlockState stair(int metadata){return R5Content.ELDRITCH_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,new Direction[]{Direction.EAST,Direction.WEST,Direction.SOUTH,Direction.NORTH}[metadata&3]).setValue(StairBlock.HALF,(metadata&4)==0?Half.BOTTOM:Half.TOP);}
    @Override public void raw(int x,int y,int z,int block,int metadata,int flags){
        var pos=new BlockPos(x,y,z);BlockState result=switch(block){
            case 0->Blocks.AIR.defaultBlockState();
            case 1->(switch(metadata){case 0->R3Content.ELDRITCH_ALTAR.get();case 1->R5Content.OBELISK.get();case 2->R5Content.ELDRITCH_BODY.get();case 3->R5Content.ELDRITCH_CAP.get();case 4->R5Content.CARVED_STONE.get();case 5->R5Content.ANCIENT_RUNIC_STONE.get();case 6->R5Content.DECORATED_STONE.get();case 7->R5Content.ELDRITCH_DOOR_STONE.get();case 8->R5Content.ELDRITCH_LOCK.get();case 9->R5Content.CRAB_SPAWNER.get();case 10->R5Content.ELDRITCH_TRAP.get();default->throw new IllegalArgumentException("Eldritch metadata "+metadata);}).defaultBlockState();
            case 2->R5Content.ELDRITCH_PORTAL.get().defaultBlockState();
            case 3->(switch(metadata){case 11->R5Content.ELDRITCH_STONE.get();case 12->R5Content.ELDRITCH_ROCK.get();case 13->R5Content.ELDRITCH_STONE_NOSPAWN.get();case 14->R5Content.ANCIENT_STONE.get();case 15->R5Content.ELDRITCH_PILLAR.get();default->throw new IllegalArgumentException("Cosmetic metadata "+metadata);}).defaultBlockState();
            case 4->R5Content.ELDRITCH_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE,(metadata&8)==0?SlabType.BOTTOM:SlabType.TOP);
            case 5->R5Content.ANCIENT_CRYSTAL.get().defaultBlockState();
            case 6,7->(block==6?R5Content.LOOT_URN.get():R5Content.LOOT_CRATE.get()).defaultBlockState().setValue(AncientLootBlock.RARITY,metadata);
            case 8->(metadata==0?R2Content.TAINT_FIBRES.get():R2Content.TAINT_GROWTH.get()).defaultBlockState();
            case 9->Blocks.COBWEB.defaultBlockState();
            case 10->Blocks.SPAWNER.defaultBlockState();
            default->throw new IllegalArgumentException("Original raw room block "+block);
        };put(pos,result,flags);
    }
    @Override public boolean crustCandidate(int x,int y,int z){var p=new BlockPos(x,y,z);var s=state(p);return isAir(p)||s.is(R5Content.ELDRITCH_STONE.get())||s.is(R5Content.ELDRITCH_ROCK.get())||s.is(R5Content.ELDRITCH_STONE_NOSPAWN.get())||s.is(R5Content.ANCIENT_STONE.get())||s.is(R5Content.ELDRITCH_PILLAR.get())||s.is(R5Content.ELDRITCH_STAIRS.get());}
    @Override public boolean adjacentSolid(int x,int y,int z){var pos=new BlockPos(x,y,z);for(var d:DIRECTIONS){var p=pos.relative(d);var s=state(p);if(!s.is(ORIGINAL_ELDRITCH)&&s.isFaceSturdy(terrain(),p,d.getOpposite()))return true;}return false;}
    @Override public void crystal(int x,int y,int z,Direction facing){put(new BlockPos(x,y,z),R5Content.ANCIENT_CRYSTAL.get().defaultBlockState().setValue(AncientCrystalBlock.FACING,facing),3);}
    @Override public void lock(int x,int y,int z,Direction facing){var pos=new BlockPos(x,y,z);int raw=facing==null?6:facing.ordinal();if(plan!=null){if(state(pos).is(R5Content.ELDRITCH_LOCK.get())){put(pos,state(pos).setValue(EldritchLockBlock.FACING,raw),2);plan.lock(pos,raw);}return;}if(world.getBlockEntity(pos) instanceof EldritchLockBlockEntity tile){tile.facing=(byte)raw;put(pos,tile.getBlockState().setValue(EldritchLockBlock.FACING,raw),2);dirty(pos,tile);}}
    @Override public void keyRoom(int x,int y,int z){
        int count=2+(world.getDifficulty()==net.minecraft.world.Difficulty.HARD?2:world.getDifficulty()==net.minecraft.world.Difficulty.NORMAL?1:0);
        var guards=new ArrayList<BlockPos>();for(int i=0;i<count;i++)guards.add(new BlockPos(x+(1+random.nextInt(3))*(random.nextInt(3)-1),y,z+(1+random.nextInt(3))*(random.nextInt(3)-1)));
        var pos=new BlockPos(x,y,z);if(plan!=null)plan.key(pos,guards);else spawnKeyRoom(world,pos,guards);
    }
    static void spawnKeyRoom(WorldGenLevel world,BlockPos pos,List<BlockPos> guards){var level=world.getLevel();var item=R5Entities.PERMANENT_ITEM.get().create(level);item.setItem(new net.minecraft.world.item.ItemStack(R5Content.RUNIC_KEY.get()));item.setPos(pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5);item.setDeltaMovement(0,0,0);world.addFreshEntity(item);int guardIndex=0;for(var p:guards){var guardian=R2Entities.ELDRITCH_GUARDIAN.get().create(level);guardian.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);guardian.finalizeSpawn(world,world.getCurrentDifficultyAt(p),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);guardian.restrictTo(pos,16);if(guardIndex++==0&&world.getDifficulty()==net.minecraft.world.Difficulty.HARD)ChampionSystem.makeChampion(guardian,true);world.addFreshEntity(guardian);}}
    @Override public void taintBiome(int x,int z){taint.add(new BlockPos(x,0,z));}
    @Override public void mindSpiderSpawner(int x,int y,int z){var pos=new BlockPos(x,y,z);put(pos,Blocks.SPAWNER.defaultBlockState(),3);if(plan!=null){plan.spawner(pos);return;}if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity tile){
        // Spawner's update callback queries a FULL server chunk; during FEATURES that would wait on itself.
        tile.getSpawner().setEntityId(R3Entities.MIND_SPIDER.get(),world instanceof net.minecraft.server.level.ServerLevel?world.getLevel():null,world.getRandom(),pos);dirty(pos,tile);
    }}
    private boolean adjacentEldritch(BlockPos p){for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)if((x!=0||y!=0||z!=0)&&state(p.offset(x,y,z)).is(ORIGINAL_ELDRITCH))return true;return false;}
    private int exposed(BlockPos p){int count=0;for(var d:DIRECTIONS)if(isAir(p.relative(d)))count++;return count;}
    private boolean bedrockShowing(BlockPos p){for(var d:DIRECTIONS){var q=p.relative(d);if(state(q).isSolidRender(terrain(),q))continue;var behind=state(p.relative(d.getOpposite()));if(behind.is(Blocks.BEDROCK)||behind.is(R5Content.ELDRITCH_VOID.get()))return true;}return false;}
    public void finish(){
        for(var pos:urns)if(isAir(pos.above())){raw(pos.getX(),pos.getY(),pos.getZ(),3,15,3);float roll=random.nextFloat();raw(pos.getX(),pos.getY()+1,pos.getZ(),6,roll<.025f?2:roll<.1f?1:0,3);}
        for(var pos:common){int count=exposed(pos);if(count<=0||(count!=1&&bedrockShowing(pos))||adjacentEldritch(pos))continue;int metadata=random.nextInt(3)!=0?4:random.nextInt(8)!=0?5:10;raw(pos.getX(),pos.getY(),pos.getZ(),1,metadata,3);if(metadata==4&&random.nextInt(12)==0)for(var d:DIRECTIONS)if(isAir(pos.relative(d))){var p=pos.relative(d);crystal(p.getX(),p.getY(),p.getZ(),d);break;}}
        for(var pos:crabs){if(exposed(pos)!=1||adjacentEldritch(pos))continue;for(var d:DIRECTIONS)if(isAir(pos.relative(d))){put(pos,R5Content.CRAB_SPAWNER.get().defaultBlockState().setValue(EldritchCrabSpawnerBlock.FACING,d),3);break;}}
        for(var pos:stairs){var s=state(pos);if(s.is(R5Content.ELDRITCH_STAIRS.get())){var updated=s.updateShape(Direction.NORTH,state(pos.north()),shapes(),pos,pos.north());if(plan==null)world.setBlock(pos,updated,2);else plan.put(pos,updated,2);}}
        for(var pos:fibres){var s=state(pos);if(s.getBlock() instanceof TaintFibresBlock){for(var d:DIRECTIONS)s=s.updateShape(d,state(pos.relative(d)),shapes(),pos,pos.relative(d));if(plan==null)world.setBlock(pos,s,2);else plan.put(pos,s,2);}}
        for(var pos:taint)if(plan==null)RoomBiomeWrites.put(world,pos.getX(),pos.getZ());else plan.taint(pos);
        common.clear();crabs.clear();urns.clear();stairs.clear();taint.clear();fibres.clear();
    }
}

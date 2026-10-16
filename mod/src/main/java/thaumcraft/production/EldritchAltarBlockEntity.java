package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class EldritchAltarBlockEntity extends BlockEntity {
    public int eyes,counter;public boolean guardianSpawner,open,spawnedClerics;public byte spawnType;
    public EldritchAltarBlockEntity(BlockPos pos,BlockState state){super(thaumcraft.registry.R3Content.ELDRITCH_ALTAR_ENTITY.get(),pos,state);}
    public void setSpawner(boolean value){guardianSpawner=value;setChanged();}
    public void setSpawnType(byte value){spawnType=value;setChanged();}
    public void setEyes(byte value){eyes=value;setChanged();}
    public void installEye(){if(eyes>=4||level==null||level.isClientSide)return;if(eyes>=2){guardianSpawner=true;spawnType=1;}eyes++;checkForMaze();setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);level.playSound(null,worldPosition,thaumcraft.registry.R2Sounds.CRYSTAL.get(),net.minecraft.sounds.SoundSource.BLOCKS,.2f,1);}
    public static void tick(Level level,BlockPos pos,BlockState state,EldritchAltarBlockEntity altar){
        if(!(level instanceof ServerLevel server)||!altar.guardianSpawner||altar.counter++<80||altar.counter%40!=0)return;
        if(altar.spawnType==0){if(!altar.spawnedClerics)altar.spawnClerics(server);else altar.spawnGuards(server);}else if(altar.spawnType==1)altar.spawnGuardian(server);
    }
    private BlockPos randomSpawn(ServerLevel server){var r=server.random;return worldPosition.offset((4+r.nextInt(7))*(r.nextInt(3)-1),r.nextInt(4)*(r.nextInt(3)-1),(4+r.nextInt(7))*(r.nextInt(3)-1));}
    private boolean canSpawn(ServerLevel server,net.minecraft.world.entity.Mob mob,BlockPos pos){return server.hasChunkAt(pos)&&server.getBlockState(pos.below()).isFaceSturdy(server,pos.below(),Direction.UP)&&server.isUnobstructed(mob)&&server.noCollision(mob)&&!server.containsAnyLiquid(mob.getBoundingBox());}
    private void spawnGuardian(ServerLevel server){var pos=randomSpawn(server);var mob=thaumcraft.entities.R2Entities.ELDRITCH_GUARDIAN.get().create(server);if(mob==null)return;mob.moveTo(pos.getX(),pos.getY(),pos.getZ(),0,0);if(!canSpawn(server,mob,pos)||!mob.checkSpawnRules(server,net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED))return;finishSpawn(server,mob,pos,16);}
    private void spawnGuards(ServerLevel server){var area=new net.minecraft.world.phys.AABB(worldPosition).inflate(24,16,24);if(server.getEntitiesOfClass(thaumcraft.entities.CultistCleric.class,area).isEmpty()){setSpawner(false);return;}if(server.getEntitiesOfClass(thaumcraft.entities.Cultist.class,area).size()>=8)return;var pos=randomSpawn(server);var mob=thaumcraft.entities.R5Entities.CULTIST_KNIGHT.get().create(server);if(mob==null)return;mob.moveTo(pos.getX(),pos.getY(),pos.getZ(),0,0);if(canSpawn(server,mob,pos))finishSpawn(server,mob,pos,16);}
    private void spawnClerics(ServerLevel server){int success=0;for(int[] offset:new int[][]{{-2,-2},{-2,2},{2,-2},{2,2}}){var pos=worldPosition.offset(offset[0],0,offset[1]);var mob=thaumcraft.entities.R5Entities.CULTIST_CLERIC.get().create(server);if(mob==null)continue;mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);if(!canSpawn(server,mob,pos))continue;mob.restrictTo(worldPosition,8);mob.finalizeSpawn(server,server.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED,null);mob.spawnAnim();if(server.addFreshEntity(mob)){success++;mob.setRitualist(true);}}if(success>2){spawnedClerics=true;setChanged();}}
    private void finishSpawn(ServerLevel server,net.minecraft.world.entity.Mob mob,BlockPos pos,int radius){mob.finalizeSpawn(server,server.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED,null);mob.spawnAnim();mob.restrictTo(worldPosition,radius);server.addFreshEntity(mob);}
    /** Maze allocation precedes the vis check, as in WandManager.createOculus. */
    public boolean checkForMaze(){
        if(!(level instanceof ServerLevel server))return false;
        int width=15+server.random.nextInt(8)*2,height=15+server.random.nextInt(8)*2;
        int x=worldPosition.getX()>>4,z=worldPosition.getZ()>>4;
        var data=thaumcraft.world.dim.LabyrinthData.get(server);
        if(data.inRange(x,z,width,height))return true;
        data.generate(x,z,width,height,server.random.nextLong());return false;
    }
    public boolean activate(net.minecraft.server.level.ServerPlayer player,net.minecraft.world.item.ItemStack wand){
        if(!(level instanceof ServerLevel server)||eyes!=4||open||!server.mayInteract(player,worldPosition)
            ||!thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("OCULUS")
            ||!(server.getBlockEntity(worldPosition.above()) instanceof thaumcraft.blocks.AuraNodeBlockEntity node)
            ||node.state.type!=thaumcraft.api.nodes.NodeState.Type.DARK||!checkForMaze())return false;
        if(!thaumcraft.items.WandVis.consume(wand,java.util.Map.of("aer",100,"ignis",100,"terra",100,"aqua",100,"ordo",100,"perditio",100),player,true))return false;
        open=true;setChanged();server.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        server.playSound(null,worldPosition,thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        server.removeBlock(worldPosition.above(),false);
        server.setBlock(worldPosition.above(),thaumcraft.registry.R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),3);
        return true;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putByte("Eyes",(byte)eyes);tag.putBoolean("GuardianSpawner",guardianSpawner);tag.putBoolean("Open",open);tag.putBoolean("spawnedClerics",spawnedClerics);tag.putByte("spawnType",spawnType);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);eyes=Math.clamp(tag.getByte("Eyes"),0,4);guardianSpawner=tag.getBoolean("GuardianSpawner");counter=0;open=tag.getBoolean("Open");spawnedClerics=tag.getBoolean("spawnedClerics");spawnType=tag.contains("spawnType")?tag.getByte("spawnType"):(byte)(eyes>=3?1:0);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}

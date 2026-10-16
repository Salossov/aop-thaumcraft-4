package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;

public final class EtherealBloomBlockEntity extends BlockEntity {
    private int counter=-1;
    public int growth;
    public EtherealBloomBlockEntity(BlockPos pos,BlockState state){super(R4Content.BLOOM_ENTITY.get(),pos,state);}
    public void tick(){
        if(level.isClientSide){if(growth==0)level.playLocalSound(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,R2Sounds.ROOTS.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,.6f,false);growth=Math.min(100,growth+1);return;}
        if(counter<0)counter=level.random.nextInt(100);if(++counter%20!=0)return;
        int x=level.random.nextInt(8)-level.random.nextInt(8),z=level.random.nextInt(8)-level.random.nextInt(8);
        if(x*x+z*z>81)return;cleanColumn((ServerLevel)level,worldPosition.offset(x,0,z));
    }
    public static boolean cleanColumn(ServerLevel level,BlockPos pos){
        if(!level.hasChunkAt(pos))return false;var existing=WorldBiomes.effective(level,pos);
        if(!existing.is(WorldBiomes.TAINT)&&!existing.is(WorldBiomes.EERIE)&&!existing.is(WorldBiomes.MAGICAL))return false;
        var generated=level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(pos.getX()),QuartPos.fromBlock(pos.getY()),QuartPos.fromBlock(pos.getZ()),level.getChunkSource().randomState().sampler());
        var key=generated.unwrapKey().orElse(Biomes.PLAINS);if(key.equals(WorldBiomes.TAINT))key=Biomes.PLAINS;
        return WorldBiomes.change(level,pos.getX(),pos.getZ(),key);
    }
}

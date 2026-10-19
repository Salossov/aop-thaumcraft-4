package thaumcraft.world;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.api.aspects.*;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R2Content;

public final class NodeFactory {
    public static boolean create(net.minecraft.world.level.ServerLevelAccessor level,BlockPos pos,RandomSource random) {
        return create(level,pos,random,false,false,false);
    }
    public static boolean createSilverwood(net.minecraft.world.level.WorldGenLevel level,BlockPos pos,RandomSource random){return create(level,pos,random,true,false,true);}
    public static boolean createStructure(net.minecraft.world.level.WorldGenLevel level,BlockPos pos,RandomSource random){return create(level,pos,random,false,true,false);}
    public static boolean createTotem(net.minecraft.world.level.WorldGenLevel level,BlockPos pos,RandomSource random){return create(level,pos,random,false,true,true);}
    public static boolean createSmall(net.minecraft.world.level.ServerLevelAccessor level,BlockPos pos,RandomSource random){return create(level,pos,random,false,false,false,true);}
    private static boolean create(net.minecraft.world.level.ServerLevelAccessor level,BlockPos pos,RandomSource random,boolean silverwood,boolean eerie,boolean embedded) {
        return create(level,pos,random,silverwood,eerie,embedded,silverwood);
    }
    private static boolean create(net.minecraft.world.level.ServerLevelAccessor level,BlockPos pos,RandomSource random,boolean silverwood,boolean eerie,boolean embedded,boolean small) {
        if(!embedded&&!level.isEmptyBlock(pos)) return false;var type=silverwood?NodeState.Type.PURE:eerie?NodeState.Type.DARK:NodeState.Type.NORMAL;var modifier=NodeState.Modifier.NONE;
        int rarity=ThaumcraftConfig.SPECIAL_NODE_RARITY.get();
        if(!silverwood&&!eerie&&random.nextInt(rarity)==0) type=switch(random.nextInt(10)) { case 0,1,2->NodeState.Type.DARK;case 3,4,5->NodeState.Type.UNSTABLE;case 6,7,8->NodeState.Type.PURE;default->NodeState.Type.HUNGRY; };
        if(random.nextInt(rarity/2)==0) modifier=switch(random.nextInt(3)) { case 0->NodeState.Modifier.BRIGHT;case 1->NodeState.Modifier.PALE;default->NodeState.Modifier.FADING; };
        var biome=level.getBiome(pos);int aura=BiomeProfiles.aura(biome);
        if(type!=NodeState.Type.PURE&&biome.is(WorldBiomes.TAINT)) { aura=(int)(aura*1.5f);if(random.nextBoolean()) {type=NodeState.Type.TAINTED;aura=(int)(aura*1.5f);} }
        if(silverwood||small)aura=Math.max(4,aura/4);
        int strength=random.nextInt(aura/2)+aura/2;var weights=new AspectList();var selected=BiomeProfiles.randomAspect(biome,random);
        var compounds=Aspect.all().values().stream().filter(a->!a.isPrimal()).toList();
        if(selected!=null) weights.add(selected,2);else { weights.add(compounds.get(random.nextInt(compounds.size())),1);weights.add(Aspects.PRIMALS.get(random.nextInt(6)),1); }
        for(int i=0;i<3;i++) if(random.nextBoolean()) weights.merge(random.nextInt(rarity)==0?compounds.get(random.nextInt(compounds.size())):Aspects.PRIMALS.get(random.nextInt(6)),1);
        switch(type) {
            case HUNGRY->{weights.merge(Aspects.HUNGER,2);if(random.nextBoolean()) weights.merge(Aspects.GREED,1);}
            case PURE->weights.merge(random.nextBoolean()?Aspects.LIFE:Aspects.ORDER,2);
            case DARK->{for(var aspect:List.of(Aspects.DEATH,Aspects.UNDEAD,Aspects.ENTROPY,Aspects.DARKNESS)) if(random.nextBoolean()) weights.merge(aspect,1);}
            default->{}
        }
        int water=0,lava=0,stone=0,foliage=0;
        for(var sample:BlockPos.betweenClosed(pos.offset(-5,-5,-5),pos.offset(5,5,5))) {
            var state=level.getBlockState(sample);if(state.getFluidState().is(FluidTags.WATER)) water++;else if(state.getFluidState().is(FluidTags.LAVA)) lava++;else if(state.is(Blocks.STONE)) stone++;
            if(state.is(BlockTags.LEAVES)) foliage++;
        }
        if(water>100) weights.merge(Aspects.WATER,1);if(lava>100) {weights.merge(Aspects.FIRE,1);weights.merge(Aspects.EARTH,1);}if(stone>500) weights.merge(Aspects.EARTH,1);if(foliage>100) weights.merge(Aspects.PLANT,1);
        var aspects=java.util.Arrays.asList(weights.getAspectsSorted());var amounts=new LinkedHashMap<String,Integer>();var spread=new ArrayList<Integer>();float total=0;
        for(var aspect:aspects) { int value=weights.getAmount(aspect)==2?50+random.nextInt(25):25+random.nextInt(50);spread.add(value);total+=value; }
        for(int i=0;i<aspects.size();i++) amounts.put(aspects.get(i).getTag(),Math.max(weights.getAmount(aspects.get(i)),(int)(spread.get(i)/total*strength)));
        if(!embedded)level.setBlock(pos,R2Content.AURA_NODE.get().defaultBlockState(),2);
        if(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) { node.initialize(amounts,type,modifier);if(level instanceof net.minecraft.server.level.ServerLevel server&&embedded&&eerie&&node.getBlockState().is(thaumcraft.registry.R4Content.OBSIDIAN_TOTEM_NODE.get()))StructureIndexData.get(server).record(server,StructureIndexData.Kind.TOTEM,pos.getX(),pos.getY(),pos.getZ());return true; }return false;
    }
    private NodeFactory() {}
}

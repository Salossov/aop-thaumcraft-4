package thaumcraft.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import thaumcraft.api.aspects.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.*;
import thaumcraft.world.dim.LabyrinthData;

/** One original ordered population stream; the caller owns native chunk scheduling. */
public final class LegacyNaturalPopulation {
    private static final TagKey<Structure> SCATTERED=TagKey.create(Registries.STRUCTURE,ResourceLocation.parse("thaumcraft:scattered_temples"));
    private static final Map<net.minecraft.server.level.ServerLevel,Set<BlockPos>> STRUCTURE_NODES=Collections.synchronizedMap(new WeakHashMap<>());
    public record Result(boolean aura,PopulationPolicy.Structure selected,boolean structure){}
    public static Result populate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ,boolean newGen){
        return populate(level,random,chunkX,chunkZ,newGen,null,false);
    }
    public static boolean requiresStructureSearch(net.minecraft.server.level.ServerLevel level,int chunkX,int chunkZ,boolean fresh){
        if(level.dimension().equals(Level.NETHER)||level.dimension().equals(Level.END))return false;
        int blacklist=PopulationPolicy.blacklist(ThaumcraftConfig.DIMENSION_BLACKLIST.get(),level.dimension().location());
        return blacklist!=0&&blacklist!=2&&ThaumcraftConfig.GENERATE_AURA.get()&&(fresh||ThaumcraftConfig.REGENERATE_AURA.get());
    }
    public static Result populate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ,boolean newGen,BlockPos preparedStructure,boolean searchPrepared){
        var server=level.getLevel();var dimension=server.dimension();boolean nether=dimension.equals(Level.NETHER),end=dimension.equals(Level.END),overworld=dimension.equals(Level.OVERWORLD);
        boolean flat=server.getChunkSource().getGenerator() instanceof FlatLevelSource;
        var center=new BlockPos(chunkX*16+8,server.getSeaLevel(),chunkZ*16+8);
        var biome=level.getBiome(center).unwrapKey().map(ResourceKey::location).orElse(ResourceLocation.parse("minecraft:the_void"));
        var gates=PopulationPolicy.gates(PopulationPolicy.blacklist(ThaumcraftConfig.DIMENSION_BLACKLIST.get(),dimension.location()),PopulationPolicy.blacklist(ThaumcraftConfig.BIOME_BLACKLIST.get(),biome),overworld,nether,end,flat,newGen,new PopulationPolicy.Switches(ThaumcraftConfig.GENERATE_MAGIC_TREES.get(),ThaumcraftConfig.GENERATE_AURA.get(),ThaumcraftConfig.GENERATE_STRUCTURES.get(),ThaumcraftConfig.REGENERATE_TREES.get(),ThaumcraftConfig.REGENERATE_AURA.get(),ThaumcraftConfig.REGENERATE_STRUCTURES.get()));
        if(end)return new Result(false,PopulationPolicy.Structure.NONE,false);
        // New-chunk vegetation is already part of FEATURES. Only explicit
        // regeneration of an existing chunk belongs to the server queue.
        if(gates.trees()&&!newGen)vegetation(level,random,chunkX,chunkZ);
        if(gates.ores())ores(level,random,chunkX,chunkZ,newGen);
        boolean aura=false;
        if(gates.aura()){
            if(!nether){
                var nearest=searchPrepared?preparedStructure:server.findNearestMapStructure(SCATTERED,center,100,false);
                if(nearest!=null){var nodePos=nearest;var seen=STRUCTURE_NODES.computeIfAbsent(server,key->new HashSet<>());if(seen.add(nodePos)){aura=true;NodeFactory.create(level,new BlockPos(nodePos.getX(),surface(level,nodePos.getX(),nodePos.getZ())+3,nodePos.getZ()),random);}}
            }
            aura=wildNode(level,random,chunkX,chunkZ,aura);
        }
        PopulationPolicy.Structure selected=PopulationPolicy.Structure.NONE;boolean placed=false;
        if(gates.structures()){
            int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=surface(level,x,z)-9;
            if(y<level.getMaxBuildHeight()){
                selected=PopulationPolicy.structure(random);
                var index=thaumcraft.world.StructureIndexData.get(server);
                switch(selected){
                    case MOUND->{var pos=new BlockPos(x,y,z);placed=new LegacyMound().generate(level,random,pos);if(placed){aura=true;random.nextInt(200);NodeFactory.createStructure(level,pos.offset(9,8,9),random);index.record(server,thaumcraft.world.StructureIndexData.Kind.MOUND,pos.getX()+9,pos.getY(),pos.getZ()+9);}}
                    case RING->{var ring=new LegacyEldritchRing();ring.chunkX=chunkX;ring.chunkZ=chunkZ;ring.width=PopulationPolicy.mazeSize(random);ring.height=PopulationPolicy.mazeSize(random);var pos=new BlockPos(x,y+8,z);var data=LabyrinthData.get(server);synchronized(data){placed=ring.generate(level,random,pos);if(placed){aura=true;NodeFactory.createStructure(level,pos.above(2),random);data.generate(chunkX,chunkZ,ring.width,ring.height,random.nextLong());index.record(server,thaumcraft.world.StructureIndexData.Kind.RING_ALTAR,pos.getX(),pos.getY(),pos.getZ());}}}
                    case HILLTOP->{var pos=new BlockPos(x,y+9,z);placed=new LegacyHilltopStones().generate(level,random,pos);if(placed){aura=true;NodeFactory.createStructure(level,pos.above(5),random);index.record(server,thaumcraft.world.StructureIndexData.Kind.HILLTOP,pos.getX(),pos.getY(),pos.getZ());}}
                    default->{}
                }
            }
            LegacyTotem.generate(level,random,chunkX,chunkZ,aura,newGen);
        }
        return new Result(aura,selected,placed);
    }
    public static int surface(WorldGenLevel level,int x,int z){return level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z);}
    public static void decorate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ){
        var server=level.getLevel();var dimension=server.dimension();
        if(dimension.equals(Level.END)||dimension.equals(Level.NETHER)||dimension.location().equals(ResourceLocation.parse("thaumcraft:outer_lands")))return;
        var center=new BlockPos(chunkX*16+8,server.getSeaLevel(),chunkZ*16+8);
        var biome=level.getBiome(center);
        if(biome.is(WorldBiomes.MAGICAL))MagicalForestPopulation.populate(level,random,chunkX,chunkZ);
        var gates=PopulationPolicy.gates(PopulationPolicy.blacklist(ThaumcraftConfig.DIMENSION_BLACKLIST.get(),dimension.location()),PopulationPolicy.blacklist(ThaumcraftConfig.BIOME_BLACKLIST.get(),biome.unwrapKey().orElseThrow().location()),dimension.equals(Level.OVERWORLD),false,false,server.getChunkSource().getGenerator() instanceof FlatLevelSource,true,new PopulationPolicy.Switches(ThaumcraftConfig.GENERATE_MAGIC_TREES.get(),false,false,false,false,false));
        if(gates.trees())vegetation(level,random,chunkX,chunkZ);
    }
    private static void vegetation(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ){
        if(random.nextInt(60)==3){int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=surface(level,x,z);if(BiomeProfiles.supportsSilverwood(level.getBiome(new BlockPos(x,y,z))))new LegacySilverwoodTree(false,7,4).func_76484_a(level,random,x,y,z);}
        if(random.nextInt(25)==7){int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=surface(level,x,z);if(BiomeProfiles.greatwoodChance(level.getBiome(new BlockPos(x,y,z)))>random.nextFloat()){boolean spiders=random.nextInt(8)==0;if(new LegacyGreatwoodTree().generate(level,new RandomAdapter(random),x,y,z,spiders)&&spiders){var host=level.getLevel();host.getServer().execute(()->thaumcraft.world.StructureIndexData.get(host).record(host,thaumcraft.world.StructureIndexData.Kind.SPIDER_TREE,x,y,z));}}}
        LegacyCinderpearlFlowers.populate(level,random,new BlockPos(chunkX*16,0,chunkZ*16));
    }
    private static void ores(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ,boolean newGen){
        if(ThaumcraftConfig.GENERATE_CINNABAR.get()&&(newGen||ThaumcraftConfig.REGENERATE_CINNABAR.get()))for(int i=0;i<18;i++){int x=chunkX*16+random.nextInt(16),y=random.nextInt(51),z=chunkZ*16+random.nextInt(16);var pos=new BlockPos(x,y,z);if(level.getBlockState(pos).is(Blocks.STONE))level.setBlock(pos,R3Content.CINNABAR_ORE.get().defaultBlockState(),0);}
        if(ThaumcraftConfig.GENERATE_AMBER.get()&&(newGen||ThaumcraftConfig.REGENERATE_AMBER.get()))for(int i=0;i<20;i++){int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=surface(level,x,z)-random.nextInt(25);var pos=new BlockPos(x,y,z);if(level.getBlockState(pos).is(Blocks.STONE))level.setBlock(pos,R3Content.AMBER_ORE.get().defaultBlockState(),2);}
        if(ThaumcraftConfig.GENERATE_INFUSED_STONE.get()&&(newGen||ThaumcraftConfig.REGENERATE_INFUSED_STONE.get()))for(int i=0;i<8;i++){
            int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),floor=ThaumcraftConfig.DEEP_INFUSED_STONE.get()?level.getMinBuildHeight():0,y=floor+random.nextInt(Math.max(5,surface(level,x,z)-5-floor));
            int element=random.nextInt(6);
            if(random.nextInt(3)==0){var aspect=BiomeProfiles.randomAspect(level.getBiome(new BlockPos(x,y,z)),random);if(aspect==null)element=random.nextInt(6);else if(aspect==Aspects.AIR)element=0;else if(aspect==Aspects.FIRE)element=1;else if(aspect==Aspects.WATER)element=2;else if(aspect==Aspects.EARTH)element=3;else if(aspect==Aspects.ORDER)element=4;else if(aspect==Aspects.ENTROPY)element=5;}
            LegacyInfusedVein.place(level,random,new BlockPos(x,y,z),R2Content.INFUSED_STONE.get(element).get().defaultBlockState(),ThaumcraftConfig.DEEP_INFUSED_STONE.get());
        }
    }
    private static boolean wildNode(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ,boolean aura){
        if(random.nextInt(ThaumcraftConfig.NODE_RARITY.get())!=0||aura)return false;
        int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=5;
        while(y<level.getMaxBuildHeight()-1&&!level.isEmptyBlock(new BlockPos(x,y+1,z)))y++;
        if(level.isEmptyBlock(new BlockPos(x,y+1,z)))y++;
        int offset=random.nextInt(4);var candidate=new BlockPos(x,y+offset,z);if(level.isEmptyBlock(candidate)||level.getBlockState(candidate).canBeReplaced())y+=offset;
        if(y>level.getMaxBuildHeight())return false;
        NodeFactory.create(level,new BlockPos(x,y,z),random);return true;
    }
    /** Greatwood owns its own RNG seed but receives subsequent original caller draws. */
    private static final class RandomAdapter extends Random {
        private final RandomSource source;RandomAdapter(RandomSource source){this.source=source;}
        @Override public int nextInt(int bound){return source.nextInt(bound);}@Override public int nextInt(){return source.nextInt();}@Override public long nextLong(){return source.nextLong();}@Override public float nextFloat(){return source.nextFloat();}@Override public double nextDouble(){return source.nextDouble();}@Override public boolean nextBoolean(){return source.nextBoolean();}
    }
    private LegacyNaturalPopulation(){}
}

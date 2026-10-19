package thaumcraft.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.*;
import net.neoforged.neoforge.registries.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.*;

/** Native starts predict sites from the seed; the original layouts run in the FEATURES write region. */
public final class NativeStructures {
    public enum Kind { MOUND, HILLTOP, RING_ALTAR, CULTIST_RING, TOTEM, SPIDER_TREE }
    private static final DeferredRegister<StructureType<?>> TYPES=DeferredRegister.create(Registries.STRUCTURE_TYPE,"thaumcraft");
    private static final DeferredRegister<StructurePieceType> PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,"thaumcraft");
    private static final Map<Kind,DeferredHolder<StructureType<?>,StructureType<Site>>> REGISTERED=new EnumMap<>(Kind.class);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=PIECES.register("legacy_site",()->(StructurePieceType.ContextlessType)Piece::new);
    static{for(var kind:Kind.values())REGISTERED.put(kind,TYPES.register(kind.name().toLowerCase(Locale.ROOT),()->()->Structure.simpleCodec(settings->new Site(settings,kind))));}
    public static void register(net.neoforged.bus.api.IEventBus bus){TYPES.register(bus);PIECES.register(bus);}
    private static RandomSource random(long seed,ChunkPos chunk){return RandomSource.create(PopulationPolicy.populationSeed(seed,chunk.x,chunk.z)^0x5443345354525543L);}
    public static boolean candidate(Kind kind,long seed,ChunkPos chunk){
        if(PopulationPolicy.blacklist(ThaumcraftConfig.DIMENSION_BLACKLIST.get(),Level.OVERWORLD.location())!=-1)return false;
        var r=random(seed,chunk);var choice=PopulationPolicy.structure(r);int variation=r.nextInt(10);
        return switch(kind){
            case MOUND->ThaumcraftConfig.GENERATE_STRUCTURES.get()&&choice==PopulationPolicy.Structure.MOUND;
            case HILLTOP->ThaumcraftConfig.GENERATE_STRUCTURES.get()&&choice==PopulationPolicy.Structure.HILLTOP;
            case RING_ALTAR->ThaumcraftConfig.GENERATE_STRUCTURES.get()&&choice==PopulationPolicy.Structure.RING&&(variation<1||variation>4);
            case CULTIST_RING->ThaumcraftConfig.GENERATE_STRUCTURES.get()&&choice==PopulationPolicy.Structure.RING&&variation>=1&&variation<=4;
            case TOTEM->ThaumcraftConfig.GENERATE_STRUCTURES.get()&&choice==PopulationPolicy.Structure.NONE&&r.nextInt(ThaumcraftConfig.NODE_RARITY.get()*10)==0;
            case SPIDER_TREE->{var tree=RandomSource.create(PopulationPolicy.populationSeed(seed,chunk.x,chunk.z)^0x544334545245454cL);yield ThaumcraftConfig.GENERATE_MAGIC_TREES.get()&&tree.nextInt(25)==7&&tree.nextInt(8)==0;}
        };
    }
    public static final class Site extends Structure {
        public final Kind kind;
        Site(StructureSettings settings,Kind kind){super(settings);this.kind=kind;}
        @Override public StructureType<?> type(){return REGISTERED.get(kind).get();}
        @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext c){
            if(!candidate(kind,c.seed(),c.chunkPos())||c.chunkGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource)return Optional.empty();
            int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();int height=c.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState());
            var biome=c.biomeSource().getNoiseBiome(x>>2,height>>2,z>>2,c.randomState().sampler());
            if(PopulationPolicy.blacklist(ThaumcraftConfig.BIOME_BLACKLIST.get(),biome.unwrapKey().orElseThrow().location())>=0&&kind==Kind.SPIDER_TREE)return Optional.empty();
            if(height< c.heightAccessor().getMinBuildHeight()+12||height+(kind==Kind.SPIDER_TREE?64:17)>=c.heightAccessor().getMaxBuildHeight())return Optional.empty();
            int y=switch(kind){case MOUND->height-9;case HILLTOP,SPIDER_TREE->height;default->height-1;};
            var anchor=kind==Kind.MOUND?new BlockPos(x-9,y,z-9):new BlockPos(x,y,z);
            int[][] offsets=switch(kind){case MOUND->new int[][]{{9,9},{0,0},{18,0},{18,18},{0,18}};case HILLTOP->new int[][]{{-2,-2},{0,0},{2,0},{2,2},{0,2}};case RING_ALTAR,CULTIST_RING->new int[][]{{-3,-3},{0,0},{3,0},{3,3},{0,3}};case SPIDER_TREE->new int[][]{{-1,-1},{0,-1},{-1,0},{0,0}};default->new int[][]{{0,0}};};
            if(kind==Kind.HILLTOP&&y<85)return Optional.empty();
            for(var offset:offsets){int xx=anchor.getX()+offset[0],zz=anchor.getZ()+offset[1];int top=c.chunkGenerator().getBaseHeight(xx,zz,Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState())-1;
                int expected=kind==Kind.MOUND?y+8:kind==Kind.HILLTOP||kind==Kind.SPIDER_TREE?y-1:y;
                if(Math.abs(top-expected)>(kind==Kind.SPIDER_TREE?0:1))return Optional.empty();
                var state=c.chunkGenerator().getBaseColumn(xx,zz,c.heightAccessor(),c.randomState()).getBlock(top);
                if(!state.getFluidState().isEmpty()||!(state.is(Blocks.STONE)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.DIRT)||(kind==Kind.RING_ALTAR||kind==Kind.CULTIST_RING||kind==Kind.TOTEM)&&(state.is(Blocks.SAND)||state.is(Blocks.GRAVEL)||state.is(Blocks.PACKED_ICE))))return Optional.empty();
            }
            var r=random(c.seed(),c.chunkPos());PopulationPolicy.structure(r);int variant=r.nextInt(10);
            if(kind==Kind.SPIDER_TREE&&r.nextFloat()>=BiomeProfiles.greatwoodChance(biome))return Optional.empty();
            long seed=r.nextLong();return Optional.of(new GenerationStub(new BlockPos(x,y,z),builder->builder.addPiece(new Piece(kind,anchor,seed,variant))));
        }
    }
    public static final class Piece extends StructurePiece {
        public final Kind kind;public final BlockPos anchor;public final long seed;public final int variant;
        Piece(Kind kind,BlockPos anchor,long seed,int variant){super(PIECE.get(),0,bounds(kind,anchor));this.kind=kind;this.anchor=anchor;this.seed=seed;this.variant=variant;}
        Piece(CompoundTag tag){super(PIECE.get(),tag);kind=Kind.valueOf(tag.getString("Site"));anchor=BlockPos.of(tag.getLong("Anchor"));seed=tag.getLong("SiteSeed");variant=tag.getInt("Variant");}
        private static BoundingBox bounds(Kind kind,BlockPos p){return switch(kind){case MOUND->new BoundingBox(p.getX(),p.getY()-1,p.getZ(),p.getX()+18,p.getY()+16,p.getZ()+18);case SPIDER_TREE->new BoundingBox(p.getX()-12,p.getY()-2,p.getZ()-12,p.getX()+12,p.getY()+64,p.getZ()+12);default->new BoundingBox(p.getX()-3,p.getY()-4,p.getZ()-3,p.getX()+3,p.getY()+8,p.getZ()+3);};}
        public BlockPos center(){return kind==Kind.MOUND?anchor.offset(9,0,9):anchor;}
        public BlockPos locate(){return kind==Kind.MOUND?center().above(9):kind==Kind.HILLTOP||kind==Kind.SPIDER_TREE?center().above():center().above(2);}
        @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){tag.putString("Site",kind.name());tag.putLong("Anchor",anchor.asLong());tag.putLong("SiteSeed",seed);tag.putInt("Variant",variant);}
        @Override public void postProcess(WorldGenLevel level,StructureManager manager,ChunkGenerator generator,RandomSource ignored,BoundingBox box,ChunkPos chunk,BlockPos pivot){
            if(!box.isInside(center()))return;var r=RandomSource.create(seed);
            switch(kind){
                case MOUND->new LegacyMound().generateNative(level,r,anchor);
                case HILLTOP->new LegacyHilltopStones().generateNative(level,r,anchor);
                case RING_ALTAR,CULTIST_RING->new LegacyEldritchRing().generateNative(level,r,anchor,variant);
                case TOTEM->{level.setBlock(anchor,R4Content.OBSIDIAN_TILE.get().defaultBlockState(),3);int top=5;for(int n=1;n<5;n++){level.setBlock(anchor.above(n),R4Content.OBSIDIAN_TOTEM.get().at(level,anchor.above(n)),3);if(n>1&&r.nextInt(4)==0){top=n;break;}}level.setBlock(anchor.above(top),R4Content.OBSIDIAN_TOTEM_NODE.get().at(level,anchor.above(top)),3);}
                case SPIDER_TREE->new LegacyGreatwoodTree().generateNative(level,new Random(seed),anchor.getX(),anchor.getY(),anchor.getZ());
            }
        }
    }
    /** Called with the original 5x5 population area already FULL, on the server thread. */
    public static void finish(ServerLevel level,ChunkPos chunk){
        for(var start:level.getChunk(chunk.x,chunk.z).getAllStarts().values())if(start.isValid()&&start.getStructure() instanceof Site){for(var part:start.getPieces())if(part instanceof Piece piece){
            var at=piece.anchor;var r=RandomSource.create(piece.seed^0x4e4f4445L);var index=StructureIndexData.get(level);
            switch(piece.kind){
                case MOUND->{NodeFactory.createStructure(level,at.offset(9,8,9),r);index.record(level,StructureIndexData.Kind.MOUND,at.getX()+9,at.getY(),at.getZ()+9);}
                case HILLTOP->{NodeFactory.createStructure(level,at.above(5),r);index.record(level,StructureIndexData.Kind.HILLTOP,at.getX(),at.getY(),at.getZ());}
                case RING_ALTAR,CULTIST_RING->{if(level.getBlockEntity(at.above()) instanceof thaumcraft.production.EldritchAltarBlockEntity altar){altar.setSpawner(piece.variant>=1&&piece.variant<=4||piece.variant==6||piece.variant==7);altar.setSpawnType((byte)(piece.variant==6||piece.variant==7?1:0));NodeFactory.createStructure(level,at.above(2),r);var data=thaumcraft.world.dim.LabyrinthData.get(level);if(!data.inRange(chunk.x,chunk.z,11,11))data.generate(chunk.x,chunk.z,PopulationPolicy.mazeSize(r),PopulationPolicy.mazeSize(r),r.nextLong());index.record(level,StructureIndexData.Kind.RING_ALTAR,at.getX(),at.getY(),at.getZ());if(piece.kind==Kind.CULTIST_RING)index.record(level,StructureIndexData.Kind.CULTIST_RING,at.getX(),at.getY(),at.getZ());}}
                case TOTEM->{for(int n=2;n<=5;n++)if(level.getBlockState(at.above(n)).is(R4Content.OBSIDIAN_TOTEM_NODE.get()))NodeFactory.createTotem(level,at.above(n),r);}
                case SPIDER_TREE->index.record(level,StructureIndexData.Kind.SPIDER_TREE,at.getX(),at.getY(),at.getZ());
            }
        }}
    }
    private NativeStructures(){}
}

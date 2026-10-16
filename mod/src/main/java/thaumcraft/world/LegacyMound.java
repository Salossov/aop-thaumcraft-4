package thaumcraft.world;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import thaumcraft.blocks.AncientLootBlock;
import thaumcraft.registry.R5Content;

/** The full ordered original mound geometry, including its underground chambers. */
public final class LegacyMound {
    private record Placement(int x,int y,int z,BlockState state,int flags){}
    private static final List<Placement> LAYOUT=load();
    private static List<Placement> load(){
        try(var stream=LegacyMound.class.getResourceAsStream("/data/thaumcraft/structures/mound_layout.json")){
            if(stream==null)throw new IllegalStateException("Missing original mound layout");
            var data=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var palette=new ArrayList<BlockState>();
            for(var e:data.getAsJsonArray("palette")){var entry=e.getAsJsonObject();var id=ResourceLocation.parse(entry.get("Name").getAsString());if(!BuiltInRegistries.BLOCK.containsKey(id))throw new IllegalStateException("Missing mound block "+id);var state=BuiltInRegistries.BLOCK.get(id).defaultBlockState();if(entry.has("Properties"))for(var prop:entry.getAsJsonObject("Properties").entrySet())state=property(state,prop.getKey(),prop.getValue().getAsString());palette.add(state);}
            var rows=new ArrayList<Placement>();for(var e:data.getAsJsonArray("placements")){var a=e.getAsJsonArray();rows.add(new Placement(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt(),palette.get(a.get(3).getAsInt()),a.get(4).getAsInt()));}return List.copyOf(rows);
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static <T extends Comparable<T>> BlockState set(BlockState s,Property<T> p,String value){return s.setValue(p,p.getValue(value).orElseThrow());}
    private static BlockState property(BlockState s,String key,String value){var p=s.getBlock().getStateDefinition().getProperty(key);if(p==null)throw new IllegalArgumentException("Missing property "+key);return set(s,p,value);}
    public boolean generate(WorldGenLevel level,RandomSource random,BlockPos origin){
        if(level.isOutsideBuildHeight(origin.below())||level.isOutsideBuildHeight(origin.above(16)))return false;
        for(var offset:List.of(new BlockPos(9,9,9),new BlockPos(0,9,0),new BlockPos(18,9,0),new BlockPos(18,9,18),new BlockPos(0,9,18)))if(!AncientStructureSupport.validSurface(level,origin.offset(offset)))return false;
        for(var row:LAYOUT)level.setBlock(origin.offset(row.x,row.y,row.z),row.state,row.flags);
        // Vanilla connected states replace the old renderer's implicit neighbour connections.
        for(var row:LAYOUT)if(row.state.getBlock() instanceof StairBlock||row.state.is(Blocks.IRON_BARS)){var p=origin.offset(row.x,row.y,row.z);var current=level.getBlockState(p);level.setBlock(p,Block.updateFromNeighbourShapes(current,level,p),3);}
        for(int z:new int[]{7,11}){float r=random.nextFloat();int rarity=r<.1f?2:r<.33f?1:0;var block=level.getLevel().random.nextFloat()<.3f?R5Content.LOOT_CRATE.get():R5Content.LOOT_URN.get();level.setBlock(origin.offset(9,1,z),block.defaultBlockState().setValue(AncientLootBlock.RARITY,rarity),3);}
        boolean trapped=random.nextInt(3)==0;AncientStructureSupport.chest(level,origin.offset(10,1,9),trapped,random);if(trapped)level.setBlock(origin.offset(10,-1,9),Blocks.TNT.defaultBlockState(),3);
        AncientStructureSupport.spawner(level,origin.offset(4,5,4),EntityType.SKELETON,random);AncientStructureSupport.spawner(level,origin.offset(4,5,14),EntityType.ZOMBIE,random);
        return true;
    }
}

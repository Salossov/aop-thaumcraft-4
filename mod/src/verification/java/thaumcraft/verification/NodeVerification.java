package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.entities.AspectOrb;
import thaumcraft.registry.R2Content;
import thaumcraft.world.*;

/** Real level checks with reference coordinates produced by the original MC 1.7.10 bytecode. */
final class NodeVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check) {
        vein(level,check);
        var centre=new BlockPos(8,180,8);
        for(var p:BlockPos.betweenClosed(centre.offset(-5,-5,-5),centre.offset(5,5,5)))level.setBlock(p,Blocks.AIR.defaultBlockState(),2);
        int rarity=ThaumcraftConfig.SPECIAL_NODE_RARITY.get();
        try {
            ThaumcraftConfig.SPECIAL_NODE_RARITY.set(2);
            var types=EnumSet.noneOf(NodeState.Type.class);var modifiers=EnumSet.noneOf(NodeState.Modifier.class);
            for(int seed=0;seed<120;seed++) {
                level.setBlock(centre,Blocks.AIR.defaultBlockState(),2);
                if(!NodeFactory.create(level,centre,RandomSource.create(seed*0x9E3779B97F4A7C15L)))throw new AssertionError("node factory failed seed "+seed);
                var node=(AuraNodeBlockEntity)level.getBlockEntity(centre);
                types.add(node.state.type);modifiers.add(node.state.modifier);
                if(node.state.base.isEmpty()||node.state.base.values().stream().anyMatch(v->v<=0)||!node.state.base.equals(node.state.current))throw new AssertionError("invalid initial node vis seed "+seed);
                if(node.state.type==NodeState.Type.HUNGRY&&!node.state.base.containsKey("fames"))throw new AssertionError("hungry node lacks hunger");
            }
            check.accept(types.containsAll(EnumSet.of(NodeState.Type.NORMAL,NodeState.Type.DARK,NodeState.Type.UNSTABLE,NodeState.Type.PURE,NodeState.Type.HUNGRY)),"real node factory produces all five original natural types with valid full initial vis");
            check.accept(modifiers.containsAll(EnumSet.of(NodeState.Modifier.BRIGHT,NodeState.Modifier.PALE,NodeState.Modifier.FADING)),"real node factory produces every original brightness modifier");
            var before=level.getBlockState(centre);
            check.accept(!NodeFactory.create(level,centre,RandomSource.create(0))&&level.getBlockState(centre).equals(before),"node factory cannot replace an existing block");
        } finally {ThaumcraftConfig.SPECIAL_NODE_RARITY.set(rarity);}
        boolean hard=ThaumcraftConfig.HARD_MODE_NODES.get(),biomes=ThaumcraftConfig.NODE_BIOME_CHANGES.get();
        var mode=ThaumcraftConfig.BIOME_CHANGE_MODE.get();boolean global=ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.get();
        try {
            ThaumcraftConfig.HARD_MODE_NODES.set(false);ThaumcraftConfig.NODE_BIOME_CHANGES.set(true);
            ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.TC4_1X1);ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(false);
            var node=node(level,centre,NodeState.Type.DARK);node.state.ticks=50;NodeEffects.tick(node,level,new FixedRandom());
            check.accept(WorldBiomes.is(level,centre,WorldBiomes.EERIE),"dark node changes its real effective biome at the original 50-tick interval");
            var p=centre.east();node=node(level,p,NodeState.Type.TAINTED);node.state.ticks=50;NodeEffects.tick(node,level,new FixedRandom());
            check.accept(WorldBiomes.is(level,p,WorldBiomes.TAINT),"tainted node creates a Tainted Land column");
            node.state.type=NodeState.Type.PURE;NodeEffects.tick(node,level,new FixedRandom());
            check.accept(WorldBiomes.is(level,p,WorldBiomes.MAGICAL),"pure node heals a tainted column to Magical Forest");
            ThaumcraftConfig.NODE_BIOME_CHANGES.set(false);node.state.type=NodeState.Type.DARK;NodeEffects.tick(node,level,new FixedRandom());
            check.accept(WorldBiomes.is(level,p,WorldBiomes.MAGICAL),"node biome effects obey the master switch");
            node=node(level,centre,NodeState.Type.UNSTABLE);node.state.ticks=100;int vis=node.state.amount(thaumcraft.api.aspects.Aspects.AIR);
            NodeEffects.tick(node,level,new FixedRandom());
            check.accept(node.state.amount(thaumcraft.api.aspects.Aspects.AIR)==vis-1&&level.getEntitiesOfClass(AspectOrb.class,new AABB(centre).inflate(2)).size()==1,"unstable node spends one primal vis and creates a real aspect orb");
            node=node(level,centre,NodeState.Type.HUNGRY);node.state.ticks=50;var target=centre.east(2);level.setBlock(target,Blocks.STONE.defaultBlockState(),2);
            NodeEffects.tick(node,level,new FixedRandom(0,0,0,0,0,0,2,0,0,0,0,0));
            check.accept(level.getBlockState(target).isAir(),"hungry node destroys the ray-hit soft block even with hard-mode entity effects disabled, as in TC4");
            level.setBlock(target,Blocks.OBSIDIAN.defaultBlockState(),2);NodeEffects.tick(node,level,new FixedRandom(0,0,0,0,0,0,2,0,0,0,0,0));
            check.accept(level.getBlockState(target).is(Blocks.OBSIDIAN),"hungry node respects the original hardness limit");
            level.setBlock(target,Blocks.AIR.defaultBlockState(),2);node.state.ticks=1;
            var chicken=EntityType.CHICKEN.create(level);chicken.moveTo(centre.getX()+3,centre.getY()+0.5,centre.getZ()+0.5,0,0);level.addFreshEntity(chicken);
            NodeEffects.tick(node,level,new FixedRandom());check.accept(chicken.getDeltaMovement().lengthSqr()==0,"hard-mode switch disables hungry entity attraction");
            ThaumcraftConfig.HARD_MODE_NODES.set(true);NodeEffects.tick(node,level,new FixedRandom());
            check.accept(chicken.getDeltaMovement().x<0,"hungry node attracts living entities toward its centre");chicken.discard();
            int loadedX=6250,unloadedZ=6250;level.getChunk(loadedX,unloadedZ);
            while(level.hasChunk(loadedX+1,unloadedZ))loadedX++;
            var edge=new BlockPos(loadedX*16+14,180,unloadedZ*16+8);node=node(level,edge,NodeState.Type.HUNGRY);node.state.ticks=50;
            int unloadedX=loadedX+1;
            check.accept(!level.hasChunk(unloadedX,unloadedZ),"unloaded hungry-node target fixture is outside the loaded chunk");
            NodeEffects.tick(node,level,new FixedRandom(0,0,0,0,0,0,2,0,0,0));
            check.accept(!level.hasChunk(unloadedX,unloadedZ),"hungry-node height and ray queries cannot force-load an adjacent chunk");
        } finally {
            ThaumcraftConfig.HARD_MODE_NODES.set(hard);ThaumcraftConfig.NODE_BIOME_CHANGES.set(biomes);
            ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(global);
        }
    }
    private static void vein(ServerLevel level,BiConsumer<Boolean,String> check) {
        var ore=R2Content.INFUSED_STONE.get(0).get().defaultBlockState();
        var golden=Map.of(420235L,Set.of("7,78,8","7,79,7","8,78,8","8,79,7"),7L,Set.of("7,77,8","7,78,7","7,78,8","8,78,8","8,79,7","8,79,8"),99L,Set.of("7,77,8","7,78,7","7,78,8","8,79,7","8,80,7"));
        for(var entry:golden.entrySet()) {
            for(var p:BlockPos.betweenClosed(4,76,4,11,82,11))level.setBlock(p,Blocks.STONE.defaultBlockState(),2);
            LegacyInfusedVein.place(level,RandomSource.create(entry.getKey()),new BlockPos(0,80,0),ore,false);
            var actual=new HashSet<String>();for(var p:BlockPos.betweenClosed(4,76,4,11,82,11))if(level.getBlockState(p).equals(ore))actual.add(p.getX()+","+p.getY()+","+p.getZ());
            check.accept(actual.equals(entry.getValue()),"vein coordinates match original 1.7.10 bytecode, seed="+entry.getKey());
        }
        for(var p:BlockPos.betweenClosed(4,-24,4,11,2,11))level.setBlock(p,Blocks.DEEPSLATE.defaultBlockState(),2);
        check.accept(LegacyInfusedVein.place(level,RandomSource.create(7),new BlockPos(0,-20,0),ore,true),"enabled new depth generates the original vein geometry in deepslate below zero");
        for(var p:BlockPos.betweenClosed(4,-4,4,11,2,11))level.setBlock(p,Blocks.STONE.defaultBlockState(),2);
        LegacyInfusedVein.place(level,RandomSource.create(7),new BlockPos(0,0,0),ore,false);
        boolean below=false;for(var p:BlockPos.betweenClosed(4,-4,4,11,-1,11))below|=level.getBlockState(p).equals(ore);
        check.accept(!below,"disabled new depth clips ore veins at Y=0 like original world bounds");
        for(var p:BlockPos.betweenClosed(4,76,4,11,82,11))level.setBlock(p,Blocks.OAK_PLANKS.defaultBlockState(),2);
        check.accept(!LegacyInfusedVein.place(level,RandomSource.create(7),new BlockPos(0,80,0),ore,true),"ore cannot replace arbitrary blocks");
    }
    private static AuraNodeBlockEntity node(ServerLevel level,BlockPos pos,NodeState.Type type) {
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);level.setBlock(pos,R2Content.AURA_NODE.get().defaultBlockState(),2);
        var node=(AuraNodeBlockEntity)level.getBlockEntity(pos);node.initialize(Map.of("aer",20),type,NodeState.Modifier.NONE);return node;
    }
    private static final class FixedRandom extends Random {
        private final int[] values;private int index;
        FixedRandom(int...values){this.values=values;}
        @Override public int nextInt(int bound){int value=index<values.length?values[index++]:0;if(value>=bound)throw new AssertionError("scripted random out of range");return value;}
        @Override public boolean nextBoolean(){return true;}
    }
    private NodeVerification() {}
}

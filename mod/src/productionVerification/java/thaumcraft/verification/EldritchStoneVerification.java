package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.registry.R5Content;

final class EldritchStoneVerification {
    private static final BlockPos POS=new BlockPos(2944,140,64);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var root=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"));var controls=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(root.resolve("analysis/full_port/original-room-layouts.json"))).getAsJsonObject().getAsJsonArray("textures");
        check.accept(controls.size()==252,"252 original JAR texture controls include signed coordinates, int overflow and original stairs face+100");
        for(var value:controls){var row=value.getAsJsonArray();var pos=new BlockPos(row.get(0).getAsInt(),row.get(1).getAsInt(),row.get(2).getAsInt());int meta=row.get(3).getAsInt(),side=row.get(4).getAsInt();var face=Direction.from3DDataValue(side%100);int expected=row.get(5).getAsInt();int actual=side>=100?thaumcraft.blocks.EldritchStoneTextures.stairs(pos,face):meta==12?thaumcraft.blocks.EldritchStoneTextures.rock(pos,face):thaumcraft.blocks.EldritchStoneTextures.stone(pos,face);check.accept(actual==expected-(meta==12&&side<100?21:17),"native coordinate texture equals actual TC4 bytecode: "+pos+" metadata="+meta+" side="+side);}
        var blocks=List.of(R5Content.ELDRITCH_STONE.get(),R5Content.ELDRITCH_ROCK.get(),R5Content.ELDRITCH_STONE_NOSPAWN.get(),R5Content.ANCIENT_STONE.get(),R5Content.ELDRITCH_PILLAR.get(),R5Content.ELDRITCH_STAIRS.get());
        for(int i=0;i<blocks.size();i++){
            var block=blocks.get(i);var pos=POS.offset(i*2,0,0);level.getChunkAt(pos);level.setBlockAndUpdate(pos,block.defaultBlockState());var state=level.getBlockState(pos);
            check.accept(state.getDestroySpeed(level,pos)==2&&block.getExplosionResistance()==6,"original Outer Lands hardness/resistance in real world: "+block);
            check.accept(state.getLightEmission(level,pos)==(i==3?4:0),"original crust-only light emission in real world: "+block);
            check.accept(state.isValidSpawn(level,pos,EntityType.ZOMBIE)==(i!=2&&i!=5),"native zombie spawn suitability respects original no-spawn stone and stair geometry: "+block);
            var drops=Block.getDrops(state,level,pos,null);check.accept(drops.size()==1&&drops.getFirst().is(block.asItem())&&drops.getFirst().getCount()==1,"native loot manager preserves original one-block drop: "+block);
            var encoded=net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),state).getOrThrow();
            check.accept(net.minecraft.world.level.block.state.BlockState.CODEC.parse(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),encoded).getOrThrow()==state,"registry-backed native blockstate serialization: "+block);
            var stack=new ItemStack(block);var tag=stack.save(level.registryAccess());check.accept(ItemStack.parseOptional(level.registryAccess(),(net.minecraft.nbt.CompoundTag)tag).is(block.asItem()),"native item save preserves original stone identity: "+block);
        }
        var stairs=R5Content.ELDRITCH_STAIRS.get();
        for(var direction:Direction.Plane.HORIZONTAL)for(var half:Half.values()){
            var pos=POS.offset(0,0,4);var state=stairs.defaultBlockState().setValue(StairBlock.FACING,direction).setValue(StairBlock.HALF,half);level.setBlockAndUpdate(pos,state);
            var shape=state.getCollisionShape(level,pos,CollisionContext.empty());check.accept(!shape.isEmpty()&&shape.bounds().getYsize()==1,"both original stair halves retain full bounds and a native step for "+direction+" "+half);
        }
        var corner=POS.offset(4,0,4);level.setBlockAndUpdate(corner.east(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(corner,stairs.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));level.setBlockAndUpdate(corner.east(),stairs.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        check.accept(level.getBlockState(corner).getValue(StairBlock.SHAPE)==StairsShape.OUTER_LEFT,"eldritch room stairs retain native corner connections");
        var tag=net.minecraft.tags.BlockTags.create(ResourceLocation.parse("minecraft:beacon_base_blocks"));for(var block:blocks)if(block!=stairs)check.accept(block.defaultBlockState().is(tag),"original cosmetic-solid stone works in a native beacon base: "+block);
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){
        var blocks=List.of(R5Content.ELDRITCH_STONE.get(),R5Content.ELDRITCH_ROCK.get(),R5Content.ELDRITCH_STONE_NOSPAWN.get(),R5Content.ANCIENT_STONE.get(),R5Content.ELDRITCH_PILLAR.get(),R5Content.ELDRITCH_STAIRS.get());
        for(int i=0;i<blocks.size();i++){var p=POS.offset(i*2,0,0);level.getChunkAt(p);check.accept(level.getBlockState(p).is(blocks.get(i)),"Outer Lands stone identity survives actual dedicated-server restart: "+blocks.get(i));}
        check.accept(level.getBlockState(POS.offset(4,0,4)).getValue(StairBlock.SHAPE)==StairsShape.OUTER_LEFT,"eldritch stair corner survives actual dedicated-server restart");
    }
    private EldritchStoneVerification(){}
}

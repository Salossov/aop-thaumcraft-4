package thaumcraft.verification;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

final class RoundFourScene {
    static final String[] PIPES={"essentia_tube","essentia_valve","filtered_essentia_tube","restricted_essentia_tube","directional_essentia_tube","essentia_buffer"};
    static final String[] DEVICES={"vis_relay","vis_charge_relay","node_stabilizer","advanced_node_stabilizer","node_transducer","magic_mirror","essentia_mirror"};
    static BlockPos pipe(int index){return new BlockPos(600+index*3,110,610);}
    static BlockPos device(int index){return new BlockPos(600+index*3,110,615);}
    static void prepare(ServerLevel level){
        for(var pos:BlockPos.betweenClosed(598,109,608,621,109,624))level.setBlockAndUpdate(pos,Blocks.SMOOTH_STONE.defaultBlockState());
        for(int i=0;i<PIPES.length;i++){var pos=pipe(i);var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:"+PIPES[i]));level.setBlockAndUpdate(pos,block.defaultBlockState());level.setBlockAndUpdate(pos.west(),BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:essentia_tube")).defaultBlockState());level.setBlockAndUpdate(pos.east(),BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:essentia_tube")).defaultBlockState());}
        for(int i=0;i<DEVICES.length;i++){var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:"+DEVICES[i]));var state=block.defaultBlockState();if(state.hasProperty(thaumcraft.visnet.VisDeviceBlock.FACING))state=state.setValue(thaumcraft.visnet.VisDeviceBlock.FACING,Direction.NORTH);if(state.hasProperty(thaumcraft.production.MirrorBlock.FACING))state=state.setValue(thaumcraft.production.MirrorBlock.FACING,Direction.NORTH);level.setBlockAndUpdate(device(i),state);}
        int index=0;for(String id:PIPES){drop(level,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+id))),index++);}for(String id:DEVICES)drop(level,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+id))),index++);
        for(var item:java.util.List.of(thaumcraft.registry.R2Content.WAND.get(),thaumcraft.registry.R3Equipment.STAFF.get(),thaumcraft.registry.R3Equipment.SCEPTRE.get())){var stack=new ItemStack(item);if(item.kind()==thaumcraft.items.CastingWandItem.Kind.STAFF)stack.set(thaumcraft.registry.WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts("greatwood_staff","gold"));drop(level,stack,index++);}
    }
    private static void drop(ServerLevel level,ItemStack stack,int index){var entity=new ItemEntity(level,600+index%8*2.5,111,620+index/8*2,stack);entity.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);entity.setNoGravity(true);entity.setPickUpDelay(32767);level.addFreshEntity(entity);}
}

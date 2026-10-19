package thaumcraft.verification;

import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.blocks.*;
import thaumcraft.registry.R5Content;

final class EldritchVoidVerification {
    private static final BlockPos POS=new BlockPos(3008,140,64);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var original=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/original-void-tile-policy.json"))).getAsJsonObject();
        check.accept(!original.get("0").getAsBoolean()&&original.get("1").getAsBoolean(),"unchanged owned TC4 bytecode declares tiles only for exposed metadata1");
        check.accept(Blocks.CHEST.defaultBlockState().hasBlockEntity()&&!Blocks.STONE.defaultBlockState().hasBlockEntity(),"conditional void integration preserves ordinary native block tile flags");
        level.getChunkAt(POS);var block=R5Content.ELDRITCH_VOID.get();
        for(var face:Direction.values())level.setBlock(POS.relative(face),Blocks.STONE.defaultBlockState(),2);
        level.setBlock(POS,block.defaultBlockState(),2);EldritchVoidBlock.updateExposure(level,POS);var state=level.getBlockState(POS);
        check.accept(!state.getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(POS)==null,"fully enclosed void has no tile or ticker");
        check.accept(!state.hasBlockEntity(),"hidden void advertises no native tile, matching original metadata0");
        check.accept(state.hasBlockEntity()==original.get("0").getAsBoolean(),"hidden native tile policy matches executed original bytecode");
        check.accept(state.isSolidRender(level,POS)&&state.getRenderShape()==RenderShape.INVISIBLE,"original void occludes opaque faces while its block model is invisible");
        check.accept(state.getShape(level,POS).isEmpty(),"void cannot be selected");var bounds=state.getCollisionShape(level,POS,CollisionContext.empty()).bounds();
        check.accept(bounds.minX==.125&&bounds.minY==.125&&bounds.minZ==.125&&bounds.maxX==.875&&bounds.maxY==.875&&bounds.maxZ==.875,"original inset collision is retained in the native world");
        check.accept(state.getDestroySpeed(level,POS)==-1&&block.getExplosionResistance()==3600000,"void is unbreakable with effective original blast resistance");
        check.accept(state.getLightEmission(level,POS)==3&&state.getPistonPushReaction()==net.minecraft.world.level.material.PushReaction.BLOCK,"original void light and blocked piston behavior");
        check.accept(Block.getDrops(state,level,POS,null).isEmpty()&&block.asItem()==net.minecraft.world.item.Items.AIR,"void has no item or drops");
        level.setBlockAndUpdate(POS.above(),Blocks.AIR.defaultBlockState());
        check.accept(level.getBlockState(POS).getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(POS) instanceof EldritchVoidBlockEntity,"opening an opaque neighbor creates the original exposed tile");
        check.accept(level.getBlockState(POS).hasBlockEntity(),"exposed void advertises a native tile, matching original metadata1");
        check.accept(level.getBlockState(POS).hasBlockEntity()==original.get("1").getAsBoolean(),"exposed native tile policy matches executed original bytecode");
        level.setBlockAndUpdate(POS.above(),Blocks.STONE.defaultBlockState());
        check.accept(!level.getBlockState(POS).getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(POS)==null,"closing the neighbor removes the exposed tile");
        level.setBlockAndUpdate(POS.above(),Blocks.GLASS.defaultBlockState());
        check.accept(level.getBlockState(POS).getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(POS) instanceof EldritchVoidBlockEntity,"transparent non-air neighbor also exposes void");
        var cow=new net.minecraft.world.entity.animal.Cow(net.minecraft.world.entity.EntityType.COW,level);cow.tickCount=20;float health=cow.getHealth();state.entityInside(level,POS,cow);
        check.accept(cow.getHealth()==health,"first twenty ticks of entity life are exempt from void damage");cow.tickCount=21;state.entityInside(level,POS,cow);
        check.accept(cow.getHealth()==health-8,"mature entity takes eight original out-of-world damage");
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("01234567-1111-2222-3333-012345679501"),"VoidQA"));player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.tickCount=21;float hp=player.getHealth();state.entityInside(level,POS,player);check.accept(player.getHealth()==hp,"creative player is exempt");
        check.accept(level.damageSources().fellOutOfWorld().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR),"native void source bypasses armor");
        var slab=R5Content.ELDRITCH_SLAB.get();for(var type:SlabType.values()){
            var p=POS.offset(4+type.ordinal()*2,0,0);var s=slab.defaultBlockState().setValue(SlabBlock.TYPE,type);level.setBlockAndUpdate(p,s);var drops=Block.getDrops(s,level,p,null);
            check.accept(drops.size()==1&&drops.getFirst().is(slab.asItem())&&drops.getFirst().getCount()==(type==SlabType.DOUBLE?2:1),"original single/double slab native loot: "+type);
            var shape=s.getCollisionShape(level,p).bounds();check.accept(shape.minY==(type==SlabType.TOP?.5:0)&&shape.maxY==(type==SlabType.BOTTOM?.5:1),"original lower/upper/double slab collision: "+type);
        }
        var place=POS.offset(12,0,0);level.setBlockAndUpdate(place,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(place.below(),Blocks.STONE.defaultBlockState());player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var stack=new net.minecraft.world.item.ItemStack(slab,2);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
        var item=(net.minecraft.world.item.BlockItem)slab.asItem();var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atBottomCenterOf(place),Direction.UP,place.below(),false);
        check.accept(item.place(new net.minecraft.world.item.context.BlockPlaceContext(level,player,net.minecraft.world.InteractionHand.MAIN_HAND,stack,hit)).consumesAction()&&level.getBlockState(place).getValue(SlabBlock.TYPE)==SlabType.BOTTOM&&stack.getCount()==1,"actual slab item use places bottom and consumes one");
        hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(place),Direction.UP,place,false);
        check.accept(item.place(new net.minecraft.world.item.context.BlockPlaceContext(level,player,net.minecraft.world.InteractionHand.MAIN_HAND,stack,hit)).consumesAction()&&level.getBlockState(place).getValue(SlabBlock.TYPE)==SlabType.DOUBLE&&stack.isEmpty(),"second actual slab item use merges into double and consumes one");
        var enclosed=POS.offset(0,0,8);for(int x=-1;x<=2;x++)for(int y=-1;y<=2;y++)for(int z=-1;z<=2;z++)level.setBlock(enclosed.offset(x,y,z),Blocks.STONE.defaultBlockState(),2);level.setBlock(enclosed,block.defaultBlockState(),2);EldritchVoidBlock.updateExposure(level,enclosed);
        check.accept(level.getBlockEntity(enclosed)==null&&!level.getBlockState(enclosed).getValue(EldritchVoidBlock.EXPOSED),"hidden void fixture is saved without tile data");
        level.setBlock(enclosed,block.defaultBlockState().setValue(EldritchVoidBlock.EXPOSED,true),2);
        check.accept(level.getBlockEntity(enclosed) instanceof EldritchVoidBlockEntity,"direct exposed-state placement creates a real void tile");
        level.setBlock(enclosed,block.defaultBlockState(),2);
        check.accept(level.getBlockEntity(enclosed)==null&&level.getChunkAt(enclosed).getBlockEntityNbtForSaving(enclosed,level.registryAccess())==null,"direct same-block hiding removes the tile and all serialized tile data before save");
        check.accept(R5Content.VOID_ENTITY.get().create(enclosed,block.defaultBlockState())==null,"saved legacy hidden void tile is rejected before invalid-state construction");
        var root=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"));var controls=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(root.resolve("analysis/full_port/original-void-layers.json"))).getAsJsonArray();
        check.accept(controls.size()==12,"six near and six far planes captured from unchanged original renderer bytecode");
        for(var control:controls){var row=control.getAsJsonObject();var face=switch(row.get("plane").getAsString()){case "YPos"->Direction.DOWN;case "YNeg"->Direction.UP;case "ZPos"->Direction.NORTH;case "ZNeg"->Direction.SOUTH;case "XPos"->Direction.WEST;default->Direction.EAST;};boolean near=row.get("near").getAsBoolean();var layers=row.getAsJsonArray("layers");check.accept(layers.size()==(near?16:1),"original layer count: "+face+" near="+near);
            for(int j=0;j<layers.size();j++){var layer=layers.get(j).getAsJsonObject();if(near){var color=EldritchVoidGeometry.color(j);for(int channel=0;channel<3;channel++)check.accept(Math.abs(color[channel]-layer.getAsJsonArray("color").get(channel).getAsFloat())<.000001,"actual original layer color: "+face+" layer="+j+" channel="+channel);check.accept(EldritchVoidGeometry.scale(j)==layer.get("scale").getAsFloat()&&EldritchVoidGeometry.rotation(j)==layer.get("rotation").getAsFloat(),"original layer scale and rotation: "+face+" layer="+j);}
                for(int v=0;v<4;v++){var expected=layer.getAsJsonArray("vertices").get(v).getAsJsonArray();var actual=EldritchVoidGeometry.vertex(face,v);boolean same=true;for(int axis=0;axis<3;axis++)same&=actual[axis]==expected.get(axis).getAsFloat()-(axis+2);if(!near)for(int axis=3;axis<5;axis++)same&=actual[axis]==expected.get(axis).getAsFloat();check.accept(same,"original face winding and far UV: "+face+" layer="+j+" vertex="+v);}
            }
        }
        check.accept(EldritchVoidGeometry.near(511.999)&&!EldritchVoidGeometry.near(512),"original squared-distance near threshold 512");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){level.getChunkAt(POS);check.accept(level.getBlockState(POS).is(R5Content.ELDRITCH_VOID.get())&&level.getBlockState(POS).getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(POS) instanceof EldritchVoidBlockEntity,"exposed void and tile survive actual dedicated server restart");for(var type:SlabType.values()){var p=POS.offset(4+type.ordinal()*2,0,0);check.accept(level.getBlockState(p).is(R5Content.ELDRITCH_SLAB.get())&&level.getBlockState(p).getValue(SlabBlock.TYPE)==type,"native slab half survives actual restart: "+type);}var hidden=POS.offset(0,0,8);check.accept(level.getBlockState(hidden).is(R5Content.ELDRITCH_VOID.get())&&!level.getBlockState(hidden).getValue(EldritchVoidBlock.EXPOSED)&&level.getBlockEntity(hidden)==null,"hidden void survives restart without allocating a tile");check.accept(level.getBlockState(POS.offset(12,0,0)).getValue(SlabBlock.TYPE)==SlabType.DOUBLE,"item-merged slab survives actual restart");}
    private EldritchVoidVerification(){}
}

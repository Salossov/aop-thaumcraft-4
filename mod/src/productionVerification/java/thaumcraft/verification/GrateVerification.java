package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.production.ItemGrateBlock;
import thaumcraft.registry.R4Content;

final class GrateVerification {
    private static final BlockPos POS=new BlockPos(1280,110,0);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        for(int y=-2;y<=2;y++){level.getChunkAt(POS);level.setBlockAndUpdate(POS.offset(0,y,0),y==-2?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}
        level.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(2)).forEach(ItemEntity::discard);
        level.setBlockAndUpdate(POS,R4Content.ITEM_GRATE.get().defaultBlockState());var state=level.getBlockState(POS);
        var item=new ItemEntity(level,POS.getX()+.5,POS.getY()+1.2,.5,new ItemStack(Items.COAL));
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678903"),"GrateQA"));
        check.accept(state.getCollisionShape(level,POS,CollisionContext.of(item)).isEmpty()&&!state.getCollisionShape(level,POS,CollisionContext.of(player)).isEmpty(),"open item grate passes loose items but supports creatures");
        var shape=state.getShape(level,POS);check.accept(shape.min(Direction.Axis.Y)==.8125&&shape.max(Direction.Axis.Y)==1,"grate retains original 3/16 plate geometry");
        var top=level.getCapability(Capabilities.ItemHandler.BLOCK,POS,Direction.UP);check.accept(top!=null&&top.getSlots()==1&&level.getCapability(Capabilities.ItemHandler.BLOCK,POS,Direction.DOWN)==null,"open grate accepts automated insertion only from the top face");
        var incoming=new ItemStack(Items.COAL,80);var simulated=top.insertItem(0,incoming,true);check.accept(simulated.getCount()==16&&level.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(2)).isEmpty(),"simulated insertion respects stack limit without emitting any item entity");
        var remainder=top.insertItem(0,incoming,false);var emitted=level.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(2));check.accept(remainder.getCount()==16&&incoming.getCount()==80&&emitted.size()==1&&emitted.getFirst().getItem().getCount()==64,"executed insertion emits exactly one 64-item stack and retains the correct remainder");
        var drop=emitted.getFirst();check.accept(drop.getY()==POS.getY()+.6&&drop.getDeltaMovement().y==-.1&&drop.getDeltaMovement().x==0&&drop.getDeltaMovement().z==0,"grate emits straight downward below its collision plate");
        for(int i=0;i<8;i++)drop.tick();check.accept(drop.getY()<POS.getY()+.6,"native emitted item falls freely through the open grate");
        level.setBlockAndUpdate(POS,state.setValue(ItemGrateBlock.OPEN,false));level.invalidateCapabilities(POS);var closed=level.getBlockState(POS);
        check.accept(!closed.getCollisionShape(level,POS,CollisionContext.of(item)).isEmpty()&&top.getSlots()==0&&top.insertItem(0,new ItemStack(Items.COAL),false).is(Items.COAL),"closing the grate stops loose items and invalidates an already obtained insertion handler");
        check.accept(top.extractItem(0,64,false).isEmpty()&&top.getStackInSlot(0).isEmpty(),"grate cannot extract or retain already emitted items");
        var holder=level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("thaumcraft:grate")).orElseThrow();var recipe=(ShapedRecipe)holder.value();var stacks=recipe.getIngredients().stream().map(g->g.getItems()[0].copy()).toList();var grid=CraftingInput.of(recipe.getWidth(),recipe.getHeight(),stacks);
        check.accept(recipe.matches(grid,level)&&recipe.assemble(grid,level.registryAccess()).is(R4Content.GRATE_ITEM.get()),"original iron-bars and trapdoor pattern creates the functional native item grate");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){level.getChunkAt(POS);check.accept(level.getBlockState(POS).is(R4Content.ITEM_GRATE.get())&&!level.getBlockState(POS).getValue(ItemGrateBlock.OPEN),"closed grate state survives real dedicated-server restart");}
}

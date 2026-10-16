package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.blocks.*;
import thaumcraft.items.ThaumicBannerItem;
import thaumcraft.registry.*;

final class BannerVerification {
    private static final BlockPos POS=new BlockPos(2048,150,16);
    static void run(ServerLevel l,BiConsumer<Boolean,String> check){
        var player=new FakePlayer(l,new GameProfile(UUID.fromString("01822423-b0ba-482d-97b4-180be2199f5c"),"TC4_Banner_Test"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(POS.getCenter().add(0,1,-3));
        for(int i=0;i<16;i++){
            var pos=POS.offset(i*2,0,0);l.getChunkAt(pos);l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(pos.above(2),Blocks.AIR.defaultBlockState());player.setYRot(i*22.5f-180);var stack=ThaumicBannerItem.stack(i,Aspect.getAspect("aer"));stack.setCount(2);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var hit=new BlockHitResult(pos.getCenter().add(0,.5,0),Direction.UP,pos,false);var context=new BlockPlaceContext(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
            check.accept(stack.getItem().useOn(context).consumesAction()&&stack.getCount()==1,"native placement consumes one standing banner: "+i);var state=l.getBlockState(pos.above());var tile=(ThaumicBannerBlockEntity)l.getBlockEntity(pos.above());
            check.accept(state.is(R4Content.BANNER.get())&&!state.getValue(ThaumicBannerBlock.WALL)&&state.getValue(ThaumicBannerBlock.ROTATION)==i&&tile.color==i&&tile.aspect==Aspect.getAspect("aer"),"original sixteen-step rotation and saved colour/aspect are applied: "+i);
            check.accept(state.getCollisionShape(l,pos.above()).isEmpty()&&state.getShape(l,pos.above()).bounds().maxY==2,"banner keeps original two-block selection height and empty collision: "+i);
            var drops=Block.getDrops(state,l,pos.above(),tile);check.accept(drops.size()==1&&ThaumicBannerItem.color(drops.getFirst())==i&&ThaumicBannerItem.aspect(drops.getFirst())==tile.aspect,"actual banner drops preserve variant and decoration: "+i);
            var clone=state.getBlock().getCloneItemStack(state,hit,l,pos.above(),player);var loaded=ItemStack.parseOptional(l.registryAccess(),(CompoundTag)clone.save(l.registryAccess()));check.accept(ThaumicBannerItem.color(loaded)==i&&ThaumicBannerItem.aspect(loaded)==tile.aspect,"pick-block item and native item persistence retain colour/aspect: "+i);
            var recipe=(thaumcraft.crafting.ArcaneRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:banner_"+i)).orElseThrow().value();check.accept(recipe.vis().equals(Map.of("aqua",5,"terra",5))&&ThaumicBannerItem.color(recipe.getResultItem(l.registryAccess()))==i,"original coloured wool recipe returns correct component variant: "+i);
        }
        for(var side:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST)){
            var p=POS.offset(0,0,8);l.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(p.relative(side),Blocks.AIR.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,ThaumicBannerItem.stack(3,null));var hit=new BlockHitResult(p.getCenter().add(Vec3.atLowerCornerOf(side.getNormal()).scale(.5)),side,p,false);var context=new BlockPlaceContext(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));check.accept(player.getMainHandItem().getItem().useOn(context).consumesAction(),"original wall placement accepts "+side);var s=l.getBlockState(p.relative(side));int expected=switch(side){case NORTH->8;case WEST->4;case EAST->12;default->0;};check.accept(s.getValue(ThaumicBannerBlock.WALL)&&s.getValue(ThaumicBannerBlock.ROTATION)==expected,"wall orientation agrees with original placement and outline: "+side);
        }
        var p=POS.above();var state=l.getBlockState(p);var tile=(ThaumicBannerBlockEntity)l.getBlockEntity(p);var hit=new BlockHitResult(p.getCenter(),Direction.NORTH,p,false);var phial=new ItemStack(R3Content.ESSENTIA_PHIAL.get(),2);R3Content.setAspect(phial,Aspect.getAspect("ignis"));player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.setItemInHand(InteractionHand.MAIN_HAND,phial);state.useItemOn(phial,l,player,InteractionHand.MAIN_HAND,hit);check.accept(tile.aspect==Aspect.getAspect("ignis")&&phial.getCount()==1,"aspect decoration spends one phial even in original creative mode");player.setShiftKeyDown(true);state.useWithoutItem(l,player,hit);check.accept(tile.aspect==null&&phial.getCount()==1,"sneaking removes decoration without consuming or returning phial");player.setShiftKeyDown(false);
        var empty=new ItemStack(R3Content.EMPTY_PHIAL.get(),2);state.useItemOn(empty,l,player,InteractionHand.MAIN_HAND,hit);check.accept(tile.aspect==null&&empty.getCount()==2,"empty phial leaves banner decoration and stack unchanged");var crystal=new ItemStack(R3Content.ESSENTIA_CRYSTAL.get(),2);R3Content.setAspect(crystal,Aspect.getAspect("ignis"));state.useItemOn(crystal,l,player,InteractionHand.MAIN_HAND,hit);check.accept(tile.aspect==null&&crystal.getCount()==2,"crystals cannot substitute for original essence phials");
        tile.color=-1;state.useItemOn(phial,l,player,InteractionHand.MAIN_HAND,hit);check.accept(tile.aspect==null&&phial.getCount()==1,"uncoloured cult banner cannot receive a painted aspect");tile.color=0;tile.aspect=Aspect.getAspect("aer");tile.sync();
        var synced=new ThaumicBannerBlockEntity(p,state);synced.loadWithComponents(tile.getUpdateTag(l.registryAccess()),l.registryAccess());check.accept(synced.color==0&&synced.aspect==tile.aspect,"native block-entity synchronization retains cloth tint and aspect");
        var research=thaumcraft.data.ThaumcraftData.get(l.getServer()).research().get("BANNERS");check.accept(research!=null&&research.hidden()&&research.secondary()&&research.recipeVariants("Banners").size()==16&&research.pageOrder().equals(List.of("tc.research_page.BANNERS.1","Banners")),"original hidden secondary research contains one cycling sixteen-recipe page");
        var breakPos=POS.offset(0,1,14);l.getChunkAt(breakPos);l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(breakPos).inflate(3)).forEach(net.minecraft.world.entity.Entity::discard);
        for(var mode:List.of(net.minecraft.world.level.GameType.CREATIVE,net.minecraft.world.level.GameType.SURVIVAL)){
            l.setBlockAndUpdate(breakPos,R4Content.BANNER.get().defaultBlockState());var placed=(ThaumicBannerBlockEntity)l.getBlockEntity(breakPos);placed.color=3;placed.aspect=Aspect.getAspect("aqua");placed.sync();player.setGameMode(mode);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            check.accept(player.gameMode.destroyBlock(breakPos),"native server player destroys decorated banner in "+mode);var actual=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(breakPos).inflate(3));check.accept(actual.size()==1&&actual.getFirst().getItem().getCount()==1&&ThaumicBannerItem.color(actual.getFirst().getItem())==3&&ThaumicBannerItem.aspect(actual.getFirst().getItem())==Aspect.getAspect("aqua"),"original decorated banner drops exactly once in "+mode);actual.forEach(net.minecraft.world.entity.Entity::discard);
        }
    }
    static void reload(ServerLevel l,BiConsumer<Boolean,String> check){for(int i=0;i<16;i++){var p=POS.offset(i*2,1,0);l.getChunkAt(p);var state=l.getBlockState(p);var tile=(ThaumicBannerBlockEntity)l.getBlockEntity(p);check.accept(tile!=null&&tile.color==i&&tile.aspect==Aspect.getAspect("aer")&&state.getValue(ThaumicBannerBlock.ROTATION)==i,"actual server restart retains standing banner colour, aspect and rotation: "+i);}}
    private BannerVerification(){}
}

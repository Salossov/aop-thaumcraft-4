package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import thaumcraft.registry.R4Content;
import thaumcraft.fluids.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.effects.*;

final class CleansingVerification {
    static final BlockPos ORIGIN=new BlockPos(1100,110,0);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        for(int x=-2;x<40;x++)for(int z=-2;z<3;z++){var p=ORIGIN.offset(x,0,z);level.getChunkAt(p);for(int y=-1;y<3;y++)level.setBlockAndUpdate(p.offset(0,y,0),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}
        var pos=ORIGIN;level.setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());
        var salt=new ItemEntity(level,pos.getX()+.5,pos.getY()+.1,.5,new ItemStack(R4Content.BATH_SALTS.get()));
        check.accept(salt.lifespan==200,"bath salts use the original ten-second dropped-item lifespan");
        CleansingEvents.saltExpires(new net.neoforged.neoforge.event.entity.item.ItemExpireEvent(salt));
        check.accept(level.getBlockState(pos).is(R4Content.PURIFYING_FLUID.get())&&level.getFluidState(pos).isSource(),"expiring salt converts an actual source of water to purifying fluid");
        var flowing=pos.offset(3,0,0);level.setBlockAndUpdate(flowing,Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,3));salt.setPos(flowing.getX()+.5,flowing.getY()+.1,.5);CleansingEvents.saltExpires(new net.neoforged.neoforge.event.entity.item.ItemExpireEvent(salt));
        check.accept(level.getBlockState(flowing).is(Blocks.WATER),"salt cannot convert flowing water");
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678901"),"CleansingQA"));KnowledgeService.initialize(player);player.removeAllEffects();KnowledgeService.mutate(player,k->{k.setWarp(10000,5,8,10013);return true;});
        player.setPos(pos.getX()+.5,pos.getY()+.1,.5);level.getBlockState(pos).entityInside(level,pos,player);
        check.accept(player.hasEffect(WarpEffects.WARP_WARD)&&player.getEffect(WarpEffects.WARP_WARD).getDuration()==2000,"purifying source gives warp ward using original permanent-warp duration");
        check.accept(level.getBlockState(pos).isAir(),"purifying bath consumes its source on first unwarded player contact");
        level.setBlockAndUpdate(pos,R4Content.PURIFYING_FLUID.get().defaultBlockState());level.getBlockState(pos).entityInside(level,pos,player);
        check.accept(level.getBlockState(pos).is(R4Content.PURIFYING_FLUID.get()),"warded player does not consume another bath source");
        var soap=new ItemStack(R4Content.SANITY_SOAP.get(),2);R4Content.SANITY_SOAP.get().releaseUsing(soap,level,player,5);
        check.accept(soap.getCount()==2&&KnowledgeService.get(player).temporaryWarp()==8,"releasing soap before 196 ticks does not consume it or cleanse warp");
        R4Content.SANITY_SOAP.get().finishUsingItem(soap,level,player);var k=KnowledgeService.get(player);
        check.accept(soap.getCount()==1&&k.temporaryWarp()==0&&k.permanentWarp()==10000&&k.stickyWarp()>=4&&k.stickyWarp()<=5,"finished soap consumes one item, clears temporary warp and retains permanent warp");
        var death=ORIGIN.offset(10,0,0);level.setBlockAndUpdate(death,R4Content.LIQUID_DEATH.get().defaultBlockState());
        var handler=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,death,null);
        check.accept(handler!=null&&handler.getFluidInTank(0).getAmount()==1000,"four finite death-liquid quanta expose a native 1000 mB fluid capability");
        check.accept(handler.drain(249,FluidAction.EXECUTE).isEmpty(),"death-liquid transfer refuses fractions of its 250 mB quantum");
        check.accept(handler.drain(300,FluidAction.SIMULATE).getAmount()==250&&handler.getFluidInTank(0).getAmount()==1000,"simulated death-liquid drain retains the world volume");
        check.accept(handler.drain(250,FluidAction.EXECUTE).getAmount()==250&&handler.getFluidInTank(0).getAmount()==750,"real death-liquid drain removes exactly one finite quantum");
        check.accept(R4Content.LIQUID_DEATH.get().pickupBlock(player,level,death,level.getBlockState(death)).isEmpty(),"bucket cannot collect a partial death-liquid block");
        check.accept(handler.fill(new FluidStack(R4Fluids.DEATH.get(),250),FluidAction.EXECUTE)==250,"native death-liquid fill restores one finite quantum");
        var bucket=R4Content.LIQUID_DEATH.get().pickupBlock(player,level,death,level.getBlockState(death));check.accept(bucket.is(R4Content.DEATH_BUCKET.get())&&level.getBlockState(death).isAir(),"bucket pickup consumes exactly four finite quanta");
        check.accept(R4Content.DEATH_BUCKET.get().emptyContents(player,level,death,null)&&level.getFluidState(death).is(R4Fluids.DEATH.get()),"native death bucket places its real fluid block");
        R4Content.LIQUID_DEATH.get().flow(level,death,level.random);int units=0;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var s=level.getBlockState(death.offset(x,0,z));if(s.is(R4Content.LIQUID_DEATH.get()))units+=s.getValue(FiniteDeathFluid.QUANTA);}
        check.accept(units==4,"horizontal death-liquid spread conserves all four quanta");
        var dissolve=ORIGIN.offset(16,0,0);level.setBlockAndUpdate(dissolve,R4Content.LIQUID_DEATH.get().defaultBlockState());var zombie=EntityType.ZOMBIE.create(level);zombie.setPos(dissolve.getX()+.5,dissolve.getY(),.5);float health=zombie.getHealth();level.getBlockState(dissolve).entityInside(level,dissolve,zombie);
        check.accept(zombie.getHealth()==health-4&&zombie.getLastDamageSource().is(LiquidDeathBlock.DISSOLVE),"full death liquid inflicts original four-point armor-bypassing dissolve damage");
        var bloom=ORIGIN.offset(24,0,0);level.setBlockAndUpdate(bloom,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(bloom,R4Content.ETHEREAL_BLOOM.get().defaultBlockState());check.accept(level.getBlockEntity(bloom) instanceof thaumcraft.blocks.EtherealBloomBlockEntity,"ethereal bloom uses a real ticking block entity");
        var mode=thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.get();
        try{for(var value:thaumcraft.config.ThaumcraftConfig.BiomeChangeMode.values()){thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(value);var target=bloom.offset(4,0,0);thaumcraft.world.WorldBiomes.change(level,target.getX(),target.getZ(),thaumcraft.world.WorldBiomes.TAINT);check.accept(thaumcraft.blocks.EtherealBloomBlockEntity.cleanColumn(level,target)&&!thaumcraft.world.WorldBiomes.effective(level,target).is(thaumcraft.world.WorldBiomes.TAINT),"ethereal bloom restores generated biome in "+value);}}
        finally{thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);}
        var saved=ORIGIN.offset(32,0,0);level.setBlockAndUpdate(saved,R4Content.LIQUID_DEATH.get().defaultBlockState().setValue(FiniteDeathFluid.QUANTA,3));
        level.getDataStorage().save();level.getChunkAt(saved).setUnsaved(true);
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){var saved=ORIGIN.offset(32,0,0);level.getChunkAt(saved);check.accept(level.getBlockState(saved).is(thaumcraft.registry.R4Content.LIQUID_DEATH.get())&&level.getBlockState(saved).getValue(FiniteDeathFluid.QUANTA)==3,"partial death-liquid volume survives a real world restart");var bloom=ORIGIN.offset(24,0,0);level.getChunkAt(bloom);check.accept(level.getBlockEntity(bloom) instanceof thaumcraft.blocks.EtherealBloomBlockEntity,"ethereal bloom block entity survives a real world restart");}
}

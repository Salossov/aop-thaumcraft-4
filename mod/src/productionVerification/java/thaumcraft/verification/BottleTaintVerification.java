package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.effects.TaintEffects;
import thaumcraft.entities.*;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;

final class BottleTaintVerification {
    private static final BlockPos POS=new BlockPos(1944,150,24);
    static void run(ServerLevel l,BiConsumer<Boolean,String> check){
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){var p=POS.offset(x,0,z);l.getChunkAt(p);l.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());for(int y=1;y<=5;y++)l.setBlockAndUpdate(p.above(y),Blocks.AIR.defaultBlockState());}
        var region=new AABB(POS).inflate(30);l.getEntitiesOfClass(BottleTaintProjectile.class,region).forEach(Entity::discard);
        var player=new FakePlayer(l,new GameProfile(UUID.fromString("3c93884e-1b7f-43f6-8a61-782c38a8bc35"),"TC4_Taint_Test"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(POS.above(3).getCenter());player.setYRot(-90);player.setXRot(0);
        var stack=new ItemStack(R3Equipment.BOTTLE_TAINT.get(),8);player.setItemInHand(InteractionHand.MAIN_HAND,stack);check.accept(stack.getMaxStackSize()==8,"bottles preserve original stack limit of eight");
        player.setDeltaMovement(1,1,1);stack.getItem().use(l,player,InteractionHand.MAIN_HAND);var shot=l.getEntitiesOfClass(BottleTaintProjectile.class,region).getFirst();check.accept(stack.getCount()==7&&shot.getOwner()==player,"survival throws one real bottle entity and consumes one bottle");check.accept(shot.getDeltaMovement().length()<.55&&shot.getDeltaMovement().x>.4&&shot.getDeltaMovement().y>.1&&Math.abs(shot.getDeltaMovement().z)<.04,"original half-speed raised throw ignores moving shooter velocity");
        shot.discard();player.setDeltaMovement(Vec3.ZERO);player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);stack.getItem().use(l,player,InteractionHand.MAIN_HAND);check.accept(stack.getCount()==7,"creative throws without spending a bottle");shot=l.getEntitiesOfClass(BottleTaintProjectile.class,region).getFirst();var saved=shot.saveWithoutId(new CompoundTag());var copy=R3Entities.BOTTLE_TAINT.get().create(l);copy.load(saved);check.accept(copy.getItem().is(R3Equipment.BOTTLE_TAINT.get())&&copy.getDeltaMovement().equals(shot.getDeltaMovement()),"thrown bottle appearance and flight survive native NBT round trip");shot.discard();
        var pig=EntityType.PIG.create(l);pig.setPos(POS.getCenter().add(0,1,2));pig.setNoAi(true);l.addFreshEntity(pig);var zombie=EntityType.ZOMBIE.create(l);zombie.setPos(POS.getCenter().add(2,1,2));zombie.setNoAi(true);l.addFreshEntity(zombie);var tainted=R2Entities.TAINT_PIG.get().create(l);tainted.setPos(POS.getCenter().add(3,1,2));tainted.setNoAi(true);l.addFreshEntity(tainted);var far=EntityType.PIG.create(l);far.setPos(POS.getCenter().add(12,1,2));far.setNoAi(true);l.addFreshEntity(far);
        var biomeMode=ThaumcraftConfig.BIOME_CHANGE_MODE.get();boolean enabled=ThaumcraftConfig.NODE_BIOME_CHANGES.get();
        try{
            ThaumcraftConfig.NODE_BIOME_CHANGES.set(true);
            for(var mode:ThaumcraftConfig.BiomeChangeMode.values()){
                ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);
                for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)WorldBiomes.change(l,POS.getX()+x,POS.getZ()+z,WorldBiomes.MAGICAL);
                shot=R3Entities.BOTTLE_TAINT.get().create(l);shot.setPos(POS.above(1).getCenter());shot.setDeltaMovement(0,-1,0);shot.getRandom().setSeed(27);l.random.setSeed(31);l.addFreshEntity(shot);shot.tick();
                check.accept(shot.isRemoved(),"real block impact removes the bottle, biome mode "+mode);
                check.accept(pig.getEffect(TaintEffects.FLUX_TAINT)!=null&&pig.getEffect(TaintEffects.FLUX_TAINT).getDuration()==100&&pig.getEffect(TaintEffects.FLUX_TAINT).getAmplifier()==0,"impact grants original five-second flux taint, biome mode "+mode);
                check.accept(!zombie.hasEffect(TaintEffects.FLUX_TAINT)&&!tainted.hasEffect(TaintEffects.FLUX_TAINT)&&!far.hasEffect(TaintEffects.FLUX_TAINT),"undead, tainted creatures and entities outside five-block box remain immune");
                int changed=0,fibres=0;for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){var p=POS.offset(x,1,z);if(WorldBiomes.is(l,p,WorldBiomes.TAINT)){changed++;if(l.getBlockState(p).is(R2Content.TAINT_FIBRES.get()))fibres++;}}
                check.accept(changed>0&&changed<169,"ten original attempts edit a bounded selection of biome columns: "+mode);check.accept(fibres>0,"eligible fresh taint columns receive actual taint fibres: "+mode);
                var effect=pig.getEffect(TaintEffects.FLUX_TAINT);float health=pig.getHealth();pig.invulnerableTime=0;TaintEffects.FLUX_TAINT.get().applyEffectTick(pig,0);check.accept(pig.getHealth()<health,"impact effect causes actual taint damage");pig.removeEffect(TaintEffects.FLUX_TAINT);
            }
        }finally{ThaumcraftConfig.BIOME_CHANGE_MODE.set(biomeMode);ThaumcraftConfig.NODE_BIOME_CHANGES.set(enabled);pig.discard();zombie.discard();tainted.discard();far.discard();}
        var recipe=(CrucibleRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:bottle_taint")).orElseThrow().value();var phial=new ItemStack(R3Content.ESSENTIA_PHIAL.get());R3Content.setAspect(phial,Aspect.getAspect("vitium"));var wrong=phial.copy();R3Content.setAspect(wrong,Aspect.getAspect("ignis"));
        check.accept(recipe.catalyst().test(phial)&&!recipe.catalyst().test(wrong)&&!recipe.catalyst().test(new ItemStack(R3Content.ESSENTIA_PHIAL.get())),"crucible catalyst requires a taint phial and rejects other or unset aspects");check.accept(recipe.catalyst().getItems().length==1&&R3Content.aspect(recipe.catalyst().getItems()[0])==Aspect.getAspect("vitium"),"recipe book ingredient retains the actual taint phial component");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),l.registryAccess());try{var serializer=(thaumcraft.crafting.CrucibleRecipeData.Serializer)recipe.getSerializer();serializer.streamCodec().encode(buffer,(thaumcraft.crafting.CrucibleRecipeData)recipe);var network=serializer.streamCodec().decode(buffer);check.accept(network.catalyst().test(phial)&&!network.catalyst().test(wrong)&&!buffer.isReadable(),"native recipe synchronization preserves component-sensitive bottle catalyst");}finally{buffer.release();}
        var cruciblePos=POS.offset(20,1,0);l.getChunkAt(cruciblePos);l.setBlockAndUpdate(cruciblePos,R3Content.CRUCIBLE.get().defaultBlockState());var crucible=(thaumcraft.production.CrucibleBlockEntity)l.getBlockEntity(cruciblePos);crucible.heat=200;crucible.fillWater(1000);crucible.addToContainer(Aspect.getAspect("vitium"),8);crucible.addToContainer(Aspect.getAspect("praecantatio"),8);thaumcraft.knowledge.KnowledgeService.mutate(player,k->k.completeResearch("BOTTLETAINT"));
        var ingredient=new ItemEntity(l,cruciblePos.getX()+.5,cruciblePos.getY()+.8,cruciblePos.getZ()+.5,phial);ingredient.setThrower(player);l.addFreshEntity(ingredient);crucible.smelt(ingredient);
        check.accept(ingredient.isRemoved()&&crucible.water()==950&&crucible.getAspects().size()==0&&l.getEntitiesOfClass(ItemEntity.class,new AABB(cruciblePos).inflate(2)).stream().anyMatch(e->e.getItem().is(R3Equipment.BOTTLE_TAINT.get())),"actual cauldron crafts bottle, spends taint/magic and fifty water, and ejects product");
        check.accept(thaumcraft.knowledge.WarpService.gearWarp(player)==1,"held bottle contributes original one point of gear warp");var research=thaumcraft.data.ThaumcraftData.get(l.getServer()).research().get("BOTTLETAINT");check.accept(research!=null&&research.hidden()&&research.aspectTriggers().contains("vitium")&&research.recipePages().contains("BottleTaint"),"original hidden taint research, aspect trigger and recipe page are present");
        var payload=new thaumcraft.network.BottleTaintBreakPayload(new Vec3(-2.7,149.3,4.9),.25f);var packet=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),l.registryAccess());try{thaumcraft.network.BottleTaintBreakPayload.CODEC.encode(packet,payload);check.accept(payload.equals(thaumcraft.network.BottleTaintBreakPayload.CODEC.decode(packet))&&!packet.isReadable(),"impact packet retains exact position and original projectile height");}finally{packet.release();}
    }
    private BottleTaintVerification(){}
}

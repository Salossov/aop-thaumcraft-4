package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import thaumcraft.items.*;
import thaumcraft.registry.*;

final class ThirdRoundVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);player.getInventory().clearContent();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.tickCount=20;
        var source=player.createCommandSourceStack().withPermission(4);var dispatcher=level.getServer().getCommands().getDispatcher();
        for(var held:List.of(R2Content.WAND.get(),R3Equipment.STAFF.get(),R3Equipment.SCEPTRE.get())){
            var stack=new ItemStack(held);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            check.accept(dispatcher.execute("thaumcraft wand fill 12.5",source)==1&&thaumcraft.api.aspects.Aspects.PRIMALS.stream().allMatch(a->WandVis.get(stack,a)==1250),"wand numeric fill sets each primal to 12.5 vis: "+held);
            check.accept(dispatcher.execute("thaumcraft wand fill 32767",source)==1&&thaumcraft.api.aspects.Aspects.PRIMALS.stream().allMatch(a->WandVis.get(stack,a)==WandVis.capacity(stack)),"wand fill clamps to actual capacity: "+held);
            dispatcher.execute("thaumcraft wand empty",source);check.accept(thaumcraft.api.aspects.Aspects.PRIMALS.stream().allMatch(a->WandVis.get(stack,a)==0),"wand empty clears all six primals: "+held);
            dispatcher.execute("thaumcraft wand fill",source);check.accept(thaumcraft.api.aspects.Aspects.PRIMALS.stream().allMatch(a->WandVis.get(stack,a)==WandVis.capacity(stack)),"wand fill without amount fills to capacity: "+held);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));check.accept(dispatcher.execute("thaumcraft wand fill",source)==0&&player.getMainHandItem().getComponents().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA)==null,"wand command rejects ordinary held items");
        for(String command:List.of("thaumcraft research grant @s ASPECTS","thaumcraft research forget @s ASPECTS","thaumcraft knowledge aspect @s aer 1","thaumcraft world node normal"))check.accept(dispatcher.parse(command,source).getReader().getRemaining().isEmpty(),"organized command parses: "+command);
        int count=0;for(var item:BuiltInRegistries.ITEM){var id=BuiltInRegistries.ITEM.getKey(item);if(!id.getNamespace().equals("thaumcraft")||!(item instanceof TieredItem||item instanceof ArmorItem||item instanceof BowItem))continue;count++;var stack=new ItemStack(item);check.accept(stack.isDamageableItem()&&stack.getMaxDamage()>0,"registered equipment has native durability: "+id);
            if(item instanceof TieredItem tiered){check.accept(!tiered.getTier().getRepairIngredient().isEmpty(),"tool has a valid native repair ingredient: "+id);stack.setDamageValue(10);item.inventoryTick(stack,level,player,0,false);boolean repairs=id.getPath().startsWith("void_")||id.getPath().equals("primal_crusher")||item instanceof CrimsonSwordItem;check.accept(stack.getDamageValue()==(repairs?9:10),"tool repair cadence follows its material: "+id);}
            if(item instanceof ArmorItem armor){check.accept(armor.getDefense()>0,"armor has positive native protection: "+id);stack.setDamageValue(10);item.inventoryTick(stack,level,player,0,false);boolean repairs=item instanceof ThaumicArmorItem gear&&(gear.kind()==ThaumicArmorItem.Kind.VOID||gear.kind()==ThaumicArmorItem.Kind.VOID_ROBE);check.accept(stack.getDamageValue()==(repairs?9:10),"armor self-repair follows its material: "+id);
                if(!(item instanceof ThaumicArmorItem gear&&gear.kind()==ThaumicArmorItem.Kind.HARNESS)){var slot=armor.getEquipmentSlot();player.setItemSlot(slot,ItemStack.EMPTY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);item.use(level,player,InteractionHand.MAIN_HAND);check.accept(player.getItemBySlot(slot).is(item),"armor RMB equips the correct native slot: "+id);player.setItemSlot(slot,ItemStack.EMPTY);}
            }
        }check.accept(count>=40,"every registered tool, bow and armor was exercised; equipment count="+count);
        var sword=new ItemStack(R3Equipment.TOOLS.get("elemental_sword").get());player.setItemInHand(InteractionHand.MAIN_HAND,sword);player.setDeltaMovement(0,-.24,0);player.fallDistance=12;sword.getItem().use(level,player,InteractionHand.MAIN_HAND);sword.getItem().onUseTick(level,player,sword,72000);check.accept(player.isUsingItem()&&Math.abs(player.getDeltaMovement().y+.12)<.00001&&Math.abs(player.fallDistance-10)<.00001&&sword.getDamageValue()==1,"Zephyr RMB retains flight, reduced falling and original durability cadence");player.stopUsingItem();
        var boots=new ItemStack(R3Equipment.ARMOR.get("boots_of_the_traveller").get());player.setItemSlot(EquipmentSlot.FEET,boots);player.setDeltaMovement(0,0,0);thaumcraft.equipment.EquipmentEvents.jump(new net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent(player));check.accept(Math.abs(player.getDeltaMovement().y-.275)<.000001,"Traveller boots retain jump bonus");
        var victim=net.minecraft.world.entity.EntityType.ZOMBIE.create(level);var voidSword=new ItemStack(R3Equipment.TOOLS.get("void_sword").get());voidSword.getItem().hurtEnemy(voidSword,victim,player);check.accept(victim.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)&&victim.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getDuration()==60,"Void sword applies original lesser sapping for 60 ticks");
        var shape=R3Content.BRAINBOX.get().defaultBlockState().getShape(level,net.minecraft.core.BlockPos.ZERO).bounds();check.accept(shape.minX==3/16d&&shape.minY==3/16d&&shape.maxX==13/16d&&shape.maxY==13/16d,"Mnemonic Matrix shape matches the original 3..13 cube");
        var hoe=new ItemStack(R3Equipment.TOOLS.get("elemental_hoe").get());player.setItemInHand(InteractionHand.MAIN_HAND,hoe);var farm=new net.minecraft.core.BlockPos(448,190,400);for(var pos:net.minecraft.core.BlockPos.betweenClosed(farm.offset(-16,0,-16),farm.offset(40,0,16)))level.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        var farmContext=new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(farm.getCenter(),net.minecraft.core.Direction.UP,farm,false));hoe.getItem().useOn(farmContext);int tilled=0;for(var pos:net.minecraft.core.BlockPos.betweenClosed(farm.offset(-1,0,-1),farm.offset(1,0,1)))if(level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.FARMLAND))tilled++;check.accept(tilled==9&&hoe.getDamageValue()==9,"Growth hoe tills its native 3x3 plane with per-block durability");
        for(boolean silver:List.of(false,true)){var saplingPos=farm.offset(silver?24:0,1,0);var block=silver?R3Content.SILVERWOOD_SAPLING.get():R3Content.GREATWOOD_SAPLING.get();level.setBlockAndUpdate(saplingPos.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());level.setBlockAndUpdate(saplingPos,block.defaultBlockState());hoe.setDamageValue(0);level.random.setSeed(silver?71001:71000);var context=new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(saplingPos.getCenter(),net.minecraft.core.Direction.UP,saplingPos,false));hoe.getItem().useOn(context);check.accept(level.getBlockState(saplingPos).is(net.minecraft.tags.BlockTags.LOGS)&&hoe.getDamageValue()==(silver?25:5),"Growth hoe grows magical sapling with original durability cost: silver="+silver);}
    }
}

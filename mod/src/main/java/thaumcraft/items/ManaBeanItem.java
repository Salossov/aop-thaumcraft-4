package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.*;
import thaumcraft.knowledge.KnowledgeService;

/** Aspect identity is part of the stack; bare beans acquire an original random aspect. */
public final class ManaBeanItem extends Item {
    private final java.util.Random aspectRandom=new java.util.Random();
    /** Vanilla array32 extended by nine slots; TC4 fills 32..40, preserving empty slots. */
    public static final int ORIGINAL_EFFECT_SLOTS=41;
    public ManaBeanItem(){super(new Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(.5f).alwaysEdible().build()));}
    public static Aspect aspect(ItemStack stack){return Aspect.getAspect(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("ManaBeanAspect"));}
    public static void aspect(ItemStack stack,Aspect aspect){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.putString("ManaBeanAspect",aspect.getTag()));}
    public static ItemStack of(Aspect aspect){var stack=new ItemStack(thaumcraft.registry.R5Content.MANA_BEAN.get());aspect(stack,aspect);return stack;}
    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context){
        var level=context.getLevel();var log=context.getClickedPos();var player=context.getPlayer();var stack=context.getItemInHand();
        if(player==null||context.getClickedFace()!=net.minecraft.core.Direction.DOWN||!player.mayUseItemAt(log,context.getClickedFace(),stack)||!thaumcraft.blocks.ManaPodBlock.magic(level,log)||!thaumcraft.blocks.ManaPodBlock.supportState(level.getBlockState(log)))return net.minecraft.world.InteractionResult.PASS;
        var pos=log.below();if(level.isEmptyBlock(pos)&&!level.isClientSide){level.setBlock(pos,thaumcraft.registry.R5Content.MANA_POD.get().defaultBlockState(),2);if(level.getBlockEntity(pos) instanceof thaumcraft.blocks.ManaPodBlockEntity tile)tile.setAspect(aspect(stack));if(!player.getAbilities().instabuild)stack.shrink(1);}
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }
    public static int color(ItemStack stack,long timeMillis){var value=aspect(stack);var all=Aspect.all().values().toArray(Aspect[]::new);return value!=null?value.getColor():all[Math.floorMod(timeMillis/500,all.length)].getColor();}
    private void initialize(ItemStack stack){if(!stack.has(DataComponents.CUSTOM_DATA)){var all=Aspect.all().values().toArray(Aspect[]::new);aspect(stack,all[aspectRandom.nextInt(all.length)]);}}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 10;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){if(!level.isClientSide)initialize(stack);super.inventoryTick(stack,level,entity,slot,selected);}
    @Override public void onCraftedBy(ItemStack stack,Level level,net.minecraft.world.entity.player.Player player){initialize(stack);super.onCraftedBy(stack,level,player);}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){var bean=aspect(stack);var result=super.finishUsingItem(stack,level,user);if(!level.isClientSide&&user instanceof ServerPlayer player){var effect=originalEffect(level.random.nextInt(ORIGINAL_EFFECT_SLOTS));if(effect!=null){if(effect.value().isInstantenous())effect.value().applyInstantenousEffect(player,player,player,2,3);else {var instance=new MobEffectInstance(effect,160+level.random.nextInt(80),0);instance.getCures().add(net.neoforged.neoforge.common.EffectCures.MILK);player.addEffect(instance);}}if(level.random.nextFloat()<.25f&&bean!=null)KnowledgeService.mutate(player,k->{k.addPool(bean,1);return true;});}return result;}
    public static Holder<MobEffect> originalEffect(int id){return switch(id){case 1->MobEffects.MOVEMENT_SPEED;case 2->MobEffects.MOVEMENT_SLOWDOWN;case 3->MobEffects.DIG_SPEED;case 4->MobEffects.DIG_SLOWDOWN;case 5->MobEffects.DAMAGE_BOOST;case 6->MobEffects.HEAL;case 7->MobEffects.HARM;case 8->MobEffects.JUMP;case 9->MobEffects.CONFUSION;case 10->MobEffects.REGENERATION;case 11->MobEffects.DAMAGE_RESISTANCE;case 12->MobEffects.FIRE_RESISTANCE;case 13->MobEffects.WATER_BREATHING;case 14->MobEffects.INVISIBILITY;case 15->MobEffects.BLINDNESS;case 16->MobEffects.NIGHT_VISION;case 17->MobEffects.HUNGER;case 18->MobEffects.WEAKNESS;case 19->MobEffects.POISON;case 20->MobEffects.WITHER;case 21->MobEffects.HEALTH_BOOST;case 22->MobEffects.ABSORPTION;case 23->MobEffects.SATURATION;case 32->thaumcraft.effects.TaintEffects.FLUX_TAINT;case 33->thaumcraft.effects.TaintEffects.VIS_EXHAUST;case 34->thaumcraft.effects.WarpEffects.INFECTIOUS_EXHAUST;case 35->thaumcraft.effects.WarpEffects.UNNATURAL_HUNGER;case 36->thaumcraft.effects.WarpEffects.WARP_WARD;case 37->thaumcraft.effects.WarpEffects.DEATH_GAZE;case 38->thaumcraft.effects.WarpEffects.BLURRED_VISION;case 39->thaumcraft.effects.WarpEffects.SUN_SCORNED;case 40->thaumcraft.effects.WarpEffects.THAUMARHIA;default->null;};}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){var value=aspect(stack);if(value!=null)lines.add(thaumcraft.network.ClientKnowledge.snapshot().knows(value)?net.minecraft.network.chat.Component.literal(value.getName()+" x1"):net.minecraft.network.chat.Component.translatable("tc.aspect.unknown"));super.appendHoverText(stack,context,lines,flag);}
}

package thaumcraft.items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.knowledge.KnowledgeService;
public final class ZombieBrainItem extends Item {
 public ZombieBrainItem(){super(new Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(.2f).build()));}
 @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
  if(entity instanceof ServerPlayer player){boolean sticky=level.random.nextFloat()<.1f;int amount=sticky?1:1+level.random.nextInt(3);KnowledgeService.mutate(player,k->{int s=k.stickyWarp()+(sticky?amount:0),t=k.temporaryWarp()+(sticky?0:amount);k.setWarp(k.permanentWarp(),s,t,k.permanentWarp()+s+t);return true;});}
  var result=super.finishUsingItem(stack,level,entity);
  // ItemFood drew this probability from the world, after the brain's warp draw.
  if(!level.isClientSide&&level.random.nextFloat()<.8f)entity.addEffect(new MobEffectInstance(MobEffects.HUNGER,600,0));
  return result;
 }
}

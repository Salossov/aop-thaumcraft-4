package thaumcraft.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import thaumcraft.api.IRepairable;

/** TC4's fast-drawing, automatic-release bow; native ammunition and enchantment hooks. */
public final class BoneBowItem extends BowItem implements IRepairable {
    public BoneBowItem(){super(new Properties().durability(512));}
    public static float power(int ticks){float f=ticks/10f;return Math.min(1,(f*f+f*2)/3);}
    @Override public int getEnchantmentValue(){return 3;}
    @Override public void onUseTick(Level l,LivingEntity user,ItemStack stack,int remaining){if(getUseDuration(stack,user)-remaining>18)user.releaseUsingItem();}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){var bow=p.getItemInHand(hand);boolean ammo=!p.getProjectile(bow).isEmpty()||BowEvents.enchantment(bow,l,net.minecraft.world.item.enchantment.Enchantments.INFINITY)>0;var result=net.neoforged.neoforge.event.EventHooks.onArrowNock(bow,l,p,hand,ammo);if(result!=null)return result;if(!ammo&&!p.hasInfiniteMaterials())return InteractionResultHolder.fail(bow);p.startUsingItem(hand);return InteractionResultHolder.consume(bow);}
    @Override public void releaseUsing(ItemStack bow,Level l,LivingEntity user,int remaining){if(!(user instanceof Player p))return;var ammo=p.getProjectile(bow);boolean free=p.hasInfiniteMaterials()||BowEvents.enchantment(bow,l,net.minecraft.world.item.enchantment.Enchantments.INFINITY)>0;int charge=net.neoforged.neoforge.event.EventHooks.onArrowLoose(bow,l,p,getUseDuration(bow,user)-remaining,!ammo.isEmpty()||free);if(charge<0)return;float f=power(charge);if(f<.1f||ammo.isEmpty()&&!free)return;if(ammo.isEmpty())ammo=new ItemStack(Items.ARROW);var projectiles=draw(bow,ammo,p);if(l instanceof ServerLevel server&&!projectiles.isEmpty())shoot(server,p,p.getUsedItemHand(),bow,projectiles,f*3.75f,1,false,null);l.playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,1,1/(l.random.nextFloat()*.4f+1.2f)+f*.5f);p.awardStat(Stats.ITEM_USED.get(this));}
    @Override public AbstractArrow customArrow(AbstractArrow arrow,ItemStack ammo,ItemStack bow){arrow.setBaseDamage(arrow.getBaseDamage()+.5);if(arrow.getOwner() instanceof LivingEntity owner)arrow.setPos(owner.getX()-net.minecraft.util.Mth.cos(owner.getYRot()/180f*(float)Math.PI)*.16f,owner.getEyeY()-.10000000014901161,owner.getZ()-net.minecraft.util.Mth.sin(owner.getYRot()/180f*(float)Math.PI)*.16f);return arrow;}
    @Override protected void shootProjectile(LivingEntity user,net.minecraft.world.entity.projectile.Projectile arrow,int index,float speed,float inaccuracy,float yawOffset,LivingEntity target){BowEvents.launch(arrow,user,speed,yawOffset);}
}

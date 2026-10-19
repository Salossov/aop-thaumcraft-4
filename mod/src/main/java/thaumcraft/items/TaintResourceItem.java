package thaumcraft.items;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** TC4 resource metadata 11/12: unstable taint poisons its carrier and consumes one matching item. */
public final class TaintResourceItem extends Item {
    public TaintResourceItem(){super(new Properties());}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
        super.inventoryTick(stack,level,entity,slot,selected);
        if(level.isClientSide||!(entity instanceof LivingEntity living)||living.isInvertedHealAndHarm()||living.hasEffect(thaumcraft.effects.TaintEffects.FLUX_TAINT)||level.random.nextInt(4321)>stack.getCount())return;
        living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.effects.TaintEffects.FLUX_TAINT,120,0,false,true));
        if(entity instanceof Player player){player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.taint_item_poison",stack.getHoverName().copy().withStyle(net.minecraft.ChatFormatting.DARK_PURPLE,net.minecraft.ChatFormatting.ITALIC)));for(var item:player.getInventory().items)if(item.is(stack.getItem())){item.shrink(1);player.getInventory().setChanged();break;}}
    }
}

package thaumcraft.items;

import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.entities.BottleTaintProjectile;

/** Original throwable bottle: eight per stack and no returned empty bottle. */
public final class BottleTaintItem extends Item {
    public BottleTaintItem(){super(new Properties().stacksTo(8));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        level.playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,.5f,.4f/(level.random.nextFloat()*.4f+.8f));
        if(!level.isClientSide){level.addFreshEntity(BottleTaintProjectile.thrown(level,player));if(!player.isCreative())stack.shrink(1);}
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}

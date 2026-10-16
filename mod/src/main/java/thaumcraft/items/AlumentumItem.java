package thaumcraft.items;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import thaumcraft.entities.*;
public final class AlumentumItem extends Item{
 public AlumentumItem(){super(new Properties());}
 @Override public int getBurnTime(ItemStack stack,RecipeType<?> type){return 6400;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);level.playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,.3f,.4f/(level.random.nextFloat()*.4f+.8f));if(!level.isClientSide){var projectile=new AlumentumProjectile(R3Entities.ALUMENTUM.get(),level);projectile.launch(player);level.addFreshEntity(projectile);if(!player.isCreative())stack.shrink(1);}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
}

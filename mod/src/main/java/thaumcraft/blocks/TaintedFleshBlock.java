package thaumcraft.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
public final class TaintedFleshBlock extends Block {
    public TaintedFleshBlock(){super(Properties.of().strength(1.5f,10).sound(SoundType.SLIME_BLOCK));}
    @Override public void stepOn(Level level,BlockPos pos,BlockState state,Entity entity){if(!level.isClientSide&&entity instanceof LivingEntity living&&!living.isInvertedHealAndHarm()&&level.random.nextInt(entity instanceof Player?100:20)==0)living.addEffect(new net.minecraft.world.effect.MobEffectInstance(thaumcraft.effects.TaintEffects.FLUX_TAINT,entity instanceof Player?80:160));super.stepOn(level,pos,state,entity);}
}

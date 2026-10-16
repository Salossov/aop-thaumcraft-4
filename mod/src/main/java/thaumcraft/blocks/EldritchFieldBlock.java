package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Original blockAiry metadata11, independent of the room's metadata10 stone trap. */
public final class EldritchFieldBlock extends Block {
    public static java.util.function.BiConsumer<Level,BlockPos> clientEffects=(level,pos)->{};
    // Native isAir also controls section occupancy and skips writes into empty sections.
    // Retain a real stored block; TC4 callers use their original air predicate explicitly.
    public EldritchFieldBlock(){super(Properties.of().replaceable().noCollission().noOcclusion().forceSolidOff().randomTicks().strength(100,50).noLootTable().lightLevel(s->8));}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){level.removeBlock(pos,false);}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){tick(state,level,pos,random);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(entity instanceof thaumcraft.api.entities.IEldritchMob)return;var motion=entity.getDeltaMovement();entity.setDeltaMovement(motion.x*.66,motion.y,motion.z*.66);if(level.isClientSide)return;if(level.random.nextInt(100)==0)entity.hurt(level.damageSources().wither(),1);if(entity instanceof Player player)player.causeFoodExhaustion(.05f);if(entity instanceof LivingEntity living)living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100,1,true,true));}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){clientEffects.accept(level,pos);}
}

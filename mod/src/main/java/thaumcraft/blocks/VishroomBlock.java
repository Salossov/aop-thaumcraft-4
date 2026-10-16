package thaumcraft.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

public final class VishroomBlock extends BushBlock {
    public static java.util.function.Consumer<net.minecraft.world.phys.Vec3> clientWisp=position->{};
    public static final MapCodec<VishroomBlock> CODEC=simpleCodec(VishroomBlock::new);
    public VishroomBlock(){this(Properties.ofFullCopy(Blocks.DANDELION).lightLevel(state->8));}
    public VishroomBlock(BlockBehaviour.Properties properties){super(properties);}
    @Override protected MapCodec<? extends BushBlock> codec(){return CODEC;}
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return Block.box(1.6,0,1.6,14.4,12.8,14.4);}
    @Override public void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(!level.isClientSide&&entity instanceof LivingEntity living)living.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));super.entityInside(state,level,pos,entity);}
    @Override public int getFlammability(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.core.Direction face){return 100;}
    @Override public int getFireSpreadSpeed(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.core.Direction face){return 60;}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,net.minecraft.util.RandomSource random){if(random.nextInt(3)==0){var r=level.random;clientWisp.accept(new net.minecraft.world.phys.Vec3((float)pos.getX()+.5f+(r.nextFloat()-r.nextFloat())*.4f,(float)pos.getY()+.3f,(float)pos.getZ()+.5f+(r.nextFloat()-r.nextFloat())*.4f));}}
}

package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class ShockFieldBlock extends Block {
    public record Spark(net.minecraft.world.phys.Vec3 position,float scale,float red,float green,float blue,float alpha){}
    public static java.util.function.Consumer<Spark> clientSpark=effect->{};
    private static final IntegerProperty AGE=IntegerProperty.create("age",0,4);
    public ShockFieldBlock(){super(BlockBehaviour.Properties.of().air().noCollission().noOcclusion().replaceable().strength(0).noLootTable().randomTicks().lightLevel(state->8));registerDefaultState(stateDefinition.any().setValue(AGE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(AGE);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){level.removeBlock(pos,false);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){
        entity.hurt(level.damageSources().magic(),1+level.random.nextInt(2));entity.setDeltaMovement(entity.getDeltaMovement().multiply(.8,1,.8));
        if(!level.isClientSide&&level.random.nextInt(100)==0)level.removeBlock(pos,false);
    }
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){
        float height=random.nextFloat()*.33f;var r=level.random;
        clientSpark.accept(new Spark(new net.minecraft.world.phys.Vec3((float)pos.getX()+r.nextFloat(),(float)pos.getY()+.1515f+height/2,(float)pos.getZ()+r.nextFloat()),.33f+height,.65f+r.nextFloat()*.1f,1,1,.8f));
        if(random.nextInt(50)==0)level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),thaumcraft.registry.R2Sounds.JACOBS.get(),net.minecraft.sounds.SoundSource.BLOCKS,.5f,1+(random.nextFloat()-random.nextFloat())*.2f,false);
    }
}

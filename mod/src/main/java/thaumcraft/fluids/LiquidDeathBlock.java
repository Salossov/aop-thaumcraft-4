package thaumcraft.fluids;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;

public final class LiquidDeathBlock extends Block implements BucketPickup {
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> DISSOLVE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("thaumcraft","dissolve"));
    public LiquidDeathBlock(){super(Properties.of().noCollission().noOcclusion().strength(100).noLootTable().lightLevel(s->8));registerDefaultState(stateDefinition.any().setValue(FiniteDeathFluid.QUANTA,4));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FiniteDeathFluid.QUANTA);}
    @Override protected FluidState getFluidState(BlockState s){return R4Fluids.DEATH.get().defaultFluidState().setValue(FiniteDeathFluid.QUANTA,s.getValue(FiniteDeathFluid.QUANTA));}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c){return Shapes.empty();}
    @Override protected void onPlace(BlockState s,Level level,BlockPos pos,BlockState old,boolean moved){level.scheduleTick(pos,this,7);}
    @Override protected void neighborChanged(BlockState s,Level level,BlockPos pos,Block neighbor,BlockPos from,boolean moving){level.scheduleTick(pos,this,7);}
    @Override protected void tick(BlockState s,ServerLevel level,BlockPos pos,RandomSource random){flow(level,pos,random);}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){thaumcraft.client.CleansingParticles.bubble(pos.getX()+random.nextFloat(),pos.getY()+.1+.225*(state.getValue(FiniteDeathFluid.QUANTA)-1),pos.getZ()+random.nextFloat(),.075f+random.nextFloat()*.075f,.3f-random.nextFloat()*.1f,0,.4f+random.nextFloat()*.1f,.8f);if(random.nextInt(50)==0)level.playLocalSound(pos.getX()+random.nextFloat(),pos.getY()+.9,pos.getZ()+random.nextFloat(),net.minecraft.sounds.SoundEvents.LAVA_POP,net.minecraft.sounds.SoundSource.BLOCKS,.1f+random.nextFloat()*.1f,.9f+random.nextFloat()*.15f,false);}
    private int units(Level level,BlockPos pos){if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos))return -1;var s=level.getBlockState(pos);return s.is(this)?s.getValue(FiniteDeathFluid.QUANTA):s.isAir()?0:-1;}
    public static void setUnits(Level level,BlockPos pos,int n){level.setBlockAndUpdate(pos,n<=0?Blocks.AIR.defaultBlockState():thaumcraft.registry.R4Content.LIQUID_DEATH.get().defaultBlockState().setValue(FiniteDeathFluid.QUANTA,n));}
    public void flow(ServerLevel level,BlockPos pos,RandomSource random){
        if(!level.getBlockState(pos).is(this))return;int amount=units(level,pos);var down=pos.below();int below=units(level,down);
        if(below>=0&&below<4){int moved=Math.min(amount,4-below);setUnits(level,down,below+moved);amount-=moved;setUnits(level,pos,amount);if(amount<=1)return;}
        if(amount<=1)return;
        var neighbors=new ArrayList<BlockPos>();int total=amount;
        for(var d:Direction.Plane.HORIZONTAL){var target=pos.relative(d);int q=units(level,target);if(q>=0&&q<amount-1){neighbors.add(target);total+=q;}}
        if(neighbors.isEmpty())return;Collections.shuffle(neighbors,new Random(random.nextLong()));neighbors.add(pos);
        int each=total/neighbors.size(),remainder=total%neighbors.size();for(var target:neighbors)setUnits(level,target,each+(remainder-->0?1:0));
    }
    @Override protected void entityInside(BlockState s,Level level,BlockPos pos,Entity entity){if(!level.isClientSide&&entity instanceof LivingEntity living)living.hurt(new net.minecraft.world.damagesource.DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DISSOLVE)),s.getValue(FiniteDeathFluid.QUANTA));}
    @Override public ItemStack pickupBlock(net.minecraft.world.entity.player.Player player,LevelAccessor level,BlockPos pos,BlockState state){if(state.getValue(FiniteDeathFluid.QUANTA)!=4)return ItemStack.EMPTY;level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);return new ItemStack(thaumcraft.registry.R4Content.DEATH_BUCKET.get());}
    @Override public Optional<net.minecraft.sounds.SoundEvent> getPickupSound(){return Optional.of(net.minecraft.sounds.SoundEvents.BUCKET_FILL);}
}

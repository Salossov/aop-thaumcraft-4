package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.MapColor;
import thaumcraft.effects.TaintEffects;
import thaumcraft.world.WorldBiomes;

public final class TaintSolidBlock extends Block {
    private final boolean fibrous;
    public TaintSolidBlock(boolean fibrous) { super(Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(fibrous?1.75f:1.5f,10).sound(SoundType.SLIME_BLOCK).randomTicks());this.fibrous=fibrous; }
    public static boolean canFallBelow(Level level,BlockPos pos) {
        if(!level.hasChunkAt(pos)) return false;
        for(var check:BlockPos.betweenClosed(pos.offset(-1,-1,-1),pos.offset(1,1,1))) if(level.getBlockState(check).is(BlockTags.LOGS)) return false;
        var state=level.getBlockState(pos);
        if(isDenseGoo(state))return false;
        return state.isAir()||state.is(Blocks.FIRE)||state.getBlock() instanceof TaintFibresBlock||state.canBeReplaced()||!state.getFluidState().isEmpty();
    }
    public static boolean isDenseGoo(BlockState state){return state.is(thaumcraft.registry.R2Content.FLUX_GOO.get())&&state.getValue(thaumcraft.fluids.FiniteFluxFluid.QUANTA)>=5;}
    private boolean fall(ServerLevel level,BlockPos source,BlockPos target,BlockState state) {
        if(target.getY()<=level.getMinBuildHeight()||!canFallBelow(level,target.below())) return false;
        return level.addFreshEntity(new thaumcraft.entities.FallingTaint(level,source,target,state));
    }
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        TaintFibresBlock.spreadBiome(level,pos,random);
        if(fibrous) {
            if(fall(level,pos,pos,state)) return;
            if(level.isEmptyBlock(pos.above())) {
                var direction=Direction.from2DDataValue(random.nextInt(4));boolean slope=true;
                for(int i=0;i<4;i++) slope&=level.isEmptyBlock(pos.relative(direction).below(i))&&level.getBlockState(pos.below(i)).getBlock() instanceof TaintSolidBlock;
                if(slope&&fall(level,pos,pos.relative(direction),state)) return;
            }
        }
        var target=pos.offset(random.nextInt(3)-1,random.nextInt(5)-3,random.nextInt(3)-1);
        if(level.hasChunkAt(target)&&WorldBiomes.is(level,target,WorldBiomes.TAINT)) {
            TaintFibresBlock.spread(level,target);
            if(fibrous) {
                if(thaumcraft.config.ThaumcraftConfig.SPAWN_TAINT_SPORE.get()&&level.isEmptyBlock(pos.above())&&random.nextInt(200)==0) {
                    if(level.getEntitiesOfClass(thaumcraft.entities.TaintSwarmer.class,new net.minecraft.world.phys.AABB(pos).inflate(16)).isEmpty()) {
                        var spore=thaumcraft.entities.R2Entities.TAINT_SWARMER.get().create(level);
                        spore.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);level.removeBlock(pos,false);level.addFreshEntity(spore);
                    }
                } else {
                    boolean enclosed=level.getBlockState(pos.above()).getBlock() instanceof TaintSolidBlock;
                    for(var direction:Direction.Plane.HORIZONTAL) enclosed&=level.getBlockState(pos.relative(direction)).getBlock() instanceof TaintSolidBlock;
                    if(enclosed) thaumcraft.fluids.FluxBlock.setUnits(level,pos,thaumcraft.registry.R2Content.FLUX_GOO.get(),9);
                }
            }
        } else if(fibrous&&random.nextInt(20)==0) thaumcraft.fluids.FluxBlock.setUnits(level,pos,thaumcraft.registry.R2Content.FLUX_GOO.get(),9);
        else if(!fibrous&&random.nextInt(10)==0) level.setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());
    }
    @Override public void stepOn(Level level,BlockPos pos,BlockState state,Entity entity) {
        if(!level.isClientSide&&entity instanceof LivingEntity living&&!living.isInvertedHealAndHarm()
                &&level.random.nextInt(entity instanceof Player?100:20)==0) living.addEffect(new MobEffectInstance(TaintEffects.FLUX_TAINT,entity instanceof Player?80:160));
        super.stepOn(level,pos,state,entity);
    }
}

package thaumcraft.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.effects.WarpEffects;
import thaumcraft.knowledge.KnowledgeService;

public final class PurifyingLiquidBlock extends LiquidBlock {
    public PurifyingLiquidBlock(){super(R4Fluids.PURE.get(),Properties.ofFullCopy(Blocks.WATER).lightLevel(s->10));}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,net.minecraft.util.RandomSource random){thaumcraft.client.CleansingParticles.pureBubble(pos.getX()+random.nextFloat(),pos.getY()+.125*(8-state.getValue(LEVEL)),pos.getZ()+random.nextFloat());if(random.nextInt(25)==0)level.playLocalSound(pos.getX()+random.nextFloat(),pos.getY()+.9,pos.getZ()+random.nextFloat(),net.minecraft.sounds.SoundEvents.LAVA_POP,net.minecraft.sounds.SoundSource.BLOCKS,.1f+random.nextFloat()*.1f,.9f+random.nextFloat()*.15f,false);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){
        super.entityInside(state,level,pos,entity);
        if(state.getValue(LEVEL)!=0||!(entity instanceof ServerPlayer player)||player.hasEffect(WarpEffects.WARP_WARD))return;
        int permanent=KnowledgeService.get(player).permanentWarp();
        int divisor=Math.max(1,(int)Math.sqrt(permanent));
        player.addEffect(new MobEffectInstance(WarpEffects.WARP_WARD,Math.min(32000,200000/divisor),0,true,true));
        level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
    }
}

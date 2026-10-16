package thaumcraft.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.effects.WarpEffects;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.*;

public final class SanitySoapItem extends Item {
    public SanitySoapItem(){super(new Properties());}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 200;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.BLOCK;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){clean(stack,level,user);return stack;}
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining){if(getUseDuration(stack,user)-remaining>195)clean(stack,level,user);}
    @Override public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining){if(level.isClientSide)thaumcraft.client.CleansingParticles.soap(user,5);if(!level.isClientSide&&level.random.nextInt(5)==0)level.playSound(null,user.blockPosition(),R2Sounds.ROOTS.get(),net.minecraft.sounds.SoundSource.PLAYERS,.1f,1.5f+level.random.nextFloat()*.2f);}
    private static void clean(ItemStack stack,Level level,LivingEntity user){
        if(level.isClientSide){thaumcraft.client.CleansingParticles.soap(user,20);return;}
        if(!(user instanceof ServerPlayer player)||stack.isEmpty())return;
        // Soap is consumed even in creative in TC4; permanent warp is never removable.
        stack.shrink(1);float chance=.33f;
        if(player.hasEffect(WarpEffects.WARP_WARD))chance+=.25f;
        if(level.getBlockState(player.blockPosition()).is(R4Content.PURIFYING_FLUID.get()))chance+=.25f;
        boolean sticky=level.random.nextFloat()<chance;
        KnowledgeService.mutate(player,k->{k.setWarp(k.permanentWarp(),Math.max(0,k.stickyWarp()-(sticky?1:0)),0,k.warpCounter());return true;});
        level.playSound(null,player.blockPosition(),R2Sounds.CRAFT_START.get(),net.minecraft.sounds.SoundSource.PLAYERS,.25f,1);
    }
}

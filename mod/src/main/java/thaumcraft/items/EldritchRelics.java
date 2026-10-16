package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.knowledge.KnowledgeService;

public final class EldritchRelics {
    public static final class KnowledgeFragment extends Item {
        public KnowledgeFragment(){super(new Properties());}
        @Override public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(player instanceof ServerPlayer server){KnowledgeService.mutate(server,k->{for(var aspect:Aspects.PRIMALS)k.addPool(aspect,1+server.getRandom().nextInt(2));return true;});stack.consume(1,player);}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    }
    public static final class SanityChecker extends Item{public SanityChecker(){super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));}}
    public static final class SinisterStone extends Item{public SinisterStone(){super(new Properties().stacksTo(1).rarity(Rarity.RARE));}}
    public static final class Eye extends Item{public Eye(){super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));}@Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<Component> lines,TooltipFlag flags){lines.add(Component.translatable("item.ItemEldritchObject.text.1").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));}}
    public static final class CrimsonRites extends Item{public CrimsonRites(){super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));}@Override public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand){if(player instanceof ServerPlayer server)KnowledgeService.mutate(server,knowledge->knowledge.completeResearch("CRIMSON"));return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);}}
    public static final class Pearl extends Item {
        public Pearl(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));}
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<Component> lines,TooltipFlag flags){lines.add(Component.translatable("item.ItemEldritchObject.text.5").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));lines.add(Component.translatable("item.ItemEldritchObject.text.6").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));}
        @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){var level=context.getLevel();if(!(level.getBlockEntity(context.getClickedPos()) instanceof AuraNodeBlockEntity node))return InteractionResult.PASS;if(context.getPlayer() instanceof ServerPlayer player){boolean researched=KnowledgeService.get(player).completedResearch().contains("PRIMNODE");stack.consume(1,player);for(var key:java.util.List.copyOf(node.state.base.keySet())){var aspect=thaumcraft.api.aspects.Aspect.getAspect(key);int base=node.state.base.get(key);if(aspect!=null&&aspect.isPrimal())base+=-2+level.random.nextInt(researched?9:6);else if(level.random.nextBoolean())base--;node.state.base.put(key,Math.max(0,base));}for(var aspect:Aspects.PRIMALS){int amount=level.random.nextInt(researched?4:3);if(amount>node.state.base.getOrDefault(aspect.getTag(),0)){node.state.base.put(aspect.getTag(),amount);node.state.current.merge(aspect.getTag(),1,Integer::sum);}}node.state.modifier=switch(node.state.modifier){case FADING->level.random.nextBoolean()?NodeState.Modifier.PALE:NodeState.Modifier.FADING;case PALE->level.random.nextBoolean()?NodeState.Modifier.NONE:NodeState.Modifier.PALE;case NONE->level.random.nextInt(5)==0?NodeState.Modifier.BRIGHT:NodeState.Modifier.NONE;default->node.state.modifier;};node.changedAndSync();var pos=context.getClickedPos();level.explode(null,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,3+level.random.nextFloat()*(researched?3:5),true,net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);for(int i=0;i<33;i++){var flux=pos.offset(level.random.nextInt(6)-level.random.nextInt(6),level.random.nextInt(6)-level.random.nextInt(6),level.random.nextInt(6)-level.random.nextInt(6));if(level.hasChunkAt(flux)&&level.isEmptyBlock(flux)){var block=flux.getY()<pos.getY()?thaumcraft.registry.R2Content.FLUX_GOO.get():thaumcraft.registry.R2Content.FLUX_GAS.get();level.setBlockAndUpdate(flux,block.defaultBlockState().setValue(thaumcraft.fluids.FiniteFluxFluid.QUANTA,8));}}}return InteractionResult.sidedSuccess(level.isClientSide);}
    }
    private EldritchRelics(){}
}

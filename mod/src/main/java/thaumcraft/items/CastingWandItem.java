package thaumcraft.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import thaumcraft.blocks.*;
import thaumcraft.registry.R2Content;

public final class CastingWandItem extends Item implements thaumcraft.api.wands.ICastingWand {
    private record Target(net.minecraft.resources.ResourceKey<Level> dimension,net.minecraft.core.BlockPos pos) {}
    private static final java.util.Map<ServerPlayer,Target> ACTIVE=new java.util.WeakHashMap<>();
    public enum Kind { WAND, STAFF, SCEPTRE }
    private final Kind kind;
    public CastingWandItem() { this(Kind.WAND); }
    public CastingWandItem(Kind kind) { super(new Properties().stacksTo(1)); this.kind=kind; }
    public Kind kind(){return kind;}
    @Override public void inventoryTick(ItemStack stack,Level level,net.minecraft.world.entity.Entity entity,int slot,boolean selected){
        if(!level.isClientSide&&entity instanceof Player player){var rod=WandMaterials.rod(stack);if(rod!=null)rod.onUpdate(stack,player);}
    }
    @Override public net.minecraft.network.chat.Component getName(ItemStack stack){
        if(stack.is(R2Content.WAND.get()))return super.getName(stack);
        return net.minecraft.network.chat.Component.translatable("item.thaumcraft.composite_"+kind.name().toLowerCase(java.util.Locale.ROOT),
            net.minecraft.network.chat.Component.translatable("item.thaumcraft.cap."+WandMaterials.parts(stack).cap()),
            net.minecraft.network.chat.Component.translatable("item.thaumcraft.rod."+WandMaterials.parts(stack).rod()));
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flags) {
        super.appendHoverText(stack,context,lines,flags);
        var parts=WandMaterials.parts(stack);
        if(flags.hasShiftDown()){
            lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.wand.component.cap",net.minecraft.network.chat.Component.translatable("thaumcraft.wand.cap."+parts.cap())).withStyle(net.minecraft.ChatFormatting.GRAY));
            lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.wand.component.rod",net.minecraft.network.chat.Component.translatable("thaumcraft.wand.rod."+parts.rod())).withStyle(net.minecraft.ChatFormatting.GRAY));
        }else lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.wand.details").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        Player player=context.level()!=null&&context.level().isClientSide?thaumcraft.client.WandClientEffects.tooltipPlayer():null;
        // Original shows each stored vis amount per primal; shift expands into the
        // consumption% and the equipped focus's cost per cast/tick.
        StringBuilder amounts=new StringBuilder();boolean any=false;boolean shift=flags.hasShiftDown();
        for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS){int vis=WandVis.get(stack,aspect);if(vis<=0)continue;any=true;
            float mod=WandMaterials.modifier(stack,aspect,player!=null?WandVis.discount(player,aspect):0);
            if(shift){var focus=WandFocus.get(stack);var fc=focus.getItem() instanceof thaumcraft.api.wands.ItemFocusBasic f?f.getVisCost(focus).get(aspect):null;lines.add(net.minecraft.network.chat.Component.literal(" §"+aspect.getChatColor()+aspect.getName()+"§r x "+(vis/100f)+", §o("+new java.text.DecimalFormat("#####.##").format(mod*100)+"% "+net.minecraft.network.chat.Component.translatable("tc.vis.cost").getString()+")"+(fc!=null?"§r, "+new java.text.DecimalFormat("#####.##").format(fc*mod/100f)+" "+net.minecraft.network.chat.Component.translatable(focus.getItem() instanceof thaumcraft.api.wands.ItemFocusBasic i&&i.isVisCostPerTick(focus)?"item.Focus.cost2":"item.Focus.cost1").getString():"")));}
            else{if(amounts.length()>0)amounts.append(" | ");amounts.append("§"+aspect.getChatColor()+vis/100f);}}
        // Stored amounts are a fixed-width tooltip component; values remain live.
        int capacity=WandVis.capacity(stack);
        var capLine=net.minecraft.network.chat.Component.translatable("item.capacity.text").append(" "+java.math.BigDecimal.valueOf(capacity,2).stripTrailingZeros().toPlainString());
        if(any&&!shift){int total=0,num=0;for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)if(WandVis.get(stack,aspect)>0){total+=(int)(WandMaterials.modifier(stack,aspect,player!=null?WandVis.discount(player,aspect):0)*100);num++;}capLine.append("§r ("+(num>0?total/num:0)+"% "+net.minecraft.network.chat.Component.translatable("tc.vis.costavg").getString()+")");}
        lines.add(capLine.withStyle(net.minecraft.ChatFormatting.GOLD));
        var focus=WandFocus.get(stack);if(!focus.isEmpty()){lines.add(net.minecraft.network.chat.Component.literal("§5§o"+focus.getHoverName().getString()));focus.getItem().appendHoverText(focus,context,lines,flags);}
    }
    @Override public java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> getTooltipImage(ItemStack stack){
        if(thaumcraft.client.WandClientEffects.expandedTooltip())return java.util.Optional.empty();
        var amounts=new java.util.LinkedHashMap<String,Integer>();for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)amounts.put(aspect.getTag(),WandVis.get(stack,aspect));
        return java.util.Optional.of(new WandVisTooltip(amounts,WandVis.capacity(stack)));
    }
    @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean slotChanged) {
        // Vis charge writes arrive as stack updates every tick; only a real
        // item/slot change should replay the raise animation.
        return slotChanged||!oldStack.is(newStack.getItem())||!WandMaterials.parts(oldStack).equals(WandMaterials.parts(newStack))||!ItemStack.isSameItemSameComponents(WandFocus.get(oldStack),WandFocus.get(newStack));
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) { return 72000; }
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){
        var level=context.getLevel();var player=context.getPlayer();
        // Modern sneak-use skips the block's useItemOn; TC4's wand hook still empties the crucible.
        if(player!=null&&player.isShiftKeyDown()&&level.getBlockEntity(context.getClickedPos()) instanceof thaumcraft.production.CrucibleBlockEntity crucible){if(!level.isClientSide)crucible.empty();return InteractionResult.sidedSuccess(level.isClientSide);}
        return OwnedBlockInteractions.removeWithWand(context);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();var player=context.getPlayer();if(player==null) return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof JarredNodeBlockEntity jar){if(!level.isClientSide&&level.mayInteract(player,pos))jar.release();return InteractionResult.sidedSuccess(level.isClientSide);}
        if((level.getBlockState(pos).is(Blocks.GLASS)||level.getBlockState(pos).is(net.minecraft.tags.BlockTags.WOODEN_SLABS))&&player instanceof ServerPlayer server&&NodeJarActions.pack(server,context.getItemInHand(),pos))return InteractionResult.CONSUME;
        if(player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof thaumcraft.production.EldritchAltarBlockEntity altar&&altar.activate(server,context.getItemInHand()))return InteractionResult.CONSUME;
        if(player instanceof ServerPlayer server&&(level.getBlockState(pos).is(Blocks.OBSIDIAN)||level.getBlockState(pos).is(Blocks.NETHER_BRICKS)||level.getBlockState(pos).is(Blocks.IRON_BARS))&&thaumcraft.production.InfernalFurnaceBlock.activate(server,context.getItemInHand(),pos))return InteractionResult.CONSUME;
        if(player instanceof ServerPlayer server&&(level.getBlockState(pos).is(thaumcraft.registry.R4Content.ADVANCED_CONSTRUCT.get())||level.getBlockState(pos).is(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT.get()))&&thaumcraft.production.AdvancedAlchemyBlock.activate(server,context.getItemInHand(),pos))return InteractionResult.CONSUME;
        if(level.getBlockState(pos).is(Blocks.BOOKSHELF)) {
            if(!level.isClientSide) { level.removeBlock(pos,false);var drop=new ItemEntity(level,pos.getX()+0.5,pos.getY()+0.3,pos.getZ()+0.5,new ItemStack(R2Content.BOOK.get()));drop.setDeltaMovement(Vec3.ZERO);level.addFreshEntity(drop);for(var observer:((net.minecraft.server.level.ServerLevel)level).players())if(observer.distanceToSqr(drop)<1024)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(observer,new thaumcraft.network.BookBirthPayload(drop.getId(),drop.position())); }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(level.getBlockState(pos).is(R2Content.TABLE.get())) {
            if(!level.isClientSide) { level.setBlock(pos,R2Content.ARCANE_WORKBENCH.get().defaultBlockState(),3);if(level.getBlockEntity(pos) instanceof TableBlockEntity tile) { tile.setItem(10,context.getItemInHand().copy());player.setItemInHand(context.getHand(),ItemStack.EMPTY);tile.changedAndSync(); } }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(level.getBlockState(pos).is(Blocks.CAULDRON)) {
            if(!level.isClientSide) level.setBlockAndUpdate(pos,thaumcraft.registry.R3Content.CRUCIBLE.get().defaultBlockState());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(level.getBlockState(pos).is(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT.get())){
            var lower=pos;if(!level.getBlockState(lower.below()).is(thaumcraft.registry.R3Content.CRUCIBLE.get()))lower=lower.below();
            if(level.getBlockState(lower).is(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT.get())&&level.getBlockState(lower.above()).is(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT.get())&&level.getBlockState(lower.below()).is(thaumcraft.registry.R3Content.CRUCIBLE.get())){
                if(player instanceof ServerPlayer server&&thaumcraft.knowledge.KnowledgeService.get(server).completedResearch().contains("THAUMATORIUM")&&WandVis.consume(context.getItemInHand(),java.util.Map.of("ignis",15,"ordo",30,"aqua",30),WandVis.discount(player),true)){
                    var state=thaumcraft.registry.R3Content.THAUMATORIUM.get().defaultBlockState().setValue(thaumcraft.production.ThaumatoriumBlock.FACING,context.getClickedFace());
                    level.setBlock(lower,state,2);level.setBlockAndUpdate(lower.above(),state.setValue(thaumcraft.production.ThaumatoriumBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
                    level.playSound(null,lower,thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
                }return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if(level.getBlockEntity(pos) instanceof thaumcraft.production.EssentiaBlockEntity) return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
            if(player instanceof ServerPlayer server) ACTIVE.put(server,new Target(level.dimension(),pos.immutable()));
            player.startUsingItem(context.getHand());return InteractionResult.CONSUME;
        }
        if(WandFocus.supports(context.getItemInHand())&&!WandFocus.get(context.getItemInHand()).isEmpty()){if(player instanceof ServerPlayer server)FocusCasting.begin(server,context.getHand());else if(FocusCasting.continuous(context.getItemInHand()))player.startUsingItem(context.getHand());return InteractionResult.CONSUME;}
        return InteractionResult.PASS;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var wand=player.getItemInHand(hand);if(!WandFocus.supports(wand)||WandFocus.get(wand).isEmpty())return InteractionResultHolder.pass(wand);if(player instanceof ServerPlayer server)FocusCasting.begin(server,hand);else{var f=WandFocus.get(wand);if(f.getItem() instanceof thaumcraft.api.wands.ItemFocusBasic i)thaumcraft.client.WandHud.castAt(level.getGameTime()+(i.getActivationCooldown(f)+49)/50);if(FocusCasting.continuous(wand))player.startUsingItem(hand);}return InteractionResultHolder.consume(wand);}
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining) {
        if(!(entity instanceof ServerPlayer player)) return;var hit=player.pick(8,0,false);var original=ACTIVE.get(player);
        if(original==null){FocusCasting.tick(player,stack);return;}
        if(!original.dimension.equals(level.dimension())||!(hit instanceof BlockHitResult blockHit)||!original.pos.equals(blockHit.getBlockPos())||hit.getType()!=HitResult.Type.BLOCK||!(level.getBlockEntity(blockHit.getBlockPos()) instanceof AuraNodeBlockEntity node)) {stopDrain(player);player.stopUsingItem();return;}
        if(remaining%5==0) node.charge(player,stack);
    }
    private static void stopDrain(ServerPlayer player){var target=ACTIVE.remove(player);if(target==null)return;var level=player.server.getLevel(target.dimension);if(level!=null&&level.hasChunkAt(target.pos)&&level.getBlockEntity(target.pos) instanceof AuraNodeBlockEntity node)node.stopDrain(player);}
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) {if(entity instanceof ServerPlayer player){stopDrain(player);FocusCasting.stop(player);}}
}

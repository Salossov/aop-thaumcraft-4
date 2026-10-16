package thaumcraft.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;

public final class ResearchNotesItem extends Item {
    public ResearchNotesItem() { super(new Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)); }
    @Override public boolean isFoil(ItemStack stack){return true;}
    @Override public net.minecraft.network.chat.Component getName(ItemStack stack){var note=ResearchNotes.read(stack);return net.minecraft.network.chat.Component.translatable(note!=null&&note.complete?"item.discovery.name":"item.researchnotes.name");}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flags){
        if(UnknownResearchNotes.isUnknown(stack)){lines.add(net.minecraft.network.chat.Component.translatable("item.researchnotes.unknown.1").withStyle(net.minecraft.ChatFormatting.GOLD));lines.add(net.minecraft.network.chat.Component.translatable("item.researchnotes.unknown.2").withStyle(net.minecraft.ChatFormatting.BLUE));}
        var note=ResearchNotes.read(stack);if(note!=null&&!note.key.isEmpty()){
            lines.add(net.minecraft.network.chat.Component.translatable("tc.research_name."+note.key).withStyle(net.minecraft.ChatFormatting.GOLD));
            lines.add(net.minecraft.network.chat.Component.translatable("tc.research_text."+note.key).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC));
        }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(UnknownResearchNotes.isUnknown(stack)){if(player instanceof ServerPlayer serverPlayer)UnknownResearchNotes.open(stack,serverPlayer);return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
        var note=ResearchNotes.read(stack);
        if(note==null||!note.complete) return InteractionResultHolder.pass(stack);
        if(player instanceof ServerPlayer serverPlayer) {
            var definition=ThaumcraftData.get(serverPlayer.server).research().get(note.key);
            var known=KnowledgeService.get(serverPlayer).completedResearch();
            if(definition==null||definition.virtual()||known.contains(note.key)||!known.containsAll(definition.parents())||!known.containsAll(definition.hiddenParents())) return InteractionResultHolder.fail(stack);
            if(KnowledgeService.mutate(serverPlayer,k->k.completeResearch(note.key))){stack.shrink(1);net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,new thaumcraft.network.ResearchCompletePayload(note.key));}
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}

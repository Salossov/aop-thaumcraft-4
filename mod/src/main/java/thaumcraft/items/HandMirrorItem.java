package thaumcraft.items;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.production.*;

public final class HandMirrorItem extends Item {
    public HandMirrorItem(){super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));}
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MirrorBlockEntity tile&&!tile.essentia()){if(!context.getLevel().isClientSide){tile.address().write(stack);context.getLevel().playSound(null,context.getClickedPos(),thaumcraft.registry.R2Sounds.JAR.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,2);if(context.getPlayer()!=null)context.getPlayer().displayClientMessage(Component.translatable("tc.handmirrorlinked").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE,net.minecraft.ChatFormatting.ITALIC),false);}return InteractionResult.sidedSuccess(context.getLevel().isClientSide);}return InteractionResult.PASS;}
    public static MirrorBlockEntity destination(net.minecraft.server.level.ServerPlayer player,ItemStack stack){var link=MirrorLink.from(stack);if(link==null)return null;var target=link.target(player.server,true);return target!=null&&!target.essentia()?target:null;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(player instanceof net.minecraft.server.level.ServerPlayer server&&MirrorLink.from(stack)!=null){if(destination(server,stack)==null){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.remove("MirrorLink"));player.displayClientMessage(Component.translatable("tc.handmirrorerror").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE,net.minecraft.ChatFormatting.ITALIC),false);}else {int slot=hand==InteractionHand.MAIN_HAND?player.getInventory().selected:40;server.openMenu(new SimpleMenuProvider((id,inventory,p)->new thaumcraft.menu.HandMirrorMenu(id,inventory,slot),stack.getHoverName()),buffer->buffer.writeVarInt(slot));}}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    @Override public boolean isFoil(ItemStack stack){return MirrorLink.from(stack)!=null;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);MirrorItem.linkedTooltip(stack,lines);}
}

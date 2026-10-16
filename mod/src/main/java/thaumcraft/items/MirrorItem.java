package thaumcraft.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import thaumcraft.production.*;

public final class MirrorItem extends BlockItem {
    public MirrorItem(MirrorBlock block){super(block,new Properties());}
    @Override public InteractionResult useOn(UseOnContext context){if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MirrorBlockEntity tile&&tile.essentia()==(getBlock()==thaumcraft.registry.R3Content.ESSENTIA_MIRROR.get())){if(!context.getLevel().isClientSide){tile.address().write(context.getItemInHand());context.getLevel().playSound(null,context.getClickedPos(),thaumcraft.registry.R2Sounds.JAR.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,2);}return InteractionResult.sidedSuccess(context.getLevel().isClientSide);}return super.useOn(context);}
    @Override public boolean isFoil(ItemStack stack){return MirrorLink.from(stack)!=null;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);linkedTooltip(stack,lines);}
    public static void linkedTooltip(ItemStack stack,List<Component> lines){var link=MirrorLink.from(stack);if(link!=null)lines.add(Component.translatable("tc.handmirrorlinkedto").append(" "+link.pos().getX()+","+link.pos().getY()+","+link.pos().getZ()+" — "+link.dimension().location()).withStyle(ChatFormatting.DARK_PURPLE));}
}

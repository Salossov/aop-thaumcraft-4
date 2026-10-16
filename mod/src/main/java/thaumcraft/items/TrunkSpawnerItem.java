package thaumcraft.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

public final class TrunkSpawnerItem extends Item {
    public TrunkSpawnerItem(){super(new Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext context){var player=context.getPlayer();if(player==null)return InteractionResult.PASS;var pos=context.getClickedPos().relative(context.getClickedFace());if(!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()))return InteractionResult.FAIL;if(!context.getLevel().isClientSide){var trunk=thaumcraft.entities.R3Entities.TRUNK.get().create(context.getLevel());if(trunk==null)return InteractionResult.FAIL;trunk.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,player.getYRot(),0);trunk.configure(context.getItemInHand(),player);if(!context.getLevel().noCollision(trunk))return InteractionResult.FAIL;context.getLevel().addFreshEntity(trunk);context.getItemInHand().consume(1,player);}return InteractionResult.sidedSuccess(context.getLevel().isClientSide);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flags){var tag=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();int upgrade=tag.contains("upgrade")?tag.getByte("upgrade"):-1;if(upgrade>=0&&upgrade<6)lines.add(net.minecraft.network.chat.Component.translatable("item.thaumcraft."+java.util.List.of("air","earth","fire","water","order","entropy").get(upgrade)+"_golem_upgrade").withStyle(net.minecraft.ChatFormatting.BLUE));if(tag.contains("inventory"))lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.trunk.packed").withStyle(net.minecraft.ChatFormatting.GRAY));}
}

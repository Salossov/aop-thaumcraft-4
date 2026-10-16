package thaumcraft.items;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.entities.*;

public final class GolemPlacerItem extends Item {
    public final GolemMaterial material;
    public GolemPlacerItem(GolemMaterial material){super(new Properties().stacksTo(1));this.material=material;}
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){if(context.getPlayer()==null||context.getPlayer().isShiftKeyDown())return InteractionResult.PASS;var level=context.getLevel();var pos=context.getClickedPos().relative(context.getClickedFace());if(!level.getWorldBorder().isWithinBounds(pos)||!level.getBlockState(pos).getCollisionShape(level,pos).isEmpty())return InteractionResult.FAIL;
        if(level instanceof ServerLevel server){var golem=R3Entities.GOLEM.get().create(server);if(golem==null)return InteractionResult.FAIL;golem.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,context.getPlayer().getYRot(),0);golem.initialize(material,context.getPlayer().getUUID(),pos,context.getClickedFace(),stack);if(!server.noCollision(golem)||!server.addFreshEntity(golem))return InteractionResult.FAIL;if(!context.getPlayer().getAbilities().instabuild)stack.shrink(1);}
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public boolean isFoil(ItemStack stack){return advanced(stack);}
    public static boolean advanced(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("advanced");}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);if(advanced(stack))lines.add(Component.translatable("tc.advancedgolem").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));}
}

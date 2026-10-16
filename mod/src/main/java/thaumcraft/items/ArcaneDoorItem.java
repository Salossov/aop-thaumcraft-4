package thaumcraft.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.registry.R4Content;

public final class ArcaneDoorItem extends BlockItem {
    public ArcaneDoorItem(){super(R4Content.ARCANE_DOOR.get(),new Item.Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext c){if(c.getClickedFace()!=net.minecraft.core.Direction.UP)return InteractionResult.PASS;var result=super.useOn(c);if(result.consumesAction()&&c.getPlayer()!=null&&c.getPlayer().isCreative())c.getItemInHand().shrink(1);return result;}
}

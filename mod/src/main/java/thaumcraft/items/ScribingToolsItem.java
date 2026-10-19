package thaumcraft.items;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.blocks.*;
import thaumcraft.registry.R2Content;

public final class ScribingToolsItem extends Item implements thaumcraft.api.IScribeTools {
    public ScribingToolsItem() { super(new Properties().durability(100)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();var stack=context.getItemInHand();
        if(context.getPlayer()==null||!level.getBlockState(pos).is(R2Content.TABLE.get())) return InteractionResult.PASS;
        for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}) {
            var other=pos.relative(direction);if(!level.getBlockState(other).is(R2Content.TABLE.get())) continue;
            if(!level.isClientSide) {
                var block=R2Content.RESEARCH_TABLE.get();
                level.setBlock(pos,block.defaultBlockState().setValue(TableBlock.FACING,direction),2);
                level.setBlock(other,block.defaultBlockState().setValue(TableBlock.FACING,direction.getOpposite()).setValue(TableBlock.SLAVE,true),2);
                if(level.getBlockEntity(pos) instanceof TableBlockEntity tile) { tile.setItem(0,stack.copyWithCount(1));tile.changedAndSync(); }
                if(!context.getPlayer().getAbilities().instabuild) stack.shrink(1);
                level.updateNeighborsAt(pos,block);level.updateNeighborsAt(other,block);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }return InteractionResult.PASS;
    }
}

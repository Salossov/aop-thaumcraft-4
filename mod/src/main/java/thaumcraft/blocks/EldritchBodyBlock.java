package thaumcraft.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Original eldritch metadata2 is an invisible, solid part of the floating obelisk. */
public final class EldritchBodyBlock extends Block {
    public EldritchBodyBlock(){super(Properties.of().strength(50,12000).lightLevel(s->8).noOcclusion().noLootTable().isValidSpawn((s,l,p,e)->false));}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected void onRemove(BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,BlockState replacement,boolean moving){EldritchStructure.removed(level,pos,state,replacement);super.onRemove(state,level,pos,replacement,moving);}
}

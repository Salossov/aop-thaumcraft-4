package thaumcraft.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R5Content;

/** Original eldritch metadata 1 (obelisk) and 3 (cap), retaining full cube collision. */
public final class EldritchObeliskBlock extends BaseEntityBlock {
    private final boolean cap;
    public EldritchObeliskBlock(boolean cap){super(Properties.of().strength(50,12000).lightLevel(s->8).noOcclusion().isValidSpawn((s,l,p,e)->false));this.cap=cap;}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new EldritchObeliskBlock(cap));}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving){EldritchStructure.removed(level,pos,state,replacement);super.onRemove(state,level,pos,replacement,moving);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return cap?new EldritchCapBlockEntity(pos,state):new EldritchObeliskBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return cap?null:createTickerHelper(type,R5Content.OBELISK_ENTITY.get(),EldritchObeliskBlockEntity::tick);}
}

package thaumcraft.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R5Content;

public final class EldritchPortalBlock extends BaseEntityBlock {
    public static final TagKey<Block> SUPPORT=TagKey.create(net.minecraft.core.registries.Registries.BLOCK,ResourceLocation.parse("thaumcraft:legacy_eldritch"));
    public EldritchPortalBlock(){super(Properties.of().strength(-1,200000).noCollission().noOcclusion().noLootTable().lightLevel(s->15).isValidSpawn((s,l,p,e)->false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new EldritchPortalBlock());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos neighbor,boolean piston){if(!level.getBlockState(pos.above()).is(SUPPORT)||!level.getBlockState(pos.below()).is(SUPPORT))level.removeBlock(pos,false);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EldritchPortalBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,R5Content.PORTAL_ENTITY.get(),EldritchPortalBlockEntity::tick);}
}

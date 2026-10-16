package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R2Content;

public final class AuraNodeBlock extends Block implements EntityBlock {
    public AuraNodeBlock() { super(Properties.of().strength(2,5).noOcclusion().noCollission().noLootTable().lightLevel(s->7)); }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.player.Player player){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node){
            for(var entry:node.state.current.entrySet()){
                var aspect=thaumcraft.api.aspects.Aspect.getAspect(entry.getKey());if(aspect==null||entry.getValue()<5)continue;
                var essence=thaumcraft.items.WispEssenceItem.of(aspect);for(int count=1+entry.getValue()/10;count>0;count-=64)popResource(level,pos,essence.copyWithCount(Math.min(64,count)));
            }
            level.playSound(null,pos,thaumcraft.registry.R2Sounds.CRAFT_FAIL.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override public net.minecraft.world.item.ItemStack getCloneItemStack(BlockState state,net.minecraft.world.phys.HitResult target,net.minecraft.world.level.LevelReader level,BlockPos pos,net.minecraft.world.entity.player.Player player){
        var stack=new net.minecraft.world.item.ItemStack(this);if(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node)node.saveToItem(stack,level.registryAccess());return stack;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new AuraNodeBlockEntity(pos,state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return box(4,4,4,12,12,12); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide||type!=R2Content.NODE_ENTITY.get()) return null;return (l,p,s,t)->((AuraNodeBlockEntity)t).serverTick();
    }
}

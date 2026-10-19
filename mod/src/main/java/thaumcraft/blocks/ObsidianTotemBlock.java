package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import thaumcraft.registry.R2Content;

/** Cosmetic-solid metadata 0 and 8. Face textures retain Java's signed remainder. */
public class ObsidianTotemBlock extends Block {
    public static final IntegerProperty STYLE=IntegerProperty.create("style",0,2),PHASE=IntegerProperty.create("phase",0,18);
    private final boolean node;
    public ObsidianTotemBlock(boolean node){super(Properties.of().strength(30,999).sound(SoundType.STONE));this.node=node;registerDefaultState(stateDefinition.any().setValue(STYLE,1).setValue(PHASE,9));}
    public boolean containsNode(){return node;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(STYLE,PHASE);}
    public BlockState at(LevelReader level,BlockPos pos){return defaultBlockState().setValue(PHASE,phase(pos)).setValue(STYLE,style(level,pos));}
    public static int phase(BlockPos pos){return pos.getX()%4+pos.getY()%4+pos.getZ()%4+9;}
    public static int faceTexture(BlockPos pos,Direction face){return Math.abs((face.get3DDataValue()+pos.getX()%4+pos.getY()%4+pos.getZ()%4)%4);}
    private static boolean totem(net.minecraft.world.level.block.state.BlockState state){return state.getBlock() instanceof ObsidianTotemBlock;}
    private static int style(LevelReader level,BlockPos pos){return totem(level.getBlockState(pos.above()))?0:totem(level.getBlockState(pos.below()))?2:1;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return at(c.getLevel(),c.getClickedPos());}
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos adjacent){return direction.getAxis()==Direction.Axis.Y?state.setValue(STYLE,style(level,pos)):state;}
    /** Only the charged variant owns a tile; plain totem segments must not save empty tile records. */
    public static final class Node extends ObsidianTotemBlock implements EntityBlock {
        public Node(){super(true);}
        @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new AuraNodeBlockEntity(pos,state);}
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&type==R2Content.NODE_ENTITY.get()?(l,p,s,t)->((AuraNodeBlockEntity)t).serverTick():null;}
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.player.Player player){
        if(node&&!level.isClientSide&&level.getBlockEntity(pos) instanceof AuraNodeBlockEntity tile){
            for(var entry:tile.state.current.entrySet())if(entry.getValue()>=5){var aspect=thaumcraft.api.aspects.Aspect.getAspect(entry.getKey());if(aspect!=null)for(int i=0;i<=entry.getValue()/10;i++)popResource(level,pos,thaumcraft.items.WispEssenceItem.of(aspect));}
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
}

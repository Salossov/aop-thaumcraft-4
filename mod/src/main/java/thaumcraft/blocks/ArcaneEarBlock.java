package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.registry.R4Content;

/** Original tuned note receiver; redstone output remains independent of space above it. */
public final class ArcaneEarBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
    public ArcaneEarBlock(){super(Properties.of().strength(2.5f,10).sound(SoundType.WOOD).noOcclusion().instrument(NoteBlockInstrument.BASS));registerDefaultState(stateDefinition.any().setValue(POWERED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(POWERED);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArcaneEarBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide&&t==R4Content.EAR_ENTITY.get()?(w,p,state,tile)->((ArcaneEarBlockEntity)tile).tick():null;}
    @Override protected boolean isSignalSource(BlockState s){return true;}
    @Override protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(POWERED)?15:0;}
    @Override protected int getDirectSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return getSignal(s,l,p,d);}
    @Override public boolean canConnectRedstone(BlockState s,BlockGetter l,BlockPos p,Direction d){return true;}
    @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(!l.isClientSide&&l.getBlockEntity(p) instanceof ArcaneEarBlockEntity ear)ear.updateTone();}
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block neighbor,BlockPos from,boolean moving){if(!l.isClientSide&&l.getBlockEntity(p) instanceof ArcaneEarBlockEntity ear)ear.updateTone();}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){super.onRemove(s,l,p,next,moving);if(!s.is(next.getBlock())&&s.getValue(POWERED)){l.updateNeighborsAt(p,this);l.updateNeighborsAt(p.below(),this);}}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof ArcaneEarBlockEntity ear){ear.changePitch();ear.triggerNote(true);}return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected boolean triggerEvent(BlockState s,Level l,BlockPos p,int instrument,int note){if(instrument>=0&&instrument<5)l.playSound(null,p,ArcaneEarBlockEntity.INSTRUMENTS[instrument].getSoundEvent().value(),SoundSource.RECORDS,3,NoteBlock.getPitchFromNote(note));if(l.isClientSide)l.addParticle(ParticleTypes.NOTE,p.getX()+.5,p.getY()+1.2,p.getZ()+.5,note/24.0,0,0);return true;}
}

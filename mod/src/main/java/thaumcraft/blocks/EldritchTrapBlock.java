package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.valueproviders.UniformInt;

/** The concealed room trap (legacy eldritch metadata 10). */
public final class EldritchTrapBlock extends DropExperienceBlock implements EntityBlock {
    public EldritchTrapBlock(){super(UniformInt.of(1,4),Properties.of().strength(15,30).noOcclusion().noLootTable().sound(SoundType.STONE).lightLevel(s->0).isValidSpawn((s,l,p,t)->false));}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EldritchTrapBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&type==thaumcraft.registry.R5Content.TRAP_ENTITY.get()?(l,p,s,t)->((EldritchTrapBlockEntity)t).serverTick():null;}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,net.minecraft.util.RandomSource random){
        int x=pos.getX()+random.nextInt(2)-random.nextInt(2),y=pos.getY()+random.nextInt(2)-random.nextInt(2),z=pos.getZ()+random.nextInt(2)-random.nextInt(2);
        if(level.isEmptyBlock(new BlockPos(x,y,z)))PavingStoneBlock.runes.accept(new PavingStoneBlock.Rune(
            (float)x+random.nextFloat(),(float)y+random.nextFloat(),(float)z+random.nextFloat(),
            .5f+random.nextFloat()*.5f,random.nextFloat()*.3f,.9f+random.nextFloat()*.1f,16+random.nextInt(4),0));
    }
}

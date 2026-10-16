package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.registry.R5Content;

public final class ManaPodBlockEntity extends BlockEntity implements IAspectContainer {
    public Aspect aspect;
    public ManaPodBlockEntity(BlockPos pos,BlockState state){super(R5Content.MANA_POD_ENTITY.get(),pos,state);}
    public void checkGrowth(){
        if(level==null||level.isClientSide)return;int age=getBlockState().getValue(ManaPodBlock.AGE);
        if(age<7)level.setBlock(worldPosition,getBlockState().setValue(ManaPodBlock.AGE,++age),3);
        if(age<=2)return;
        if(age==3){var neighbors=new AspectList();if(aspect!=null)neighbors.add(aspect,1);
            for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST})if(level.getBlockEntity(worldPosition.relative(direction)) instanceof ManaPodBlockEntity tile&&tile.aspect!=null)neighbors.add(tile.aspect,1);
            if(neighbors.size()>1){var available=new ArrayList<Aspect>();var all=neighbors.getAspects();for(int i=0;i<all.length;i++){available.add(all[i]);for(int j=0;j<all.length;j++)if(i!=j){var combined=Aspect.combine(all[i],all[j]);if(combined!=null){available.add(combined);available.add(combined);}}}if(!available.isEmpty()){aspect=available.get(level.random.nextInt(available.size()));sync();}}
            if(neighbors.size()>=1&&aspect==null){aspect=neighbors.getAspects()[0];for(var candidate:neighbors.getAspects())if(neighbors.getAmount(candidate)>neighbors.getAmount(aspect))aspect=candidate;sync();}
        }
        if(aspect==null){aspect=level.random.nextInt(8)==0?Aspects.PLANT:Aspects.PRIMALS.get(level.random.nextInt(Aspects.PRIMALS.size()));sync();}
    }
    public void setAspect(Aspect value){aspect=value;sync();}
    public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);if(aspect!=null)tag.putString("aspect",aspect.getTag());}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);aspect=Aspect.getAspect(tag.getString("aspect"));}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public AspectList getAspects(){return aspect!=null&&getBlockState().getValue(ManaPodBlock.AGE)==7?new AspectList().add(aspect,1):null;}
    @Override public void setAspects(AspectList aspects){}
    @Override public boolean doesContainerAccept(Aspect aspect){return false;}
    @Override public int addToContainer(Aspect aspect,int amount){return 0;}
    @Override public boolean takeFromContainer(Aspect aspect,int amount){return false;}
    @Override public boolean doesContainerContainAmount(Aspect aspect,int amount){return false;}
    @Override public int containerContains(Aspect aspect){return 0;}
}

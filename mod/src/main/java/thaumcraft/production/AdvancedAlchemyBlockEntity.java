package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.registry.R4Content;

/** Original vis-powered, thrown-item furnace with a single mixed 500-point tank. */
public final class AdvancedAlchemyBlockEntity extends BlockEntity {
    public final EssentiaStore store=new EssentiaStore(500,true,false);
    public int heat,powerPerditio,powerAqua,cooldown;
    public boolean destroy;
    private int counter;
    public AdvancedAlchemyBlockEntity(BlockPos p,BlockState s){super(R4Content.ADVANCED_FURNACE_ENTITY.get(),p,s);}
    public void serverTick(){if(level==null||level.isClientSide)return;if(counter%20==0)for(int part=0;part<18;part++){if(part==13)continue;var p=worldPosition.offset(AdvancedAlchemyBlock.offset(part));if(!level.hasChunkAt(p))continue;var s=level.getBlockState(p);if(!s.is(R4Content.ADVANCED_FURNACE.get())||s.getValue(AdvancedAlchemyBlock.PART)!=part){destroy=true;break;}}if(destroy){AdvancedAlchemyBlock.restore(level,worldPosition,null);return;}if(cooldown>0)cooldown--;
        if(++counter%5==0){int before=heat;heat--;if(heat<=500)heat+=VisNetHandler.drainVis(level,worldPosition,Aspect.getAspect("ignis"),50);if(powerPerditio<=500)powerPerditio+=VisNetHandler.drainVis(level,worldPosition,Aspect.getAspect("perditio"),50);if(powerAqua<=500)powerAqua+=VisNetHandler.drainVis(level,worldPosition,Aspect.getAspect("aqua"),50);setChanged();if(before/50!=heat/50)sync();}
    }
    public boolean process(ItemStack stack){if(level==null||level.isClientSide||cooldown>0||stack.isEmpty())return false;var values=thaumcraft.data.ThaumcraftData.aspects(level.getServer(),stack);int total=values.values().stream().mapToInt(Integer::intValue).sum();if(total<=0||total+store.amount()>500||heat<total*2||powerPerditio<total||powerAqua<total)return false;
        heat-=total*2;powerPerditio-=total;powerAqua-=total;cooldown=(int)(5+Math.max(0,(1-heat/500f)*100));values.forEach((tag,n)->store.addToContainer(Aspect.getAspect(tag),n));sync();return true;
    }
    public void sync(){setChanged();if(level==null)return;var s=getBlockState();int light=heat>100?Math.min(15,(int)(heat/500f*12)):0;if(s.getValue(AdvancedAlchemyBlock.LIGHT)!=light)level.setBlock(worldPosition,s.setValue(AdvancedAlchemyBlock.LIGHT,light),2);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);for(var d:Direction.Plane.HORIZONTAL){var p=worldPosition.relative(d);level.updateNeighbourForOutputSignal(p,R4Content.ADVANCED_FURNACE.get());}}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Heat",heat);t.putInt("PowerPerditio",powerPerditio);t.putInt("PowerAqua",powerAqua);var a=new CompoundTag();store.getAspects().asMap().forEach((aspect,n)->a.putInt(aspect.getTag(),n));t.put("Essentia",a);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);heat=t.getInt("Heat");powerPerditio=t.getInt("PowerPerditio");powerAqua=t.getInt("PowerAqua");var saved=new AspectList();var a=t.getCompound("Essentia");for(var key:a.getAllKeys()){var aspect=Aspect.getAspect(key);if(aspect==null||a.getInt(key)<0)throw new IllegalArgumentException("Invalid saved advanced-furnace aspect "+key);saved.add(aspect,a.getInt(key));}store.setAspects(saved);cooldown=counter=0;destroy=false;}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}

}

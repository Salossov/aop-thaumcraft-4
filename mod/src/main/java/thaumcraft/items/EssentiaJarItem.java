package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.aspects.*;

/** Uses the same BLOCK_ENTITY_DATA consumed by the real native transport entity on placement. */
public class EssentiaJarItem extends BlockItem implements IEssentiaJarItem {
    public EssentiaJarItem(Block block){super(block,new Properties());}
    @Override public AspectList getAspects(ItemStack stack){
        var contents=stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag().getCompound("Essentia");
        var result=new AspectList();
        for(var key:contents.getAllKeys()){
            var aspect=Aspect.getAspect(key);int amount=contents.getInt(key);if(aspect!=null&&amount>0)result.add(aspect,amount);
        }
        return result.size()==0?null:result;
    }
    @Override public void setAspects(ItemStack stack,AspectList aspects){
        var data=stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag();var contents=new CompoundTag();
        if(aspects!=null)aspects.asMap().forEach((aspect,amount)->{if(amount>0)contents.putInt(aspect.getTag(),amount);});
        data.put("Essentia",contents);data.putString("id","thaumcraft:essentia_transport");stack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(data));
    }
}

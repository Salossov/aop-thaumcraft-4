package thaumcraft.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.items.ThaumicArmorItem;

public final class ArmorClientExtensions implements IClientItemExtensions {
    @Override public int getDefaultDyeColor(ItemStack stack){return stack.getItem() instanceof ThaumicArmorItem armor&&armor.kind()==ThaumicArmorItem.Kind.VOID_ROBE?0xff6a3880:IClientItemExtensions.super.getDefaultDyeColor(stack);}
    private final Map<String,OriginalArmorGeometry> models=new HashMap<>();
    @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){
        String geometry=stack.getItem() instanceof ThaumicArmorItem armor?switch(armor.kind()){case FORTRESS->"fortress";case VOID_ROBE->"void_robe";default->null;}:stack.getItem() instanceof thaumcraft.items.CultistArmorItem cultist?switch(cultist.kind()){case PLATE->"cultist_plate";case LEADER->"cultist_leader";case ROBE->"cultist_robe";default->null;}:null;
        if(geometry==null)return original;boolean inner=slot==EquipmentSlot.LEGS||stack.getItem() instanceof thaumcraft.items.CultistArmorItem&&slot==EquipmentSlot.HEAD;
        var model=models.computeIfAbsent(geometry+"/"+inner,key->OriginalArmorGeometry.bake(geometry,inner));model.prepare(wearer,stack);return model;
    }
}

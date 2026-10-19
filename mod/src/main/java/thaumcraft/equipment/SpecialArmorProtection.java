package thaumcraft.equipment;

import java.util.ArrayList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.ISpecialArmor;
import thaumcraft.api.SpecialArmorProperties;
import thaumcraft.items.ThaumicArmorItem;

/** Forge's player armor path, selected only when an addon supplies special armor. */
public final class SpecialArmorProtection {
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD};
    public static boolean active(LivingEntity wearer) {
        if(!(wearer instanceof Player))return false;
        for(var stack:wearer.getArmorSlots())if(stack.getItem() instanceof ISpecialArmor)return true;
        return false;
    }
    public static float absorb(Player wearer,DamageSource source,float incoming) {
        var pieces=new ItemStack[SLOTS.length];var properties=new ArrayList<SpecialArmorProperties>();
        for(int slot=0;slot<SLOTS.length;slot++) {
            var stack=pieces[slot]=wearer.getItemBySlot(SLOTS[slot]);
            SpecialArmorProperties prop=null;
            if(stack.getItem() instanceof ISpecialArmor special)prop=special.getProperties(wearer,stack,source,incoming,slot);
            else if(stack.getItem() instanceof ArmorItem armor) {
                prop=coreProperties(wearer,stack,armor,source);
                if(prop==null&&!source.is(DamageTypeTags.BYPASSES_ARMOR))
                    prop=new SpecialArmorProperties(0,armor.getDefense()/25D,stack.getMaxDamage()+1-stack.getDamageValue());
            }
            if(prop!=null) {
                var copy=new SpecialArmorProperties(prop.priority,prop.absorbRatio,prop.absorbMax);copy.slot=slot;properties.add(copy);
            }
        }
        var result=SpecialArmorProperties.apply(incoming,properties);
        for(var absorbed:result.absorbed()) {
            var stack=pieces[absorbed.slot()];
            if(stack.getItem() instanceof ISpecialArmor special)
                special.damageArmor(wearer,stack,source,absorbed.durabilityDamage(),absorbed.slot());
            else if(!(stack.getItem() instanceof ThaumicArmorItem armor&&(armor.kind()==ThaumicArmorItem.Kind.FORTRESS||armor.kind()==ThaumicArmorItem.Kind.VOID_ROBE)&&source.is(DamageTypes.FALL)))
                stack.hurtAndBreak(absorbed.durabilityDamage(),wearer,SLOTS[absorbed.slot()]);
            if(stack.isEmpty())wearer.setItemSlot(SLOTS[absorbed.slot()],ItemStack.EMPTY);
        }
        // Old EntityPlayer returned immediately for ApplyArmor <= 0; modern DamageContainer
        // must receive zero rather than a negative value that could increase health.
        return Math.max(0,result.remaining());
    }
    public static int display(Player wearer) {
        int total=0;
        for(int slot=0;slot<SLOTS.length;slot++) {
            var stack=wearer.getItemBySlot(SLOTS[slot]);
            if(stack.getItem() instanceof ISpecialArmor special)total+=special.getArmorDisplay(wearer,stack,slot);
            else if(stack.getItem() instanceof ArmorItem armor)total+=armor.getDefense();
        }
        return total;
    }
    private static SpecialArmorProperties coreProperties(Player player,ItemStack stack,ArmorItem item,DamageSource source) {
        if(!(item instanceof ThaumicArmorItem armor)||(armor.kind()!=ThaumicArmorItem.Kind.FORTRESS&&armor.kind()!=ThaumicArmorItem.Kind.VOID_ROBE))return null;
        int priority=0;double ratio=armor.getDefense()/25D;
        if(source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC)||source.is(DamageTypes.THORNS)) {priority=1;ratio=armor.getDefense()/35D;}
        else if(armor.kind()==ThaumicArmorItem.Kind.FORTRESS&&(source.is(DamageTypeTags.IS_FIRE)||source.is(DamageTypeTags.IS_EXPLOSION))) {priority=1;ratio=armor.getDefense()/20D;}
        else if(source.is(DamageTypeTags.BYPASSES_ARMOR))ratio=0;
        if(armor.kind()==ThaumicArmorItem.Kind.FORTRESS) {
            double multiplier=.875;
            for(int slot=1;slot<4;slot++) {
                var piece=player.getItemBySlot(SLOTS[slot]);
                if(piece.getItem() instanceof ThaumicArmorItem other&&other.kind()==ThaumicArmorItem.Kind.FORTRESS) {
                    multiplier+=.125;
                    if(piece.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains("mask"))multiplier+=.05;
                }
            }
            ratio*=multiplier;
        }
        return new SpecialArmorProperties(priority,ratio,stack.getMaxDamage()+1-stack.getDamageValue());
    }
    private SpecialArmorProtection() {}
}

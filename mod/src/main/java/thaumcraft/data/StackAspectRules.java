package thaumcraft.data;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import thaumcraft.items.WispEssenceItem;

/** TC4 stack bonuses, restricted to reviewed IDs and data-defined old potion/enchantment rules. */
public final class StackAspectRules {
    public record Settings(Map<String,Map<String,Integer>> items,Map<String,Map<String,Integer>> potionBases,Map<String,Map<String,Integer>> effects,Map<String,Map<String,Integer>> enchantments){public static final Settings EMPTY=new Settings(Map.of(),Map.of(),Map.of(),Map.of());public Settings{items=Map.copyOf(items);potionBases=Map.copyOf(potionBases);effects=Map.copyOf(effects);enchantments=Map.copyOf(enchantments);}}
    public static Map<String,Integer> aspects(ItemStack stack,Map<String,Integer> base,Settings settings){
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();var values=new LinkedHashMap<>(base);minimum(values,settings.items.getOrDefault(id,Map.of()),1);
        if(stack.is(Items.POTION)||stack.is(Items.SPLASH_POTION)){var contents=stack.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY);String potion=potionKey(contents);var initial=settings.potionBases.get(potion);if(initial!=null){minimum(values,initial,1);var effects=contents.potion().map(holder->holder.value().getEffects()).orElse(List.of());if(!effects.isEmpty()&&stack.is(Items.SPLASH_POTION))values.merge("perditio",2,Math::max);for(var effect:effects){String key=BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();minimum(values,settings.effects.getOrDefault(key,Map.of()),effect.getAmplifier()+1);}}}
        if(stack.is(thaumcraft.registry.R3Content.ESSENTIA_PHIAL.get())||stack.is(thaumcraft.registry.R3Content.ESSENTIA_CRYSTAL.get())) {var aspect=thaumcraft.registry.R3Content.aspect(stack);if(aspect!=null)values.merge(aspect.getTag(),stack.is(thaumcraft.registry.R3Content.ESSENTIA_PHIAL.get())?8:1,Integer::sum);}
        if(stack.getItem() instanceof WispEssenceItem)WispEssenceItem.aspects(stack).asMap().forEach((aspect,amount)->values.merge(aspect.getTag(),amount,Integer::sum));
        var enchants=stack.getOrDefault(stack.is(Items.ENCHANTED_BOOK)?DataComponents.STORED_ENCHANTMENTS:DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY);int magic=0;
        if(!values.isEmpty()||settings.items.containsKey(id))for(var entry:enchants.entrySet()){var key=entry.getKey().unwrapKey().map(resource->resource.location().toString()).orElse("");var rule=settings.enchantments.get(key);if(rule!=null){minimum(values,rule,entry.getIntValue());magic+=entry.getIntValue();}}
        if(magic>0)values.merge("praecantatio",magic,Math::max);values.entrySet().removeIf(e->e.getValue()<=0);return Map.copyOf(values);
    }
    private static void minimum(Map<String,Integer> values,Map<String,Integer> minimum,int level){minimum.forEach((tag,amount)->values.merge(tag,amount*level,Math::max));}
    private static String potionKey(PotionContents contents){return contents.potion().flatMap(holder->holder.unwrapKey()).map(key->key.location().toString()).orElse("minecraft:water");}
    public static String scanKey(ItemStack stack,Map<String,String> groups){String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();String key=groups.getOrDefault(id,id);return stack.is(Items.POTION)||stack.is(Items.SPLASH_POTION)?key+"/"+potionKey(stack.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY)):key;}
    /** Legacy slot scans sent item ID and metadata; retain the potion identity, not extra NBT. */
    public static ItemStack inventoryScanStack(ItemStack source){var stack=new ItemStack(source.getItem());var contents=source.get(DataComponents.POTION_CONTENTS);if(contents!=null)stack.set(DataComponents.POTION_CONTENTS,new PotionContents(contents.potion(),Optional.empty(),List.of()));return stack;}
    private StackAspectRules(){}
}

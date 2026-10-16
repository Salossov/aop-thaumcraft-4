package thaumcraft.loot;

import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.internal.WeightedRandomLoot;
import thaumcraft.api.aspects.*;
import thaumcraft.items.WandVis;
import thaumcraft.registry.R3Equipment;

/** Source weights and the original rare gear algorithm; native enchantments/components replace metadata. */
public final class LegacyLoot {
    private static boolean initialized;
    public static void initialize(){if(initialized)return;initialized=true;LegacyLootEntries.register();var amulet=new ItemStack(R3Equipment.CURIOS.get("vis_amulet_lesser").get());var random=RandomSource.create();for(var a:List.of(Aspects.AIR,Aspects.EARTH,Aspects.FIRE,Aspects.WATER,Aspects.ORDER,Aspects.ENTROPY))WandVis.set(amulet,a,random.nextInt(5)*100);ThaumcraftApi.addLootBagItem(amulet,6,1,2);}
    public static ItemStack stack(String id,int count){var key=ResourceLocation.parse(id);if(!BuiltInRegistries.ITEM.containsKey(key))throw new IllegalStateException("Missing original loot item "+key);return new ItemStack(BuiltInRegistries.ITEM.get(key),count);}
    public static ItemStack potion(int metadata,String effect,int duration,int amplifier,String name){var stack=new ItemStack((metadata&0x4000)!=0?Items.SPLASH_POTION:Items.POTION);var holder=BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect)).orElseThrow();stack.set(DataComponents.POTION_CONTENTS,new PotionContents(Optional.empty(),Optional.empty(),List.of(new MobEffectInstance(holder,duration,amplifier))));stack.set(DataComponents.ITEM_NAME,Component.translatable("item.minecraft."+((metadata&0x4000)!=0?"splash_potion":"potion")+".effect."+name));return stack;}
    public static ItemStack generate(ServerLevel level,int rarity,RandomSource random){
        ItemStack result;
        if(rarity>0&&random.nextFloat()<.025f*rarity){result=gear(level,rarity,random);if(result.isEmpty())return generate(level,rarity,random);}
        else{var entries=rarity==1?WeightedRandomLoot.lootBagUncommon:rarity==2?WeightedRandomLoot.lootBagRare:WeightedRandomLoot.lootBagCommon;result=WeightedRandom.getRandomItem(random,entries).orElseThrow(()->new IllegalStateException("Empty TC4 loot pool")).item.copy();}
        if(result.is(Items.BOOK))result=enchant(level,result,(int)(5+rarity*.75f*random.nextInt(18)),random);
        return result.copy();
    }
    public static ItemStack gear(ServerLevel level,int rarity,RandomSource random){
        int quality=random.nextInt(2);for(float chance:new float[]{.2f,.15f,.1f,.095f,.095f})if(random.nextFloat()<chance)quality++;
        var item=gearItem(random.nextInt(5),quality);if(item==Items.AIR)return ItemStack.EMPTY;
        var result=new ItemStack(item);result.setDamageValue(random.nextInt(1+result.getMaxDamage()/6));if(random.nextInt(4)<rarity)result=enchant(level,result,(int)(5+rarity*.75f*random.nextInt(18)),random);return result.copy();
    }
    public static Item gearItem(int slot,int quality){if(slot<0||slot>4||quality<0||quality>6)return Items.AIR;String kind=new String[]{"sword","boots","leggings","chestplate","helmet"}[slot];if(quality==4||quality==6){String id=(quality==4?"thaumium":"void")+"_"+kind;return slot==0?R3Equipment.TOOLS.get(id).get():R3Equipment.ARMOR.get(id).get();}String material=new String[]{slot==0?"wooden":"leather",slot==0?"stone":"chainmail","iron","golden","","diamond"}[quality];return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(material+"_"+kind));}
    private static ItemStack enchant(ServerLevel level,ItemStack stack,int value,RandomSource random){return EnchantmentHelper.enchantItem(random,stack,value,level.registryAccess(),level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_RANDOM_LOOT));}
    private LegacyLoot(){}
}

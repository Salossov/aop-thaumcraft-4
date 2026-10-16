package thaumcraft.entities;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.enchantment.*;
import thaumcraft.registry.*;

/** Each entry has the original tier; duplicates deliberately preserve their weighting. */
public final class PechTrades {
    public record Offer(int tier,ItemStack stack){}
    public static List<Offer> inventory(Level level,int variant){var offers=new ArrayList<Offer>();add(offers,1,R5Content.MANA_BEAN.get());switch(variant){case 0->{for(var metal:List.of("iron","gold","cinnabar"))add(offers,1,R3Equipment.NATIVE_CLUSTERS.get(metal).get());for(var metal:List.of("copper","tin","silver","lead"))if(thaumcraft.crafting.TagOutput.present(level.registryAccess(),"c:ingots/"+metal))add(offers,1,R3Equipment.NATIVE_CLUSTERS.get(metal).get());add(offers,2,Items.BLAZE_ROD);add(offers,2,R3Content.SHIMMERLEAF.get().asItem());potion(offers,2,Potions.STRENGTH);potion(offers,2,Potions.SWIFTNESS);add(offers,3,Items.EXPERIENCE_BOTTLE);add(offers,3,R3Equipment.KNOWLEDGE_FRAGMENT.get());add(offers,3,Items.GOLDEN_APPLE);potion(offers,3,Potions.LONG_STRENGTH);potion(offers,3,Potions.LONG_NIGHT_VISION);add(offers,5,Items.ENCHANTED_GOLDEN_APPLE);add(offers,4,R3Equipment.TOOLS.get("thaumium_pickaxe").get());add(offers,5,R3Content.SILVERWOOD_SAPLING.get().asItem());add(offers,5,R3Content.SILVERWOOD_SAPLING.get().asItem());}case 1->{for(var shard:R2Content.SHARDS)add(offers,1,shard.get());add(offers,1,R3Equipment.KNOWLEDGE_FRAGMENT.get());add(offers,2,R3Equipment.KNOWLEDGE_FRAGMENT.get());potion(offers,2,Potions.REGENERATION);potion(offers,2,Potions.HEALING);book(offers,level,3,"thaumcraft:haste");add(offers,3,Items.GOLDEN_APPLE);potion(offers,3,Potions.STRONG_REGENERATION);potion(offers,3,Potions.STRONG_HEALING);for(var crystal:R3Content.CRYSTAL_CLUSTERS)add(offers,4,crystal.get().asItem());add(offers,5,Items.ENCHANTED_GOLDEN_APPLE);book(offers,level,5,"thaumcraft:repair");add(offers,5,R3Equipment.FOCUS_POUCH.get());add(offers,5,R5Content.PECH_FOCUS.get());add(offers,5,R3Equipment.CURIOS.get("vis_amulet_lesser").get());}case 2->{for(int i=0;i<15;i++)add(offers,1,R4Content.CANDLES.get(i).get().asItem());add(offers,2,Items.GHAST_TEAR);potion(offers,2,Potions.SWIFTNESS);potion(offers,2,Potions.STRENGTH);book(offers,level,2,"minecraft:power");add(offers,3,Items.EXPERIENCE_BOTTLE);add(offers,3,R3Equipment.KNOWLEDGE_FRAGMENT.get());potion(offers,3,Potions.LONG_INVISIBILITY);potion(offers,3,Potions.STRONG_REGENERATION);add(offers,3,Items.GOLDEN_APPLE);add(offers,5,Items.ENCHANTED_GOLDEN_APPLE);add(offers,4,R3Equipment.ARMOR.get("thaumium_boots").get());add(offers,5,R3Equipment.CURIOS.get("runic_ring_minor").get());book(offers,level,5,"minecraft:infinity");book(offers,level,5,"minecraft:flame");}default->throw new IllegalArgumentException("Invalid Pech variant");}return offers;}
    private static void add(List<Offer> offers,int tier,Item item){offers.add(new Offer(tier,new ItemStack(item)));}
    private static void potion(List<Offer> offers,int tier,net.minecraft.core.Holder<Potion> potion){offers.add(new Offer(tier,PotionContents.createItemStack(Items.POTION,potion)));}
    private static void book(List<Offer> offers,Level level,int tier,String id){var enchant=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.parse(id)));var stack=new ItemStack(Items.ENCHANTED_BOOK);stack.enchant(enchant,1);offers.add(new Offer(tier,stack));}
    private PechTrades(){}
}

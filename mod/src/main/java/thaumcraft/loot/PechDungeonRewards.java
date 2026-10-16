package thaumcraft.loot;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import thaumcraft.api.aspects.*;
import thaumcraft.items.WandVis;

/** Original uniform entry sampling, source weight/count rejection and the fifty-draw limit. */
public final class PechDungeonRewards {
    public record Entry(String item,int weight,int max,boolean charged,boolean enchanted){}
    private static final List<Entry> ENTRIES=read();
    private static ItemStack chargedAmulet;
    private static List<Entry> read(){try(var stream=PechDungeonRewards.class.getResourceAsStream("/data/thaumcraft/pech_dungeon.json")){
        if(stream==null)throw new IllegalStateException("Missing original Pech dungeon entries");
        var rows=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("entries");var result=new ArrayList<Entry>();
        for(var value:rows){var row=value.getAsJsonObject();result.add(new Entry(row.get("item").getAsString(),row.get("weight").getAsInt(),row.get("max").getAsInt(),row.has("charged"),row.has("enchanted")));}
        return List.copyOf(result);
    }catch(IOException e){throw new UncheckedIOException(e);}}
    public static List<Entry> entries(){return ENTRIES;}
    public static ItemStack generate(ServerLevel level,Player player){
        if(thaumcraft.config.ThaumcraftConfig.MODERN_PECH_DUNGEON.get()){
            var table=level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse("minecraft:chests/simple_dungeon")));
            var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,player.position()).withLuck(player.getLuck()).create(LootContextParamSets.CHEST);
            var items=table.getRandomItems(params);if(items.isEmpty())return ItemStack.EMPTY;
            var result=items.get(level.random.nextInt(items.size())).copyWithCount(1);result.getItem().onCraftedBy(result,level,player);return result;
        }
        // ChestGenHooks.getItems generated the level-30 book before selecting an entry.
        var book=EnchantmentHelper.enchantItem(level.random,new ItemStack(Items.BOOK),30,level.registryAccess(),level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_RANDOM_LOOT));
        Entry entry=null;
        for(int attempt=0;attempt<50;attempt++){entry=ENTRIES.get(level.random.nextInt(ENTRIES.size()));if(entry.weight()<=5&&entry.max()<=1)break;}
        ItemStack result;
        if(entry.enchanted())result=book;
        else if(entry.charged()){
            if(chargedAmulet==null){chargedAmulet=LegacyLoot.stack(entry.item(),1);var random=net.minecraft.util.RandomSource.create();for(var aspect:List.of(Aspects.AIR,Aspects.EARTH,Aspects.FIRE,Aspects.WATER,Aspects.ORDER,Aspects.ENTROPY))WandVis.set(chargedAmulet,aspect,random.nextInt(5)*100);}
            result=chargedAmulet.copy();
        }else result=LegacyLoot.stack(entry.item(),1);
        result.getItem().onCraftedBy(result,level,player);return result;
    }
    private PechDungeonRewards(){}
}

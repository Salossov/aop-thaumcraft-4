package thaumcraft.verification;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import thaumcraft.data.ThaumcraftData;

final class PhotoReviewVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        queue(check);fractionalCraft(level,check);
        var pech=(thaumcraft.entities.Pech)BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:pech")).create(level);var keys=new HashSet<String>();
        for(int variant=0;variant<3;variant++){pech.variant(variant);var tags=ThaumcraftData.aspects(level.getServer(),pech);check.accept(tags.getOrDefault("praecantatio",0)==(variant==2?4:2)&&tags.getOrDefault(variant==0?"lucrum":variant==1?"telum":"permutatio",0)==2,"native byte-NBT Pech variant "+variant+" retains its TC4 aspects");keys.add(ThaumcraftData.entityScanKey(level.getServer(),pech));}
        check.accept(keys.size()==3,"Pech byte variants have three distinct scanner keys");
        var server=level.getServer();var data=ThaumcraftData.get(server);var report=new LinkedHashMap<String,Object>();
        var items=new TreeMap<String,Object>();
        for(var item:BuiltInRegistries.ITEM){var id=BuiltInRegistries.ITEM.getKey(item);if(!Set.of("minecraft","thaumcraft").contains(id.getNamespace()))continue;var stack=new ItemStack(item);if(stack.isEmpty())continue;items.put(id.toString(),ThaumcraftData.aspects(server,stack));}
        var blocks=new TreeMap<String,Object>();
        for(var block:BuiltInRegistries.BLOCK){var id=BuiltInRegistries.BLOCK.getKey(block);if(!Set.of("minecraft","thaumcraft").contains(id.getNamespace()))continue;blocks.put(id.toString(),ThaumcraftData.aspects(server,block.defaultBlockState()));}
        var entities=new TreeMap<String,Object>();
        for(var type:BuiltInRegistries.ENTITY_TYPE){var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);if(!Set.of("minecraft","thaumcraft").contains(id.getNamespace())||id.getPath().equals("player"))continue;try{var entity=type.create(level);if(entity!=null)entities.put(id.toString(),Map.of("class",entity.getClass().getName(),"mob",entity instanceof Mob,"aspects",ThaumcraftData.aspects(server,entity)));}catch(Exception ignored){}}
        var recipes=new ArrayList<Object>();
        for(var holder:server.getRecipeManager().getRecipes()){var recipe=holder.value();var out=recipe.getResultItem(level.registryAccess());if(out.isEmpty())continue;var input=new ArrayList<Object>();for(var ingredient:recipe.getIngredients())input.add(Arrays.stream(ingredient.getItems()).map(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).toList());recipes.add(Map.of("id",holder.id().toString(),"type",recipe.getClass().getName(),"output",BuiltInRegistries.ITEM.getKey(out.getItem()).toString(),"count",out.getCount(),"inputs",input));}
        report.put("items",items);report.put("blocks",blocks);report.put("entities",entities);report.put("recipes",recipes);report.put("resolved_objects",data.objects());
        var path=Path.of(System.getProperty("thaumcraft.r3.root"),".cache/photo-fixes-2026-10-16/aspect-audit.json");Files.createDirectories(path.getParent());Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(report));
        check.accept(!items.isEmpty()&&!entities.isEmpty(),"native registry and resolved aspect audit exported");
    }
    static void queue(BiConsumer<Boolean,String> check){
        var q=new thaumcraft.client.ResearchNotificationQueue<String>(3,6000);for(int i=0;i<6;i++)q.add("row"+i,100);
        check.accept(q.dismiss(1,200)&&q.rows().stream().map(r->r.value()).toList().equals(List.of("row0","row2","row3"))&&q.remaining()==2,"middle notification dismissal retains FIFO and fills exactly one pending row");
        check.accept(!q.dismiss(-1,200)&&!q.dismiss(3,200),"invalid notification indices have no effect");q.dismiss(0,250);q.advance(6249);check.accept(q.rows().getFirst().value().equals("row2"),"head dismissal restarts next row lifetime");q.advance(6250);check.accept(q.rows().getFirst().value().equals("row3"),"next notification expires at its own deadline");q.clear();check.accept(q.rows().isEmpty()&&q.remaining()==0&&q.shift(7000)==0,"clear removes queue animation and pending entries");
    }
    static void fractionalCraft(ServerLevel level,BiConsumer<Boolean,String> check){
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.fromString("240f5ba1-bae0-4bc0-ae50-2c72687e7659"),"PhotoVisQA"));thaumcraft.knowledge.KnowledgeService.initialize(player);player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new ItemStack(thaumcraft.registry.R3Equipment.ARMOR.get("void_robe_chestplate").get()));
        var recipe=(thaumcraft.crafting.ArcaneRecipe)level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("thaumcraft:wandcapgold")).orElseThrow().value();thaumcraft.knowledge.KnowledgeService.mutate(player,k->k.completeResearch(recipe.research()));
        var table=new net.minecraft.world.SimpleContainer(11);var w=PhotoLiveScene.wand();table.setItem(10,w);int width=recipe.pattern().width();for(int i=0;i<recipe.getIngredients().size();i++){var ingredient=recipe.getIngredients().get(i);if(!ingredient.isEmpty())table.setItem((i/width)*3+i%width,ingredient.getItems()[0].copy());}
        var cost=thaumcraft.items.WandVis.cost(w,recipe.vis(),player);check.accept(cost.values().stream().anyMatch(n->n%100!=0),"actual arcane recipe with robe discount costs fractional vis: "+cost);
        for(var entry:cost.entrySet())thaumcraft.items.WandVis.set(w,entry.getKey(),entry.getValue());var first=cost.entrySet().iterator().next();thaumcraft.items.WandVis.set(w,first.getKey(),first.getValue()-1);var menu=new thaumcraft.menu.ArcaneMenu(1,player.getInventory(),table);
        check.accept(menu.craft(player).isEmpty()&&thaumcraft.items.WandVis.get(w,first.getKey())==first.getValue()-1,"arcane table rejects a deficit of exactly 0.01 vis without spending");
        thaumcraft.items.WandVis.set(w,first.getKey(),first.getValue());var output=menu.craft(player);check.accept(!output.isEmpty()&&cost.keySet().stream().allMatch(a->thaumcraft.items.WandVis.get(w,a)==0),"arcane crafting spends exact centivis to zero without integer rounding");player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,ItemStack.EMPTY);
    }
    private PhotoReviewVerification(){}
}

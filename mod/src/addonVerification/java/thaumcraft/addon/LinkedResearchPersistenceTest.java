package thaumcraft.addon;

import com.google.gson.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.*;
import thaumcraft.data.*;
import thaumcraft.knowledge.WorldKnowledgeStore;

/** Loader transactions and wire/save compatibility, without launching a game. */
final class LinkedResearchPersistenceTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static JsonObject research(String key,String category,String icon,String linked){
        var json=new JsonObject();json.addProperty("kind","research");json.addProperty("key",key);json.addProperty("category",category);json.addProperty("icon",icon);
        if(!linked.isEmpty())json.addProperty("linked_research",linked);
        return json;
    }
    private static void rejects(Method method,Object listener,Map<ResourceLocation,JsonElement> data)throws Exception{
        try{method.invoke(listener,data,null,null);throw new AssertionError("Invalid linked reload accepted");}
        catch(InvocationTargetException expected){if(!(expected.getCause() instanceof IllegalArgumentException))throw expected;checks++;}
    }
    @SuppressWarnings("unchecked") static void run()throws Exception{
        var ctor=ThaumcraftData.class.getDeclaredConstructors()[0];ctor.setAccessible(true);var listener=ctor.newInstance(new Object[]{null});
        var apply=ThaumcraftData.class.getDeclaredMethod("apply",Map.class,net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);apply.setAccessible(true);
        var field=ThaumcraftData.class.getDeclaredField("SERVERS");field.setAccessible(true);var snapshots=(Map<Object,ThaumcraftData.Snapshot>)field.get(null);
        var bookField=ThaumcraftData.class.getDeclaredField("BOOKS");bookField.setAccessible(true);var books=(Map<Object,BookCatalog>)bookField.get(null);
        var valid=new LinkedHashMap<ResourceLocation,JsonElement>();
        var nativeNode=research("GOGGLES","BASICS","minecraft:diamond","");var text=new JsonArray();text.add("tc.native.page");nativeNode.add("text_pages",text);
        valid.put(ResourceLocation.parse("thaumcraft:goggles"),nativeNode);
        valid.put(ResourceLocation.parse("thaumcraft:eldritch"),research("ELDRITCHMINOR","ELDRITCH","minecraft:book",""));
        var alias=research("example:alias","example:links","minecraft:air","GOGGLES");alias.addProperty("hidden",true);alias.addProperty("stub",true);alias.addProperty("column",-3);alias.addProperty("row",2);
        valid.put(ResourceLocation.parse("example:alias"),alias);
        var category=new JsonObject();category.addProperty("kind","book_category");category.addProperty("id","example:links");category.addProperty("icon","minecraft:book");valid.put(ResourceLocation.parse("example:category"),category);
        apply.invoke(listener,valid,null,null);var original=snapshots.get(null);var originalBook=books.get(null);
        var resolved=ResearchCatalogRules.resolve(original.research());
        check(resolved.get("example:alias").icon().equals("minecraft:diamond")&&resolved.get("example:alias").textPages().equals(List.of("tc.native.page")),"JSON link delegates current native presentation");
        check(originalBook.categories().containsKey("example:links")&&resolved.get("GOGGLES").category().equals("BASICS"),"category registration retains native category");
        var wire=new CompoundTag();ResearchDataSync.encode(wire,resolved);var decoded=ResearchDataSync.decode(wire);
        check(decoded.equals(resolved)&&decoded.get("example:alias").linkedResearch().equals("GOGGLES"),"link and resolved pages round trip through actual client sync");
        check(new Gson().fromJson("{\"key\":\"OLD\",\"category\":\"BASICS\",\"parents\":[],\"aspects\":{},\"textPages\":[],\"recipePages\":[],\"siblings\":[]}",ResearchDefinition.class).linkedResearch().isEmpty(),"old wire record without optional field stays compatible");
        var invalid=new LinkedHashMap<>(valid);var missing=alias.deepCopy();missing.addProperty("linked_research","example:missing");invalid.put(ResourceLocation.parse("example:alias"),missing);
        rejects(apply,listener,invalid);check(snapshots.get(null)==original&&books.get(null)==originalBook,"missing target retains exact previous data and book catalogs");
        invalid=new LinkedHashMap<>(valid);var cycle=alias.deepCopy();cycle.addProperty("linked_research","example:alias");invalid.put(ResourceLocation.parse("example:alias"),cycle);
        rejects(apply,listener,invalid);check(snapshots.get(null)==original&&books.get(null)==originalBook,"link cycle reload retains exact previous catalogs");
        var updated=nativeNode.deepCopy();updated.addProperty("icon","minecraft:emerald");var newText=new JsonArray();newText.add("tc.updated.page");updated.add("text_pages",newText);valid.put(ResourceLocation.parse("thaumcraft:goggles"),updated);
        apply.invoke(listener,valid,null,null);var reloaded=ResearchCatalogRules.resolve(snapshots.get(null).research());
        check(reloaded.get("example:alias").icon().equals("minecraft:emerald")&&reloaded.get("example:alias").textPages().equals(List.of("tc.updated.page")),"valid later reload recomputes alias from new target rather than cached copied data");
        var directory=Path.of(".cache/addon-api-2026-10-19/linked-save-tests");Files.createDirectories(directory);var id=UUID.randomUUID();var old=new PlayerKnowledge();old.completeResearch("GOGGLES");old.completeResearch("removed:progress");old.setPool("aer",17);old.setWarp(3,4,5,12);
        try(var store=new WorldKnowledgeStore(directory)){store.commit(id,old);}
        try(var store=new WorldKnowledgeStore(directory)){var loaded=store.get(id).copy();check(ResearchLinks.completeAliases(loaded,reloaded),"old saved native completion reconciles on first load");store.commit(id,loaded);}
        try(var store=new WorldKnowledgeStore(directory)){var loaded=store.get(id);check(loaded.completedResearch().containsAll(List.of("GOGGLES","example:alias","removed:progress"))&&loaded.totalWarp()==12&&loaded.aspectPools().equals(old.aspectPools()),"alias persists without losing native/unknown progress or adding warp/point costs");}
        System.out.println("TC4_LINKED_RESEARCH_PERSISTENCE_PASS checks="+checks);
    }
}

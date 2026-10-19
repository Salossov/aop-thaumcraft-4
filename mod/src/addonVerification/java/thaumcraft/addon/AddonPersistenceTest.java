package thaumcraft.addon;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.io.*;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.*;
/** Data/NBT checks only: no Minecraft server or client is started. */
public final class AddonPersistenceTest {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static void rejects(Method method,Object target,Object...args)throws Exception{try{method.invoke(target,args);throw new AssertionError("Invalid reload accepted");}catch(InvocationTargetException e){if(!(e.getCause() instanceof IllegalArgumentException))throw e;checks++;}}
 private static JsonElement research(String key,String parent){return JsonParser.parseString("{\"kind\":\"research\",\"key\":\""+key+"\",\"category\":\"BASICS\",\"parents\":"+(parent.isEmpty()?"[]":"[\""+parent+"\"]")+",\"auto_unlock\":true,\"icon\":\"minecraft:diamond\"}");}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
  var directory=Path.of(".cache/addon-api-2026-10-17/persistence-tests");Files.createDirectories(directory);var uuid=UUID.randomUUID();var state=new PlayerKnowledge();state.completeResearch("GOGGLES");state.completeResearch("ROD_greatwood");state.completeResearch("example:removed_research");state.setPool("aer",41);state.setPool("example:aspect",7);state.setWarp(3,4,5,12);state.revision(81);state.recordScan(PlayerKnowledge.ScanKind.OBJECT,"example:device");
  try(var store=new WorldKnowledgeStore(directory)){store.commit(uuid,state);store.flush();}
  try(var store=new WorldKnowledgeStore(directory)){var loaded=store.get(uuid);check(loaded.completedResearch().equals(state.completedResearch()),"NBT research identities survive save/reload");check(loaded.aspectPools().equals(state.aspectPools()),"old and namespaced aspect pools survive");check(loaded.scanned(PlayerKnowledge.ScanKind.OBJECT).contains("example:device"),"scans survive");check(loaded.revision()==81&&loaded.totalWarp()==12,"revision and warp survive");}
  var ctor=ThaumcraftData.class.getDeclaredConstructors()[0];ctor.setAccessible(true);var listener=(ThaumcraftData)ctor.newInstance(new Object[]{null});var prepare=ThaumcraftData.class.getDeclaredMethod("prepare",ResourceManager.class,ProfilerFiller.class);prepare.setAccessible(true);var apply=ThaumcraftData.class.getDeclaredMethod("apply",Map.class,ResourceManager.class,ProfilerFiller.class);apply.setAccessible(true);
  var field=ThaumcraftData.class.getDeclaredField("SERVERS");field.setAccessible(true);var snapshots=(Map<Object,ThaumcraftData.Snapshot>)field.get(null);var booksField=ThaumcraftData.class.getDeclaredField("BOOKS");booksField.setAccessible(true);var books=(Map<Object,BookCatalog>)booksField.get(null);
  var valid=new LinkedHashMap<ResourceLocation,JsonElement>();valid.put(ResourceLocation.parse("thaumcraft:goggles"),research("GOGGLES",""));valid.put(ResourceLocation.parse("thaumcraft:eldritch"),research("ELDRITCHMINOR",""));apply.invoke(listener,valid,null,null);var original=snapshots.get(null);var originalBook=books.get(null);check(original.research().containsKey("GOGGLES"),"valid reload installed");
  var invalid=new LinkedHashMap<>(valid);invalid.put(ResourceLocation.parse("example:bad"),research("example:bad","example:missing"));rejects(apply,listener,invalid,null,null);check(snapshots.get(null)==original&&books.get(null)==originalBook,"invalid references retain exact previous server catalog");
  invalid=new LinkedHashMap<>(valid);invalid.put(ResourceLocation.parse("example:page"),JsonParser.parseString("{\"kind\":\"book_page\",\"id\":\"example:page\",\"type\":\"example:unregistered\"}"));rejects(apply,listener,invalid,null,null);check(snapshots.get(null)==original&&books.get(null)==originalBook,"invalid page type retains previous catalog");
  invalid=new LinkedHashMap<>(valid);invalid.put(ResourceLocation.parse("example:bad"),research("example:bad","example:other"));invalid.put(ResourceLocation.parse("example:other"),research("example:other","example:bad"));rejects(apply,listener,invalid,null,null);check(snapshots.get(null)==original,"cycle retains previous catalog");
  var resources=new LinkedHashMap<ResourceLocation,Resource>();resources.put(ResourceLocation.parse("example:thaumcraft/broken.json"),new Resource(null,()->new ByteArrayInputStream("{invalid".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
  var manager=(ResourceManager)Proxy.newProxyInstance(AddonPersistenceTest.class.getClassLoader(),new Class[]{ResourceManager.class},(proxy,method,arguments)->{if(method.getName().equals("listResources"))return resources;throw new UnsupportedOperationException(method.getName());});rejects(prepare,listener,manager,null);check(snapshots.get(null)==original&&books.get(null)==originalBook,"malformed JSON retains previous catalog");
  valid.put(ResourceLocation.parse("example:new"),research("example:new","GOGGLES"));apply.invoke(listener,valid,null,null);check(snapshots.get(null)!=original&&snapshots.get(null).research().containsKey("example:new"),"next valid reload replaces catalog");
  try(var store=new WorldKnowledgeStore(directory)){check(store.get(uuid).completedResearch().contains("example:removed_research"),"reload never drops unknown saved progress");}
  LinkedResearchPersistenceTest.run();
  System.out.println("TC4_ADDON_PERSISTENCE_PASS checks="+checks);
 }
}

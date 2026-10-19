package thaumcraft.api;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import com.google.gson.*;
import thaumcraft.api.addon.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.book.*;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.*;
import thaumcraft.api.wands.*;
import thaumcraft.api.crafting.TransferPlanner;
public final class AddonRulesTest {
 private static int checks;
 private static void check(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
 private static void rejects(Runnable action,String label){try{action.run();throw new AssertionError(label);}catch(IllegalArgumentException|IllegalStateException|ArithmeticException expected){checks++;}}
 private record Stack(String item,String component,int n,int limit){}
 private static final Stack EMPTY=new Stack("","",0,64);
 private static final TransferPlanner.Stacks<Stack> STACKS=new TransferPlanner.Stacks<>(){
  public boolean empty(Stack s){return s.n==0;}public int count(Stack s){return s.n;}public int limit(Stack s){return s.limit;}public boolean same(Stack a,Stack b){return a.item.equals(b.item)&&a.component.equals(b.component);}public Stack count(Stack s,int n){return new Stack(s.item,s.component,n,s.limit);}public Stack empty(){return EMPTY;}
 };
 private static Stack stack(String item,int n){return new Stack(item,"",n,64);}
 private static Map<String,Integer> total(List<Stack> a,List<Stack> b){var m=new HashMap<String,Integer>();var all=new ArrayList<>(a);all.addAll(b);for(var s:all)if(s.n>0)m.merge(s.item+"/"+s.component,s.n,Integer::sum);return m;}
 private static ResearchDefinition research(String key,List<String> parents,List<String> pages){return new ResearchDefinition(key,"tests:tab",parents,Map.of(),0,true,false,0,0,pages,List.of(),"minecraft:diamond");}
 public static void main(String[] args){
  var startupCalls=new java.util.concurrent.atomic.AtomicInteger();AddonRegistrationEvent.BUS.addListener((AddonRegistrationEvent event)->startupCalls.incrementAndGet());AddonRegistrationEvent.BUS.post(new AddonRegistrationEvent());check(startupCalls.get()==1,"dedicated startup bus delivers events before game bus startup");
  var key=ResourceLocation.parse("tests:resonance");var aspect=Aspect.registerCompound(key,0xabcdef,ResourceLocation.parse("tests:textures/aspect.png"),"aspect.tests.resonance",Aspects.AURA,Aspects.CRYSTAL);
  check(Aspect.getAspect(key.toString())==aspect,"namespaced aspect");check(Aspect.getAspect("thaumcraft:aer")==Aspects.AIR,"legacy alias");check(Aspects.PRIMALS.size()==6,"six primals retained");check(aspect.getComponents().equals(List.of(Aspects.AURA,Aspects.CRYSTAL)),"compound components");check(aspect.texture().equals("tests:textures/aspect.png"),"custom icon");rejects(()->Aspect.registerCompound(key,1,key,"test",Aspects.AIR,Aspects.EARTH),"aspect collision");
  check(WandApi.centivis(1)==100&&WandApi.centivis(25)==2500,"centivis units");rejects(()->WandApi.centivis(Integer.MAX_VALUE),"vis overflow");
  var category=new BookCategory("tests:tab","Test","minecraft:textures/item/diamond.png","thaumcraft:textures/gui/gui_researchback.png",100,"");AddonRegistry.category(category);rejects(()->AddonRegistry.category(category),"category collision");rejects(()->AddonRegistry.id("unnamespaced"),"namespace required");
  var json=JsonParser.parseString("{\"elements\":[{\"kind\":\"text\",\"value\":\"text\"},{\"kind\":\"item\",\"value\":\"minecraft:diamond\",\"x\":10,\"y\":20,\"width\":16,\"height\":16}]}").getAsJsonObject();var page=BookPage.read("tests:page",json);AddonRegistry.page(page);check(page.elements().get(0).y()==-1&&page.elements().get(1).x()==10,"flow and positioned coordinates");rejects(()->AddonRegistry.page(page),"page collision");
  var root=research("tests:root",List.of(),List.of("tests:page"));var child=research("tests:child",List.of(root.key()),List.of());var definitions=Map.of(root.key(),root,child.key(),child);var books=new BookCatalog(Map.of(category.id(),category),Map.of(page.id(),page));CatalogValidator.validate(definitions,books);check(true,"valid catalog");rejects(()->CatalogValidator.validate(Map.of(child.key(),child),books),"missing parent");rejects(()->CatalogValidator.validate(Map.of(root.key(),research(root.key(),List.of(child.key()),List.of()),child.key(),child),books),"parent cycle");rejects(()->CatalogValidator.validate(Map.of(root.key(),research(root.key(),List.of(),List.of("tests:missing"))),new BookCatalog(Map.of(category.id(),category),Map.of())),"missing page");
  var data=new JsonObject();data.addProperty("v",1);var custom=new BookPage("tests:custom","tests:custom",List.of(),data);rejects(()->PageTypes.validate(custom),"unknown page type");PageTypes.register(ResourceLocation.parse("tests:custom"),v->{if(v.get("v").getAsInt()!=1)throw new IllegalArgumentException("bad v");});PageTypes.validate(custom);data.addProperty("v",2);check(custom.data().get("v").getAsInt()==1,"page data defensive copy");custom.data().addProperty("v",3);check(custom.data().get("v").getAsInt()==1,"page data accessor immutable");
  var knowledge=new PlayerKnowledge();knowledge.completeResearch("GOGGLES");knowledge.completeResearch("ROD_greatwood");knowledge.completeResearch("FOCUSFIRE");knowledge.completeResearch("tests:child");knowledge.setPool("aer",37);knowledge.discover(aspect);knowledge.setPool(aspect.getTag(),4);ResearchApi.accept(definitions,books,knowledge);var copy=ResearchApi.clientKnowledge();copy.forgetResearch("GOGGLES");check(ResearchApi.clientKnowledge().completedResearch().containsAll(List.of("GOGGLES","ROD_greatwood","FOCUSFIRE","tests:child")),"old progress keys preserved");check(ResearchApi.clientKnowledge().pool(Aspects.AIR)==37,"old aspect pool preserved");check(ResearchApi.clientKnowledge().pool(aspect)==4,"addon aspect pool preserved");
  var snapshot=ResearchApi.books();try{var bad=new BookCatalog(Map.of(),Map.of());CatalogValidator.validate(definitions,bad);ResearchApi.accept(definitions,bad,knowledge);}catch(IllegalArgumentException expected){}check(ResearchApi.books()==snapshot,"invalid catalog retains previous snapshot");
  List<Predicate<Stack>> tests=new ArrayList<>(Collections.nCopies(9,null));tests.set(0,s->s.item.equals("a"));tests.set(1,s->s.item.equals("a")||s.item.equals("b"));
  var inv=List.of(stack("a",1),stack("b",1),EMPTY);var grid=new ArrayList<Stack>(Collections.nCopies(9,EMPTY));var plan=TransferPlanner.plan(inv,grid,tests,STACKS,false).orElseThrow();check(plan.grid().get(0).item.equals("a")&&plan.grid().get(1).item.equals("b"),"overlapping ingredients matched without greed");check(total(inv,grid).equals(total(plan.inventory(),plan.grid())),"fill conserves items");
  var full=List.of(stack("a",64));grid.set(8,stack("c",64));var before=List.copyOf(grid);check(TransferPlanner.plan(full,grid,tests,STACKS,false).isEmpty(),"full inventory failure");check(grid.equals(before)&&full.get(0).n==64,"failure leaves originals untouched");
  var random=new Random(420231);for(int trial=0;trial<2000;trial++){var inventory=new ArrayList<Stack>();for(int n=0;n<12;n++)inventory.add(random.nextBoolean()?EMPTY:new Stack(""+(char)('a'+random.nextInt(4)),""+random.nextInt(2),1+random.nextInt(64),64));var table=new ArrayList<Stack>();var requirements=new ArrayList<Predicate<Stack>>();for(int n=0;n<9;n++){table.add(random.nextInt(4)==0?stack("d",1+random.nextInt(64)):EMPTY);String item=""+(char)('a'+random.nextInt(3));requirements.add(random.nextBoolean()?null:s->s.item.equals(item));}var oldInventory=List.copyOf(inventory);var oldTable=List.copyOf(table);var result=TransferPlanner.plan(inventory,table,requirements,STACKS,random.nextBoolean());check(inventory.equals(oldInventory)&&table.equals(oldTable),"planner is pure");if(result.isPresent()){var r=result.get();check(total(inventory,table).equals(total(r.inventory(),r.grid())),"randomized item/component conservation");for(int n=0;n<9;n++)check(requirements.get(n)==null?r.grid().get(n).n==0:requirements.get(n).test(r.grid().get(n))&&r.grid().get(n).n==r.crafts(),"randomized grid matches");for(var s:r.inventory())check(s.n>=0&&s.n<=s.limit,"stack bounds");}}
  var conditionEnabled=new java.util.concurrent.atomic.AtomicBoolean(false);
  var conditionalDefinition=new ResearchDefinition("tests:conditional",category.id(),List.of(),Map.of(),0,false,false,0,0,List.of(),List.of(),"");
  AddonRegistry.research(conditionalDefinition,conditionEnabled::get);
  check(AddonRegistry.research().containsKey("tests:conditional")&&AddonRegistry.disabledResearch().contains("tests:conditional"),"conditional research keeps startup identity while disabled");
  conditionEnabled.set(true);check(!AddonRegistry.disabledResearch().contains("tests:conditional"),"catalog condition is reevaluated after config change");
  AddonRegistry.freeze();rejects(()->AddonRegistry.category(new BookCategory("tests:late","Late",category.icon(),category.background(),2,"")),"registration closes");System.out.println("TC4_ADDON_API_PASS checks="+checks);
 }
}

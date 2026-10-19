package thaumcraft.viewers;

import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.api.aspects.*;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.crafting.RecipeDisplay;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.ResearchApi;

/** Uses vanilla data registries only, without starting a game or loading either viewer. */
public final class PresentationTest {
 private static int checks;
 private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 private static RecipeDisplay recipe(String id,RecipeDisplay.Kind kind,List<Ingredient> inputs,List<ItemStack> catalysts,Map<String,Integer> outputs){return new RecipeDisplay(ResourceLocation.parse("test:"+id),null,kind,List.of(),inputs,List.of(),Map.of(),outputs,Map.of(),0,0,catalysts,List.of());}
 private static thaumcraft.api.research.ResearchDefinition definition(String key,List<String> parents,boolean hidden){return new thaumcraft.api.research.ResearchDefinition(key,"BASICS",parents,Map.of(),1,false,false,0,0,List.of(),List.of(),"minecraft:book",false,false,false,false,List.of(),List.of(),hidden,0,List.of(),List.of(),List.of(),List.of(),Map.of());}
 public static void main(String[] args){
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),new ArrayList<>(),Map.of());
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
  check(AspectValue.countLabel(524288).equals("524k")&&AspectValue.countLabel(2147483647).length()<=4,"Large aspect counts fit slot labels");
  var state=new PlayerKnowledge();ResearchApi.accept(Map.of(),BookCatalog.EMPTY,state);
  var hidden=new AspectValue(Aspects.MAGIC.getTag(),false,1);
  check(!hidden.discovered(),"Compound starts unknown");
  check(((TranslatableContents)hidden.name().getContents()).getKey().equals("thaumcraft_viewers.unknown_aspect"),"Unknown name is masked");
  check(hidden.tooltip().size()==3,"Unknown tooltip exposes neither components nor scanned sources");
  check(AspectRecipes.sources(hidden.aspect()).isEmpty(),"Unknown aspect cannot expose sources");
  var values=AspectRecipes.values();check(values.size()==Aspect.all().size(),"Exactly one sidebar entry per aspect");
  check(values.stream().map(AspectValue::key).distinct().count()==values.size(),"No vis/essentia duplicates");
  var vis=new AspectValue("aer",true,150);var essence=new AspectValue("aer",false,2);
  check(vis.id().equals(essence.id())&&vis.key().equals(essence.key()),"Vis and essentia share focus identity");
  check(!vis.amountText().equals(essence.amountText()),"Shared identity retains distinct amount units");
  check(AspectPages.slots(new AspectRecipes.Entry(AspectRecipes.Kind.INDEX,"",0)).size()==values.size(),"JEI/EMI index includes masked aspects");
  for(var slot:AspectPages.slots(new AspectRecipes.Entry(AspectRecipes.Kind.INDEX,"",0)))check(slot.x()>=0&&slot.x()+16<=Layout.WIDTH,"Index fits category width");
  state.discover(Aspects.MAGIC);ResearchApi.accept(Map.of(),BookCatalog.EMPTY,state);
  check(hidden.discovered()&&state.pool(Aspects.MAGIC)==0,"Discovery remains visible at zero pool");
  check(hidden.name().getString().equals(Aspects.MAGIC.getName()),"Discovered aspect name appears");
  var combination=AspectPages.slots(new AspectRecipes.Entry(AspectRecipes.Kind.COMBINATION,hidden.aspect(),0));
  check(combination.size()==3&&combination.stream().filter(AspectPages.Slot::output).count()==1,"Compound has two parents and one output");
  check(combination.stream().filter(s->!s.output()).noneMatch(s->s.aspect().discovered()),"Undiscovered parents remain masked");

  var cauldron=new ItemStack(Items.CAULDRON);var table=new ItemStack(Items.CRAFTING_TABLE);
  var recipes=new ArrayList<RecipeDisplay>();
  for(int i=0;i<500;i++)recipes.add(recipe("crucible_"+i,RecipeDisplay.Kind.CRUCIBLE,List.of(),List.of(cauldron.copyWithCount(1+i%16)),Map.of()));
  recipes.add(recipe("arcane",RecipeDisplay.Kind.ARCANE,List.of(),List.of(table,cauldron),Map.of()));
  var stations=Workstations.collect(recipes);
  check(stations.get(RecipeDisplay.Kind.CRUCIBLE).size()==1,"500 recipes register a single JEI catalyst");
  check(stations.get(RecipeDisplay.Kind.ARCANE).size()==2,"Different workstations are retained per category");
  check(stations.values().stream().flatMap(List::stream).allMatch(s->s.getCount()==1),"Catalyst counts normalized");

  var sources=List.of(recipe("coal",RecipeDisplay.Kind.DISTILLATION,List.of(Ingredient.of(Items.COAL)),List.of(),Map.of("ignis",4)),recipe("duplicate_coal",RecipeDisplay.Kind.DISTILLATION,List.of(Ingredient.of(Items.COAL)),List.of(),Map.of("ignis",4)),recipe("blaze",RecipeDisplay.Kind.DISTILLATION,List.of(Ingredient.of(Items.BLAZE_ROD)),List.of(),Map.of("ignis",8)),recipe("dirt",RecipeDisplay.Kind.DISTILLATION,List.of(Ingredient.of(Items.DIRT)),List.of(),Map.of("terra",1)));
  var scanned=AspectRecipes.collectSources("ignis",sources,s->s.is(Items.COAL));
  check(scanned.size()==1&&scanned.getFirst().stack().is(Items.COAL)&&scanned.getFirst().amount()==4,"Only scanned matching sources shown, duplicates removed");
  check(AspectRecipes.collectSources("ignis",sources,s->false).isEmpty(),"No unscanned source leaks");
  check(AspectRecipes.collectSources("ignis",sources,s->true).getFirst().stack().is(Items.BLAZE_ROD),"Sources sorted by yield");

  var infusion=recipe("infusion",RecipeDisplay.Kind.INFUSION,List.of(Ingredient.of(Items.DIAMOND),Ingredient.of(Items.COAL),Ingredient.of(Items.COAL),Ingredient.of(Items.BLAZE_ROD)),List.of(),Map.of());
  var ritual=new Layout(infusion);
  check(ritual.items.size()==3,"Repeated infusion ingredients grouped visually");
  check(ritual.items.stream().filter(i->i.count()==2).count()==1,"Grouped item quantity retained");
  check(ritual.items.getFirst().x()==77&&ritual.items.getFirst().y()==72,"Central infusion item stays in center");
  check(infusion.inputs().size()==4,"Display grouping never changes actual recipe requirements");
  for(int count=0;count<40;count++){
   var inputs=new ArrayList<Ingredient>();for(int i=0;i<count;i++)inputs.add(Ingredient.of(Items.DIRT));
   var l=new Layout(recipe("bounds_"+count,RecipeDisplay.Kind.DISTILLATION,inputs,List.of(),Map.of("terra",1)));
   for(var item:l.items)check(item.x()>=0&&item.x()+16<=Layout.WIDTH&&item.y()+16<l.height,"Multi-input rows remain in bounds");
   for(var cost:l.essence)check(cost.x()>=0&&cost.x()+16<=Layout.WIDTH&&cost.y()+16<l.height,"Aspect amounts remain in bounds");
  }
  check(!AspectRecipes.id(new AspectRecipes.Entry(AspectRecipes.Kind.SOURCES,"aer",0,1)).equals(AspectRecipes.id(new AspectRecipes.Entry(AspectRecipes.Kind.SOURCES,"aer",0,2))),"Changed source revision gets a new JEI index identity");
  var distinct=List.of(Items.COAL,Items.BLAZE_ROD,Items.DIRT,Items.STICK,Items.APPLE,Items.IRON_INGOT,Items.GOLD_INGOT,Items.EMERALD,Items.BOOK,Items.GLASS,Items.DIAMOND,Items.SAND);
  for(int count=1;count<=distinct.size();count++){var ingredients=new ArrayList<Ingredient>();ingredients.add(Ingredient.of(Items.STONE));for(int i=0;i<count;i++)ingredients.add(Ingredient.of(distinct.get(i)));var l=new Layout(recipe("ritual_geometry_"+count,RecipeDisplay.Kind.INFUSION,ingredients,List.of(),Map.of()));for(int i=0;i<l.items.size();i++){var a=l.items.get(i);check(a.y()+16<l.instabilityY,"Infusion components clear instability text");for(int j=i+1;j<l.items.size();j++){var b=l.items.get(j);check(Math.abs(a.x()-b.x())>=16||Math.abs(a.y()-b.y())>=16,"Infusion ingredient hitboxes do not overlap");}}}
  var root=definition("root",List.of(),false);var child=definition("child",List.of("root"),false);var finalNode=definition("final",List.of("child"),false);var hiddenNode=definition("trigger",List.of("root"),true);
  var definitions=Map.of("root",root,"child",child,"final",finalNode,"trigger",hiddenNode);var knowledge=new PlayerKnowledge();
  var nav=thaumcraft.api.research.ResearchNavigation.resolve("final",definitions,BookCatalog.EMPTY,knowledge);
  check(nav.focus().equals("root")&&nav.reason()==thaumcraft.api.research.ResearchNavigation.Reason.PREREQUISITES,"Deep link focuses nearest available prerequisite");
  knowledge.completeResearch("root");nav=thaumcraft.api.research.ResearchNavigation.resolve("final",definitions,BookCatalog.EMPTY,knowledge);check(nav.focus().equals("child"),"Navigation follows current completed progress");
  nav=thaumcraft.api.research.ResearchNavigation.resolve("trigger",definitions,BookCatalog.EMPTY,knowledge);check(nav.reason()==thaumcraft.api.research.ResearchNavigation.Reason.UNDISCOVERED&&nav.focus().isEmpty(),"Trigger-locked research is not exposed or opened");
  knowledge.completeResearch("@trigger");nav=thaumcraft.api.research.ResearchNavigation.resolve("trigger",definitions,BookCatalog.EMPTY,knowledge);check(nav.reason()==thaumcraft.api.research.ResearchNavigation.Reason.AVAILABLE&&nav.focus().equals("trigger"),"Triggered research can be focused");
  knowledge.completeResearch("trigger");check(thaumcraft.api.research.ResearchNavigation.resolve("trigger",definitions,BookCatalog.EMPTY,knowledge).reason()==thaumcraft.api.research.ResearchNavigation.Reason.KNOWN,"Completed link opens the research page");
  System.out.println("TC4_VIEWER_PRESENTATION_PASS checks="+checks);
 }
}

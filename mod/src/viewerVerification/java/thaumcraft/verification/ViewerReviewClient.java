package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.api.crafting.RecipeDisplay;
import thaumcraft.viewers.*;

/** Opens actual viewer screens and reads back the game's framebuffer at GUI scales 1-4. */
final class ViewerReviewClient {
 static final Path OUT=Path.of(System.getProperty("thaumcraft.viewerqa.output"));
 static final boolean EMI=System.getProperty("thaumcraft.viewerqa.viewer","emi").equals("emi");
 static int scene=-1,ticks,wait;static boolean capture,done;static String name;
 static final List<String> checks=new ArrayList<>();
 static void install(net.neoforged.bus.api.IEventBus bus){
  bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.skipMultiplayerWarning=true;mc.options.onboardAccessibility=false;mc.options.languageCode="en_us";mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(30);mc.options.renderDistance().set(2);mc.options.save();org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);}));
  NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{try{tick();}catch(Throwable t){fail(t);}});
  NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post e)->{if(capture)try{capture=false;var mc=Minecraft.getInstance();mc.renderBuffers().bufferSource().endBatch();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}Files.writeString(OUT.resolve("checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));}catch(Throwable t){fail(t);}});
 }
 static void tick()throws Exception{
  if(done)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
  if(++ticks<140)return;
  if(scene<0){if(Catalog.all().size()<100||!(EMI?Emi.ready():Jei.ready())){if(ticks>1800)throw new AssertionError("Viewer did not initialize");return;}Files.createDirectories(OUT);Files.deleteIfExists(OUT.resolve("failure.txt"));Files.deleteIfExists(OUT.resolve("passed.json"));checks.add("actual viewer initialized: "+(EMI?"EMI":"JEI"));checks.add("aspect variants="+thaumcraft.api.aspects.AspectApi.variants().size());if(!EMI)Jei.variants();scene=Integer.getInteger("thaumcraft.viewerqa.start",0);open();return;}
  if(++wait==6&&scene>=40&&scene<=41){if(EMI)Emi.hover(scene==40);else Jei.hover(scene==40);}
  if(wait==18&&EMI&&scene==40)Emi.checkHover();
  if(wait==12){mc.gui.getChat().clearMessages(false);if(EMI&&scene<43)Emi.bounds();if(scene>=43)navigationCheck();name=scene<40?"scale-"+(scene/10+1)+"-"+(scene%10):"detail-"+scene;capture=true;}
  if(wait>=20){scene++;if(scene>=46){done=true;Files.writeString(OUT.resolve("passed.json"),new com.google.gson.Gson().toJson(Map.of("passed",true,"suite_scenes",46,"start",Integer.getInteger("thaumcraft.viewerqa.start",0),"screenshots_this_run",46-Integer.getInteger("thaumcraft.viewerqa.start",0))));mc.stop();return;}open();}
 }
 static void open()throws Exception{
  var mc=Minecraft.getInstance();if(scene>=40){extra();return;}int scale=scene/10+1;mc.options.guiScale().set(scale);mc.resizeDisplay();check(mc.getWindow().getGuiScale()==scale,"actual GUI scale "+scale);mc.setScreen(new InventoryScreen(mc.player));move(0,0);int kind=scene%10;
  if(kind<7){var k=RecipeDisplay.Kind.values()[kind];var r=Catalog.all().stream().filter(d->d.kind()==k&&Catalog.visible(d)).max(Comparator.comparingInt(d->k==RecipeDisplay.Kind.INFUSION?d.inputs().size():d.vis().size()+d.essentia().size())).orElseThrow();if(EMI)Emi.recipe(r);else Jei.recipe(r);checks.add("opened "+k+" "+r.id());}
  else {var k=AspectRecipes.Kind.values()[kind-7];var e=AspectRecipes.entries().stream().filter(a->a.kind()==k&&(k==AspectRecipes.Kind.INDEX||a.aspect().equals("ignis"))).findFirst().orElseThrow();if(EMI)Emi.aspect(e);else Jei.aspect(e);}
  check(mc.screen!=null&&!(mc.screen instanceof InventoryScreen),"real recipe screen opened");wait=0;
 }
 static String navigationKey="";
 static void extra()throws Exception{
  var mc=Minecraft.getInstance();mc.options.guiScale().set(3);mc.resizeDisplay();mc.player.closeContainer();mc.setScreen(new InventoryScreen(mc.player));move(0,0);wait=0;
  if(scene==40){var entry=AspectRecipes.entries().stream().filter(a->a.kind()==AspectRecipes.Kind.SOURCES&&a.aspect().equals("ignis")).findFirst().orElseThrow();if(EMI)Emi.aspect(entry);else Jei.aspect(entry);return;}
  var original=Catalog.all().stream().filter(r->r.kind()==RecipeDisplay.Kind.INFUSION&&!r.research().isEmpty()).findFirst().orElseThrow();
  if(scene==41){if(EMI)Emi.recipe(original);else Jei.recipe(original);return;}
  var state=thaumcraft.api.research.ResearchApi.clientKnowledge();var known=state.completedResearch().stream().filter(k->thaumcraft.api.research.ResearchApi.definitions().containsKey(k)).findFirst().orElseThrow();
  if(scene==42){var r=new RecipeDisplay(original.id(),original.recipeId(),original.kind(),List.of(known),original.inputs(),original.outputs(),original.essentia(),original.aspectOutputs(),original.vis(),original.instability(),original.experience(),original.catalysts(),original.notes());if(EMI)Emi.fixture(r);else Jei.recipe(r);return;}
  navigationKey=scene==43?original.research().getFirst():scene==44?"ELDRITCHMINOR":known;thaumcraft.api.client.BookNavigation.open(navigationKey);
 }
 static void navigationCheck()throws Exception{var mc=Minecraft.getInstance();check(mc.screen instanceof thaumcraft.client.ThaumonomiconScreen,"real server-authorized Thaumonomicon opened for "+navigationKey);var target=thaumcraft.api.research.ResearchNavigation.resolve(navigationKey,thaumcraft.api.research.ResearchApi.definitions(),thaumcraft.api.research.ResearchApi.books(),thaumcraft.api.research.ResearchApi.clientKnowledge());if(target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.KNOWN){var f=mc.screen.getClass().getDeclaredField("selected");f.setAccessible(true);check(f.get(mc.screen)!=null,"completed research opens its page");}else if(!target.focus().isEmpty()){var f=mc.screen.getClass().getDeclaredField("navigationHighlight");f.setAccessible(true);check(target.focus().equals(f.get(mc.screen)),"nearest available research highlighted");var d=thaumcraft.api.research.ResearchApi.definitions().get(target.focus());for(String field:List.of("panX","panY")){var p=mc.screen.getClass().getDeclaredField(field);p.setAccessible(true);check(p.getDouble(mc.screen)==-(field.equals("panX")?d.column():d.row())*24,"tree centered: "+field);}}else check(target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.UNDISCOVERED,"trigger locked research stays undiscovered");}
 static void check(boolean value,String text){if(!value)throw new AssertionError(text);checks.add(text);}
 static void move(int x,int y)throws Exception{var mc=Minecraft.getInstance();var w=mc.getWindow();var m=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);m.setAccessible(true);m.invoke(mc.mouseHandler,w.getWindow(),x*w.getScreenWidth()/(double)w.getGuiScaledWidth(),y*w.getScreenHeight()/(double)w.getGuiScaledHeight());}
 static void fail(Throwable t){if(done)return;done=true;t.printStackTrace();try{Files.createDirectories(OUT);Files.writeString(OUT.resolve("failure.txt"),t+" scene="+scene+" ticks="+ticks);}catch(Exception ignored){}Minecraft.getInstance().stop();}
 static final class Emi {
  static boolean ready(){return dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().stream().anyMatch(r->r instanceof thaumcraft.viewers.emi.EmiDisplay);}
  static void recipe(RecipeDisplay r){var recipe=dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().stream().filter(d->d instanceof thaumcraft.viewers.emi.EmiDisplay e&&e.display.id().equals(r.id())).findFirst().orElseThrow();dev.emi.emi.api.EmiApi.displayRecipe(recipe);}
  static void aspect(AspectRecipes.Entry e){var c=dev.emi.emi.api.EmiApi.getRecipeManager().getCategories().stream().filter(d->d.getId().getPath().equals("aspects/"+e.kind().name().toLowerCase(Locale.ROOT))).findFirst().orElseThrow();dev.emi.emi.api.EmiApi.displayRecipe(new thaumcraft.viewers.emi.EmiAspectPage(e,c));}
  static void fixture(RecipeDisplay r){var c=dev.emi.emi.api.EmiApi.getRecipeManager().getCategories().stream().filter(d->d.getId().equals(Catalog.id(r.kind()))).findFirst().orElseThrow();dev.emi.emi.api.EmiApi.displayRecipe(new thaumcraft.viewers.emi.EmiDisplay(r,c));}
  static void hover(boolean aspect)throws Exception{var screen=Minecraft.getInstance().screen;var f=screen.getClass().getDeclaredField("currentPage");f.setAccessible(true);for(var group:(List<?>)f.get(screen)){var c=group.getClass();if(c.getField("recipe").get(group)==null)continue;int gx=c.getField("x").getInt(group),gy=c.getField("y").getInt(group);if(aspect){for(var w:(List<?>)c.getField("widgets").get(group))if(w instanceof dev.emi.emi.api.widget.SlotWidget slot&&slot.getStack() instanceof thaumcraft.viewers.emi.EmiAspect){var b=slot.getBounds();move(gx+b.x()+b.width()/2,gy+b.y()+b.height()/2);return;}}else{var recipe=(thaumcraft.viewers.emi.EmiDisplay)c.getField("recipe").get(group);var l=new Layout(recipe.display);var fit=new thaumcraft.viewers.emi.FittedWidgets(Math.min(1,(c.getField("height").getInt(group)-2f)/l.height),0);var b=fit.bounds(1,15,36,18);move(gx+b.x()+2,gy+b.y()+2);return;}}}
  static void checkHover()throws Exception{var screen=Minecraft.getInstance().screen;var f=screen.getClass().getDeclaredField("hoveredWidget");f.setAccessible(true);var hovered=f.get(screen);if(hovered instanceof dev.emi.emi.api.widget.SlotWidget s)check(s.getStack().getTooltip().size()>=5,"EMI aspect has visible name, units, composition and sources");var value=dev.emi.emi.api.EmiApi.getHoveredStack(false).getStack();check(hovered instanceof dev.emi.emi.api.widget.SlotWidget slot&&slot.getStack() instanceof thaumcraft.viewers.emi.EmiAspect,"scaled aspect hover lookup: widget="+hovered+" API="+value+" mouse="+Minecraft.getInstance().mouseHandler.xpos()+","+Minecraft.getInstance().mouseHandler.ypos());}
  static void bounds()throws Exception{var screen=Minecraft.getInstance().screen;var f=screen.getClass().getDeclaredField("currentPage");f.setAccessible(true);for(var group:(List<?>)f.get(screen)){var c=group.getClass();int height=c.getField("height").getInt(group);var recipe=(dev.emi.emi.api.recipe.EmiRecipe)c.getField("recipe").get(group);if(recipe==null)continue;check(recipe.getDisplayHeight()<=height,"EMI actual card contains full layout: "+recipe.getDisplayHeight()+" <= "+height);}}
 }
 static final class Jei {
  static mezz.jei.api.runtime.IJeiRuntime runtime(){try{var f=thaumcraft.viewers.jei.JeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
  static boolean ready(){return runtime()!=null;}
  static void variants(){var all=runtime().getIngredientManager().getAllIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK);for(var variant:thaumcraft.api.aspects.AspectApi.variants())check(all.stream().anyMatch(s->net.minecraft.world.item.ItemStack.isSameItemSameComponents(s,variant)),"JEI indexes filled variant "+variant);}
  static void hover(boolean aspect)throws Exception{var screen=Minecraft.getInstance().screen;var layouts=screen.getClass().getMethod("getRecipeLayouts").invoke(screen);var field=layouts.getClass().getDeclaredField("recipeLayoutsWithButtons");field.setAccessible(true);var button=((List<?>)field.get(layouts)).getFirst();var layout=(mezz.jei.api.gui.IRecipeLayoutDrawable<?>)button.getClass().getMethod("getRecipeLayout").invoke(button);var rect=layout.getRect();if(aspect){var slot=layout.getRecipeSlotsView().getSlotViews().stream().filter(v->v.getDisplayedIngredient(thaumcraft.viewers.jei.JeiAspects.TYPE).isPresent()).findFirst().orElseThrow();var b=((mezz.jei.api.gui.ingredient.IRecipeSlotDrawable)slot).getRect();move(rect.getX()+b.getX()+b.getWidth()/2,rect.getY()+b.getY()+b.getHeight()/2);}else{var r=(RecipeDisplay)layout.getRecipe();var fit=thaumcraft.viewers.jei.FittedRecipe.of(layout.getRecipeCategory().getHeight(),new Layout(r).height);move(rect.getX()+fit.x(5),rect.getY()+fit.y(18));}}

  static void recipe(RecipeDisplay r){var type=new mezz.jei.api.recipe.RecipeType<>(Catalog.id(r.kind()),RecipeDisplay.class);var rt=runtime();rt.getRecipesGui().showRecipes(rt.getRecipeManager().getRecipeCategory(type),List.of(r),List.of());}
  static void aspect(AspectRecipes.Entry e){var rt=runtime();rt.getRecipesGui().showRecipes(rt.getRecipeManager().getRecipeCategory(thaumcraft.viewers.jei.JeiAspectCategory.type(e.kind())),List.of(e),List.of());}
 }
}

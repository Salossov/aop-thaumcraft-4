package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.lang.reflect.Field;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.*;
import thaumcraft.api.knowledge.PlayerKnowledge.ScanKind;
import thaumcraft.blocks.*;
import thaumcraft.client.ThaumonomiconScreen;
import thaumcraft.config.ThaumcraftClientConfig;
import thaumcraft.items.*;
import thaumcraft.menu.*;
import thaumcraft.network.*;
import thaumcraft.registry.R2Content;

/** Drives real client inventory clicks, use input and production payloads; no research or charge grants. */
@EventBusSubscriber(modid="thaumcraft_r2_test",value=Dist.CLIENT)
public final class ClientCycleQa {
    private static final Minecraft MC=Minecraft.getInstance();
    private static final String MODE=System.getProperty("thaumcraft.r2.phase","client_sp");
    private static final Path EVIDENCE=Path.of(System.getProperty("thaumcraft.r2.root"),"analysis/r2/evidence");
    private record Task(Runnable action,BooleanSupplier ready,int minimum,String label){}
    private static final Deque<Task> tasks=new ArrayDeque<>();private static final List<String> checks=new ArrayList<>();
    private static Task current;private static int elapsed,ticks;private static boolean launched,started,done;private static long fixtureSequence=System.currentTimeMillis(),researchSequence;
    private static final Map<String,Integer> charged=new LinkedHashMap<>();
    private static Task action(Runnable run,int delay){return new Task(run,()->true,delay,"");}
    private static Task waiting(BooleanSupplier ready,String description){return new Task(()->{},ready,5,description);}
    private static void add(Runnable run){tasks.add(action(run,8));}
    private static void waitFor(BooleanSupplier ready,String text){tasks.add(waiting(ready,text));}
    private static void front(List<Task> next){for(int i=next.size()-1;i>=0;i--)tasks.addFirst(next.get(i));}
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);checks.add(text);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(done||!MODE.startsWith("client_"))return;
        try{
            if(++ticks>18000)throw new AssertionError("overall client timeout");
            if(!launched&&MC.screen instanceof AccessibilityOnboardingScreen){MC.options.onboardAccessibility=false;MC.options.save();MC.setScreen(new TitleScreen());}
            if(!launched&&MC.screen instanceof TitleScreen){launched=true;MC.options.renderDistance().set(5);MC.options.pauseOnLostFocus=false;MC.options.guiScale().set(2);MC.options.save();
                if((MODE.equals("client_mp")||MODE.equals("client_review"))){var server=new ServerData("R2 localhost","127.0.0.1:25575",ServerData.Type.OTHER);ConnectScreen.startConnecting(MC.screen,MC,ServerAddress.parseString(server.ip),server,false,null);}
                else {var settings=new LevelSettings("TC4 R2 visual",net.minecraft.world.level.GameType.SURVIVAL,false,Difficulty.NORMAL,false,new GameRules(),WorldDataConfiguration.DEFAULT);MC.createWorldOpenFlows().createFreshLevel("tc4-r2-visual-"+MC.getUser().getName(),settings,new WorldOptions(4235,false,false),WorldPresets::createNormalWorldDimensions,MC.screen);}
            }
            if(!started&&MC.player!=null&&MC.level!=null&&MC.level.getBlockState(ClientCycleFixture.CRAFT).is(Blocks.CRAFTING_TABLE)&&ClientKnowledge.session()!=null){started=true;if(MODE.equals("client_review"))buildReview();else build();}
            if(!started)return;
            if(current==null){current=tasks.pollFirst();elapsed=0;if(current==null){finish(null);return;}current.action.run();}
            if(++elapsed>=current.minimum&&current.ready.getAsBoolean()){if(!current.label.isEmpty())checks.add(current.label);thaumcraft.Thaumcraft.LOGGER.info("R2 client step: {}",current.label);current=null;}
            else if(elapsed>900)throw new AssertionError("step timeout: "+current.label+"; screen="+MC.screen);
        }catch(Throwable failure){thaumcraft.Thaumcraft.LOGGER.error("R2 real client QA failed",failure);finish(failure);}
    }
    private static void finish(Throwable failure){done=true;MC.options.keyUse.setDown(false);MC.options.keyUp.setDown(false);try{Files.createDirectories(EVIDENCE);var report=new LinkedHashMap<String,Object>();report.put("passed",failure==null);report.put("mode",MODE);report.put("player",MC.player==null?"":MC.player.getUUID().toString());report.put("checks",checks);report.put("time",java.time.Instant.now().toString());report.put("boundary","Automated real client. Raw materials and bounded scene are fixtures. Book page selection and one available child for link-animation rendering are injected for visual inspection; crafting, research actions, charging and rewards use production client/server paths.");if(failure!=null)report.put("error",failure.toString());Files.writeString(EVIDENCE.resolve("real-"+MODE+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}catch(Exception error){throw new RuntimeException(error);}}
    private static void close(){if(MC.player.containerMenu!=MC.player.inventoryMenu)MC.player.closeContainer();MC.setScreen(null);}
    private static void click(int index,int button,ClickType type){MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId,index,button,type,MC.player);}
    private static int owned(Item item){var menu=MC.player.containerMenu;return menu.slots.stream().filter(s->s.container==MC.player.getInventory()&&s.getItem().is(item)).findFirst().orElseThrow(()->new AssertionError("Missing item "+item)).index;}
    private static int count(Item item){return MC.player.getInventory().items.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    private static void equip(Item item){close();int slot=owned(item);click(slot,0,ClickType.SWAP);MC.player.getInventory().selected=0;}
    private static void emptyHand(){close();var menu=MC.player.inventoryMenu;var slot=menu.slots.stream().filter(s->s.container==MC.player.getInventory()&&s.getSlotIndex()<36&&s.getSlotIndex()!=MC.player.getInventory().selected&&s.getItem().isEmpty()).findFirst().orElseThrow();click(slot.index,0,ClickType.SWAP);MC.player.getInventory().selected=0;}
    private static void use(BlockPos pos){MC.gameMode.useItemOn(MC.player,InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.UP,pos,false));}
    private static void useItem(){MC.gameMode.useItem(MC.player,InteractionHand.MAIN_HAND);}
    private static void move(String name){add(()->{close();try{Files.writeString(EVIDENCE.resolve("client-fixture-request.json"),new Gson().toJson(Map.of("sequence",++fixtureSequence,"command",name,"player",MC.player.getUUID().toString())));}catch(Exception e){throw new RuntimeException(e);}});waitFor(()->{try{return JsonParser.parseString(Files.readString(EVIDENCE.resolve("client-fixture-response.json"))).getAsJsonObject().get("sequence").getAsLong()==fixtureSequence;}catch(Exception e){return false;}},"fixture ready: "+name);}
    private static void snapshot(String name){tasks.add(action(()->Screenshot.grab(MC.gameDirectory,"r2-"+MODE+"-"+name+".png",MC.getMainRenderTarget(),message->thaumcraft.Thaumcraft.LOGGER.info("R2 screenshot {}",message.getString())),10));}
    private static void material(Item item,int slot){final int[] source={0};add(()->{source[0]=owned(item);click(source[0],0,ClickType.PICKUP);});add(()->click(slot,1,ClickType.PICKUP));add(()->click(source[0],0,ClickType.PICKUP));}
    private static void craft(Item[] grid,Item output){final int[] before={0};add(()->{close();use(ClientCycleFixture.CRAFT);});waitFor(()->MC.player.containerMenu instanceof CraftingMenu,"normal crafting menu opened");add(()->before[0]=count(output));for(int i=0;i<grid.length;i++)if(grid[i]!=null)material(grid[i],i+1);add(()->click(0,0,ClickType.QUICK_MOVE));waitFor(()->count(output)>before[0],"crafted "+output);add(ClientCycleQa::close);}
    private static void research(int action,String first,String second,int q,int r){var menu=(ActionMenu)MC.player.containerMenu;PacketDistributor.sendToServer(new ResearchActionPayload(MC.player.containerMenu.containerId,menu.token(),++researchSequence,action,first,second,q,r));}
    private static void ensure(String tag,int amount){var aspect=Aspect.getAspect(tag);if(ClientKnowledge.snapshot().pool(aspect)>=amount)return;if(aspect.isPrimal())throw new AssertionError("primal fixture resources exhausted: "+tag);var parts=aspect.getComponents();front(List.of(action(()->ensure(parts.get(0).getTag(),parts.get(0)==parts.get(1)?2:1),1),action(()->ensure(parts.get(1).getTag(),1),1),action(()->research(1,parts.get(0).getTag(),parts.get(1).getTag(),0,0),5),waiting(()->ClientKnowledge.snapshot().pool(aspect)>0,"combined "+tag),action(()->ensure(tag,amount),1)));}
    private static void selectPage(String key,int spread){try{var screen=(ThaumonomiconScreen)MC.screen;Field selected=ThaumonomiconScreen.class.getDeclaredField("selected"),page=ThaumonomiconScreen.class.getDeclaredField("spread");selected.setAccessible(true);page.setAccessible(true);selected.set(screen,key.isEmpty()?null:ClientKnowledge.definitions().get(key));page.setInt(screen,spread);}catch(Exception e){throw new RuntimeException(e);}}
    private static Map<String,thaumcraft.api.research.ResearchDefinition> originalDefinitions;
    private static void animationFixture(boolean enable){try{Field field=ClientKnowledge.class.getDeclaredField("definitions");field.setAccessible(true);if(!enable){field.set(null,originalDefinitions);return;}originalDefinitions=ClientKnowledge.definitions();var copy=new LinkedHashMap<>(originalDefinitions);var parent=copy.get("ASPECTS");copy.put("QA_WAVY_LINK",new thaumcraft.api.research.ResearchDefinition("QA_WAVY_LINK","BASICS",List.of("ASPECTS"),Map.of("aer",1),1,false,false,parent.column()+2,parent.row()+2,List.of(),List.of(),"thaumcraft:textures/misc/r_aspects.png"));field.set(null,Collections.unmodifiableMap(copy));}catch(Exception e){throw new RuntimeException(e);}}
    private static void aspectPage(){selectPage("ASPECTS",0);try{var method=ThaumonomiconScreen.class.getDeclaredMethod("pages");method.setAccessible(true);var pages=(List<?>)method.invoke(MC.screen);for(int i=0;i<pages.size();i++){var accessor=pages.get(i).getClass().getDeclaredMethod("aspects");accessor.setAccessible(true);if(!((List<?>)accessor.invoke(pages.get(i))).isEmpty()){selectPage("ASPECTS",i/2);return;}}throw new AssertionError("dynamic aspect pages absent");}catch(ReflectiveOperationException e){throw new RuntimeException(e);}}
    private static void recipePage(String key,String recipe){selectPage(key,0);try{var method=ThaumonomiconScreen.class.getDeclaredMethod("pages");method.setAccessible(true);var pages=(List<?>)method.invoke(MC.screen);for(int i=0;i<pages.size();i++){var accessor=pages.get(i).getClass().getDeclaredMethod("recipe");accessor.setAccessible(true);if(recipe.equals(accessor.invoke(pages.get(i)))){selectPage(key,i/2);return;}}throw new AssertionError("recipe page absent: "+recipe);}catch(ReflectiveOperationException e){throw new RuntimeException(e);}}
    private static void book(){add(()->{equip(R2Content.BOOK.get());useItem();});waitFor(()->MC.player.containerMenu instanceof ThaumonomiconMenu,"book menu synchronized");}
    private static void buildReview(){
        add(()->require(ClientKnowledge.snapshot().completedResearch().contains("GOGGLES")&&MC.player.getItemBySlot(EquipmentSlot.HEAD).is(R2Content.GOGGLES.get()),"dedicated restart preserves real research and equipped goggles"));
        add(()->{for(var shard:R2Content.SHARDS)require((MC.getItemColors().getColor(new ItemStack(shard.get()),0)>>>24)==255,"shard item tint has opaque alpha: "+shard.getId());});
        book();add(()->recipePage("THAUMOMETER","thaumometer"));snapshot("normal-crafting");add(()->recipePage("THAUMONOMICON","@thaumonomicon"));snapshot("mystical-construct");add(ClientCycleQa::aspectPage);snapshot("aspect-pages");add(()->selectPage("GOGGLES",1));snapshot("goggles-recipe");add(()->{selectPage("",0);animationFixture(true);});snapshot("tree-animation-a");tasks.add(action(()->{},5));snapshot("tree-animation-b");add(()->animationFixture(false));add(()->{close();MC.setScreen(new InventoryScreen(MC.player));});snapshot("inventory-models");
    }
    private static void build(){
        add(()->require(!ClientKnowledge.snapshot().completedResearch().contains("GOGGLES"),"fresh real UUID has no GOGGLES grant"));
        Item N=Items.IRON_NUGGET,S=Items.STICK,cap=R2Content.IRON_CAP.get(),wand=R2Content.WAND.get(),table=R2Content.TABLE_ITEM.get(),ink=R2Content.SCRIBING_TOOLS.get(),scanner=R2Content.THAUMOMETER.get();
        Item[] caps={N,N,N,N,null,N,null,null,null};craft(caps,cap);craft(caps,cap);craft(new Item[]{null,null,cap,null,S,null,cap,null,null},wand);
        Item[] tables={Items.OAK_SLAB,Items.OAK_SLAB,Items.OAK_SLAB,Items.OAK_PLANKS,null,Items.OAK_PLANKS,null,null,null};for(int i=0;i<3;i++)craft(tables,table);
        for(int i=0;i<2;i++)craft(new Item[]{Items.GLASS_BOTTLE,Items.FEATHER,Items.BLACK_DYE,null,null,null,null,null,null},ink);
        Item[] scanners={null,R2Content.SHARDS.get(0).get(),null,Items.GOLD_INGOT,Items.GLASS,Items.GOLD_INGOT,null,R2Content.SHARDS.get(1).get(),null};craft(scanners,scanner);craft(scanners,scanner);
        move("SHELF");add(()->equip(wand));add(()->use(ClientCycleFixture.SHELF));add(()->MC.options.keyUp.setDown(true));tasks.add(action(()->{},25));add(()->MC.options.keyUp.setDown(false));waitFor(()->count(R2Content.BOOK.get())==1,"bookshelf transformed and resulting book collected");
        move("RESEARCH");add(()->equip(table));add(()->use(ClientCycleFixture.TABLE.below()));add(()->use(ClientCycleFixture.TABLE.east().below()));add(()->equip(ink));add(()->use(ClientCycleFixture.TABLE));waitFor(()->MC.level.getBlockState(ClientCycleFixture.TABLE).is(R2Content.RESEARCH_TABLE.get()),"two tables transformed through normal scribing-tools use");snapshot("research-details");
        move("BENCH");add(()->equip(table));add(()->use(ClientCycleFixture.BENCH.below()));add(()->equip(wand));add(()->use(ClientCycleFixture.BENCH));waitFor(()->MC.level.getBlockState(ClientCycleFixture.BENCH).is(R2Content.ARCANE_WORKBENCH.get()),"arcane table created through normal wand use");add(()->use(ClientCycleFixture.BENCH));waitFor(()->MC.player.containerMenu instanceof ArcaneMenu,"arcane menu synchronized");add(()->click(1,0,ClickType.QUICK_MOVE));waitFor(()->count(wand)==1,"wand retrieved through networked slot");add(ClientCycleQa::close);snapshot("arcane-details");
        move("NODE");add(ClientCycleQa::emptyHand);snapshot("faint-node");add(()->equip(scanner));snapshot("revealed-node");
        for(int i=0;i<10;i++){if(i>0)move("NEW_NODE");add(()->MC.options.keyUse.setDown(true));tasks.add(action(()->{},35));add(()->MC.options.keyUse.setDown(false));}
        add(()->require(ClientKnowledge.snapshot().scanned(ScanKind.PHENOMENON).size()>=10,"ten real held scans awarded normal node knowledge"));
        book();add(()->animationFixture(true));snapshot("tree-animation-a");tasks.add(action(()->{},5));snapshot("tree-animation-b");add(()->animationFixture(false));add(()->{researchSequence=0;research(0,"GOGGLES","",0,0);});waitFor(()->count(R2Content.NOTES.get())==1,"production book request created GOGGLES note");
        move("RESEARCH");add(()->use(ClientCycleFixture.TABLE));waitFor(()->MC.player.containerMenu instanceof ResearchMenu,"research table menu synchronized");add(()->{researchSequence=0;click(owned(R2Content.NOTES.get()),0,ClickType.QUICK_MOVE);});waitFor(()->!((ResearchMenu)MC.player.containerMenu).table.getItem(1).isEmpty(),"note transferred to server-backed research slot");
        for(String tag:List.of("victus","mortuus","spiritus","vacuos","potentia","praecantatio","auram","sensus"))add(()->ensure(tag,1));
        add(()->{var note=ResearchNotes.read(((ResearchMenu)MC.player.containerMenu).table.getItem(1));var solution=GameplayVerification.solve(note,ClientKnowledge.snapshot().aspectPools().keySet());var next=new ArrayList<Task>();for(var step:solution){next.add(action(()->ensure(step.tag(),1),1));next.add(action(()->research(2,step.tag(),"",step.hex().q(),step.hex().r()),5));next.add(waiting(()->{var updated=ResearchNotes.read(((ResearchMenu)MC.player.containerMenu).table.getItem(1));return updated!=null&&updated.grid.get(step.hex()).aspect().equals(step.tag());},"placed original aspect link "+step));}front(next);});
        waitFor(()->ResearchNotes.read(((ResearchMenu)MC.player.containerMenu).table.getItem(1)).complete,"GOGGLES puzzle completed through real production payloads");snapshot("completed-research");add(()->click(1,0,ClickType.QUICK_MOVE));waitFor(()->count(R2Content.NOTES.get())==1,"completed note retrieved");add(()->{equip(R2Content.NOTES.get());useItem();});waitFor(()->ClientKnowledge.snapshot().completedResearch().contains("GOGGLES"),"using completed note unlocked GOGGLES on real client");
        move("NODE");add(()->equip(wand));add(()->MC.options.keyUse.setDown(true));tasks.add(action(()->{},20));waitFor(()->MC.player.isUsingItem()&&MC.level.getBlockEntity(ClientCycleFixture.NODE) instanceof AuraNodeBlockEntity node&&MC.player.getUUID().equals(node.drainer()),"held input and server drainer synchronize visible beam");snapshot("charge-animation-a");tasks.add(action(()->{},4));snapshot("charge-animation-b");waitFor(()->Aspects.PRIMALS.stream().allMatch(a->WandVis.get(MC.player.getMainHandItem(),a)>=600),"real held charging supplied all six arcane costs");add(()->{MC.options.keyUse.setDown(false);for(var aspect:Aspects.PRIMALS)charged.put(aspect.getTag(),WandVis.get(MC.player.getMainHandItem(),aspect));});waitFor(()->((AuraNodeBlockEntity)MC.level.getBlockEntity(ClientCycleFixture.NODE)).drainer()==null,"release clears production charge beam");
        move("BENCH");add(()->use(ClientCycleFixture.BENCH));waitFor(()->MC.player.containerMenu instanceof ArcaneMenu,"arcane menu reopened");add(()->click(owned(wand),0,ClickType.QUICK_MOVE));waitFor(()->!((ArcaneMenu)MC.player.containerMenu).table.getItem(10).isEmpty(),"charged wand inserted");Item[] inputs={Items.LEATHER,Items.GOLD_INGOT,Items.LEATHER,Items.LEATHER,null,Items.LEATHER,scanner,Items.GOLD_INGOT,scanner};for(int i=0;i<9;i++)if(inputs[i]!=null)material(inputs[i],i+2);
        waitFor(()->MC.player.containerMenu.getSlot(0).getItem().is(R2Content.GOGGLES.get()),"researched arcane output visible on real client");snapshot("arcane-goggles-output");add(()->click(0,0,ClickType.QUICK_MOVE));waitFor(()->count(R2Content.GOGGLES.get())==1,"real output pickup crafted goggles");add(()->{var menu=(ArcaneMenu)MC.player.containerMenu;var used=menu.table.getItem(10);for(var aspect:Aspects.PRIMALS)require(charged.get(aspect.getTag())-WandVis.get(used,aspect)==(int)((aspect==Aspects.ORDER||aspect==Aspects.ENTROPY?3:5)*100*1.1f),"real arcane pickup spends original cost once: "+aspect);require(menu.table.getItem(6).isEmpty()&&menu.table.getItem(8).isEmpty()&&menu.getSlot(0).getItem().isEmpty(),"both scanners consumed and no duplicate result");});
        add(()->{close();click(owned(R2Content.GOGGLES.get()),0,ClickType.PICKUP);click(5,0,ClickType.PICKUP);});waitFor(()->MC.player.getItemBySlot(EquipmentSlot.HEAD).is(R2Content.GOGGLES.get()),"goggles equipped through ordinary inventory slots");move("NODE");add(ClientCycleQa::emptyHand);snapshot("goggles-reveal");
        book();add(()->recipePage("THAUMONOMICON","@thaumonomicon"));snapshot("mystical-construct");add(()->selectPage("GOGGLES",1));snapshot("goggles-recipe");add(()->aspectPage());snapshot("aspect-pages");add(()->recipePage("THAUMOMETER","thaumometer"));snapshot("normal-crafting");add(()->{ThaumcraftClientConfig.BOOK_FONT_STYLE.set(ThaumcraftClientConfig.BookFontStyle.UNICODE);selectPage("ASPECTS",0);});snapshot("unicode-style");add(()->{ThaumcraftClientConfig.BOOK_FONT_STYLE.set(ThaumcraftClientConfig.BookFontStyle.VANILLA);selectPage("ASPECTS",0);});snapshot("vanilla-style");
        move("BENCH");add(()->use(ClientCycleFixture.BENCH));waitFor(()->MC.player.containerMenu instanceof ArcaneMenu,"retrieve spent wand menu");add(()->click(1,0,ClickType.QUICK_MOVE));waitFor(()->count(wand)==1,"spent wand retrieved for tooltip inspection");add(()->{close();MC.setScreen(new InventoryScreen(MC.player));});snapshot("inventory-models");move("RESEARCH");add(()->use(ClientCycleFixture.TABLE));waitFor(()->MC.player.containerMenu instanceof ResearchMenu,"table reopened after progression");add(()->click(0,0,ClickType.QUICK_MOVE));waitFor(()->((ResearchMenu)MC.player.containerMenu).table.getItem(0).isEmpty(),"scribing tool removed through production menu");add(ClientCycleQa::close);snapshot("parchment-without-quill");add(()->require(MC.level.getBlockEntity(ClientCycleFixture.TABLE) instanceof TableBlockEntity tile&&tile.getItem(0).isEmpty(),"world decorations receive inventory removal sync"));
    }
}

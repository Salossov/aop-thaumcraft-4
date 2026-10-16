package thaumcraft.verification;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.api.research.ResearchNote;
import thaumcraft.blocks.*;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.items.*;
import thaumcraft.knowledge.*;
import thaumcraft.menu.*;
import thaumcraft.network.ResearchActionPayload;
import thaumcraft.registry.R2Content;

/** Uses production items, recipes, menus and storage on a real dedicated server. */
final class GameplayVerification {
    record Step(ResearchNote.Hex hex,String tag) {}
    static void run(ServerLevel level,Path evidence,boolean reload,BiConsumer<Boolean,String> check) throws Exception {
        var session=evidence.resolve("gameplay-session.json");UUID id=reload?UUID.fromString(JsonParser.parseString(Files.readString(session)).getAsJsonObject().get("player").getAsString()):UUID.randomUUID();
        var player=FakePlayerFactory.get(level,new GameProfile(id,"TC4_R2_Test"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(0.5,80,-1.5,0,0);player.yRotO=0;player.xRotO=0;
        if(reload) {
            check.accept(KnowledgeService.get(player).completedResearch().contains("GOGGLES"),"GOGGLES survives dedicated server restart under the same UUID");
            check.accept(KnowledgeService.get(player).aspectPools().containsKey("auram"),"discovered compound aspects and pools survive restart");
            var file=level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("thaumcraft/players/"+id+".thaum");
            var nbt=NbtIo.readCompressed(file,NbtAccounter.create(16*1024*1024));check.accept(nbt.hasUUID("playerUUID")&&nbt.getUUID("playerUUID").equals(id),"separate compressed NBT file belongs to the tested UUID");return;
        }
        Files.createDirectories(evidence);Files.writeString(session,new Gson().toJson(Map.of("player",id.toString()))+"\n");KnowledgeService.initialize(player);
        check.accept(KnowledgeService.get(player).completedResearch().contains("THAUMOMETER")&&!KnowledgeService.get(player).completedResearch().contains("GOGGLES"),"new player receives original initial research, no GOGGLES grant");
        check.accept(Aspects.PRIMALS.stream().allMatch(a->KnowledgeService.get(player).pool(a)>=15&&KnowledgeService.get(player).pool(a)<=19),"new player receives original 15..19 points of every primal aspect");
        var initialPools=new LinkedHashMap<>(KnowledgeService.get(player).aspectPools());KnowledgeService.initialize(player);
        check.accept(KnowledgeService.get(player).aspectPools().equals(initialPools),"initial aspect grant is not repeated by login/data initialization");
        KnowledgeService.mutate(player,k->{k.setPool("aer",0);return true;});KnowledgeService.initialize(player);
        check.accept(KnowledgeService.get(player).pool(Aspects.AIR)==0,"exhausted primal pools are not refilled by initialization");
        KnowledgeService.mutate(player,k->{k.setPool("aer",initialPools.get("aer"));return true;});
        var cap=recipe(level,"iron_wand_cap",List.of(new ItemStack(Items.IRON_NUGGET),new ItemStack(Items.IRON_NUGGET),new ItemStack(Items.IRON_NUGGET),new ItemStack(Items.IRON_NUGGET),ItemStack.EMPTY,new ItemStack(Items.IRON_NUGGET)),3,2);
        check.accept(cap.is(R2Content.IRON_CAP.get())&&cap.getCount()==1,"original five-nugget cap recipe");
        var wand=recipe(level,"iron_capped_wooden_wand",List.of(ItemStack.EMPTY,ItemStack.EMPTY,cap.copy(),ItemStack.EMPTY,new ItemStack(Items.STICK),ItemStack.EMPTY,cap.copy(),ItemStack.EMPTY,ItemStack.EMPTY),3,3);
        boolean emptyVis=true;for(var primal:Aspects.PRIMALS)emptyVis&=WandVis.get(wand,primal)==0;
        check.accept(wand.is(R2Content.WAND.get())&&emptyVis,"new iron/wood wand crafted empty");
        player.setItemInHand(InteractionHand.MAIN_HAND,wand);var shelf=new BlockPos(0,81,1);level.setBlockAndUpdate(shelf,Blocks.BOOKSHELF.defaultBlockState());
        R2Content.WAND.get().useOn(context(player,shelf));var books=level.getEntitiesOfClass(ItemEntity.class,new AABB(shelf).inflate(1),e->e.getItem().is(R2Content.BOOK.get()));
        check.accept(level.getBlockState(shelf).isAir()&&books.size()==1,"wand converts bookshelf into Thaumonomicon through its normal use action");var book=books.getFirst().getItem().copy();books.getFirst().discard();
        var ink=recipe(level,"scribing_tools",List.of(new ItemStack(Items.GLASS_BOTTLE),new ItemStack(Items.FEATHER),new ItemStack(Items.BLACK_DYE)),3,1);check.accept(ink.is(R2Content.SCRIBING_TOOLS.get())&&ink.getDamageValue()==0,"scribing tools recipe starts with 100 uses");
        var scanner=recipe(level,"thaumometer",List.of(ItemStack.EMPTY,new ItemStack(R2Content.SHARDS.get(0).get()),ItemStack.EMPTY,new ItemStack(Items.GOLD_INGOT),new ItemStack(Items.GLASS),new ItemStack(Items.GOLD_INGOT),ItemStack.EMPTY,new ItemStack(R2Content.SHARDS.get(1).get()),ItemStack.EMPTY),3,3);
        check.accept(scanner.is(R2Content.THAUMOMETER.get()),"thaumometer accepts two different elemental shards");
        level.setBlockAndUpdate(shelf,Blocks.STONE.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,scanner);R2Content.THAUMOMETER.get().use(level,player,InteractionHand.MAIN_HAND);
        for(int tick=25;tick>=5;tick--)R2Content.THAUMOMETER.get().onUseTick(level,player,scanner,tick);
        check.accept(KnowledgeService.get(player).scanned(PlayerKnowledge.ScanKind.OBJECT).contains("minecraft:stone"),"20-tick held thaumometer scan awards knowledge from the targeted block");
        int earth=KnowledgeService.get(player).pool(Aspects.EARTH);R2Content.THAUMOMETER.get().use(level,player,InteractionHand.MAIN_HAND);for(int tick=25;tick>=5;tick--)R2Content.THAUMOMETER.get().onUseTick(level,player,scanner,tick);
        check.accept(KnowledgeService.get(player).pool(Aspects.EARTH)==earth,"repeat scan cannot award extra aspect points");
        var nodePos=new BlockPos(0,81,1);level.setBlockAndUpdate(nodePos,R2Content.AURA_NODE.get().defaultBlockState());var node=(AuraNodeBlockEntity)level.getBlockEntity(nodePos);
        var vis=new LinkedHashMap<String,Integer>();Aspects.PRIMALS.forEach(a->vis.put(a.getTag(),50));node.initialize(vis,NodeState.Type.NORMAL,NodeState.Modifier.NONE);
        for(int i=0;i<10;i++) {node.initialize(vis,NodeState.Type.NORMAL,NodeState.Modifier.NONE);var scan=node.scanAspects();check.accept(ScanService.scan(player,PlayerKnowledge.ScanKind.PHENOMENON,"R2_NODE_FIXTURE_"+id+"_"+i,scan),"distinct node scan supplies original primal points, fixture "+i);}
        player.setItemInHand(InteractionHand.MAIN_HAND,book);player.getInventory().add(new ItemStack(Items.PAPER,2));player.getInventory().add(ink.copy());var browser=new ThaumonomiconMenu(1,player.getInventory(),UUID.randomUUID());player.containerMenu=browser;
        check.accept(browser.accept(player,new ResearchActionPayload(1,browser.token(),1,0,"GOGGLES","",0,0)),"available GOGGLES creates a note through server menu action");
        check.accept(!browser.accept(player,new ResearchActionPayload(1,browser.token(),1,0,"GOGGLES","",0,0)),"replayed note request rejected");check.accept(!browser.accept(player,new ResearchActionPayload(1,UUID.randomUUID(),2,0,"GOGGLES","",0,0)),"forged menu token rejected");
        var noteStack=player.getInventory().items.stream().filter(s->s.is(R2Content.NOTES.get())).findFirst().orElseThrow();var note=ResearchNotes.read(noteStack);
        check.accept(!note.complete&&note.grid.values().stream().filter(e->e.type()==1).count()==3,"GOGGLES note contains three original fixed aspect anchors");
        var tablePos=new BlockPos(2,80,0);level.setBlockAndUpdate(tablePos,R2Content.TABLE.get().defaultBlockState());level.setBlockAndUpdate(tablePos.east(),R2Content.TABLE.get().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND,ink.copy());R2Content.SCRIBING_TOOLS.get().useOn(context(player,tablePos));
        check.accept(level.getBlockState(tablePos).is(R2Content.RESEARCH_TABLE.get())&&level.getBlockState(tablePos.east()).is(R2Content.RESEARCH_TABLE.get()),"two placed tables and scribing tools form a research table");var table=(TableBlockEntity)level.getBlockEntity(tablePos);table.setItem(1,noteStack.copy());noteStack.setCount(0);
        var research=new ResearchMenu(2,player.getInventory(),table);player.containerMenu=research;long seq=0;
        String[][] combinations={{"terra","aqua"},{"victus","perditio"},{"victus","mortuus"},{"aer","perditio"},{"ordo","ignis"},{"vacuos","potentia"},{"praecantatio","aer"},{"spiritus","aer"}};
        for(var pair:combinations)check.accept(research.accept(player,new ResearchActionPayload(2,research.token(),++seq,1,pair[0],pair[1],0,0)),"original component combination discovers "+Aspect.combine(Aspect.getAspect(pair[0]),Aspect.getAspect(pair[1])));
        var outsider=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TC4_R2_Other"));outsider.moveTo(player.getX(),player.getY(),player.getZ(),0,0);check.accept(!research.accept(outsider,new ResearchActionPayload(2,research.token(),seq+1,1,"terra","aqua",0,0)),"another UUID cannot operate this player's research menu");
        note=ResearchNotes.read(table.getItem(1));var solution=solve(note,KnowledgeService.get(player).aspectPools().keySet());
        for(var step:solution) {var aspect=Aspect.getAspect(step.tag);if(KnowledgeService.get(player).pool(aspect)==0){var components=aspect.getComponents();check.accept(!components.isEmpty()&&research.accept(player,new ResearchActionPayload(2,research.token(),++seq,1,components.get(0).getTag(),components.get(1).getTag(),0,0)),"replenish used compound aspect "+step.tag);}
            check.accept(research.accept(player,new ResearchActionPayload(2,research.token(),++seq,2,step.tag,"",step.hex.q(),step.hex.r())),"place legal aspect link "+step.hex+" "+step.tag);}
        check.accept(ResearchNotes.read(table.getItem(1)).complete,"all GOGGLES anchors connected through valid component links");check.accept(table.getItem(0).getDamageValue()==solution.size(),"each placed link consumes exactly one ink use");
        var completed=table.removeItem(1,1);player.containerMenu=player.inventoryMenu;player.setItemInHand(InteractionHand.MAIN_HAND,completed);R2Content.NOTES.get().use(level,player,InteractionHand.MAIN_HAND);
        check.accept(KnowledgeService.get(player).completedResearch().contains("GOGGLES")&&completed.isEmpty(),"using completed note unlocks GOGGLES and consumes the note");
        var benchPos=new BlockPos(-2,80,0);level.setBlockAndUpdate(benchPos,R2Content.TABLE.get().defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,wand);R2Content.WAND.get().useOn(context(player,benchPos));var bench=(TableBlockEntity)level.getBlockEntity(benchPos);
        check.accept(level.getBlockState(benchPos).is(R2Content.ARCANE_WORKBENCH.get())&&bench.getItem(10).is(R2Content.WAND.get())&&player.getMainHandItem().isEmpty(),"wand transforms table into arcane workbench and occupies its wand slot");
        var arcane=new ArcaneMenu(3,player.getInventory(),bench);player.containerMenu=arcane;Item[] inputs={Items.LEATHER,Items.GOLD_INGOT,Items.LEATHER,Items.LEATHER,Items.AIR,Items.LEATHER,Items.AIR,Items.GOLD_INGOT,Items.AIR};
        for(int i=0;i<9;i++)bench.setItem(i,new ItemStack(inputs[i]));bench.setItem(6,scanner.copy());bench.setItem(8,scanner.copy());
        check.accept(arcane.craft(player).isEmpty()&&!bench.getItem(0).isEmpty(),"arcane recipe refuses empty wand without consuming ingredients");
        wand=bench.removeItem(10,1);player.setItemInHand(InteractionHand.MAIN_HAND,wand);R2Content.WAND.get().useOn(context(player,nodePos));int successes=0;
        R2Content.WAND.get().onUseTick(level,player,wand,72000);var beamTag=node.getUpdateTag(level.registryAccess());
        check.accept(beamTag.hasUUID("drainer")&&beamTag.getUUID("drainer").equals(id)&&beamTag.getLong("drainUntil")>level.getGameTime(),"successful held charge synchronizes the drainer and a bounded beam lifetime");
        check.accept(!node.saveWithoutMetadata(level.registryAccess()).contains("drainer"),"transient charge beam is excluded from the world save");
        player.setYRot(180);player.yRotO=180;player.setYHeadRot(180);player.yHeadRotO=180;R2Content.WAND.get().onUseTick(level,player,wand,71995);
        check.accept(node.drainer()==null&&!node.getUpdateTag(level.registryAccess()).contains("drainer"),"looking away stops charging and removes the synchronized beam");
        player.setYRot(0);player.yRotO=0;player.setYHeadRot(0);player.yHeadRotO=0;R2Content.WAND.get().useOn(context(player,nodePos));
        for(int tick=72000;tick>=70900;tick--) {int before=Aspects.PRIMALS.stream().mapToInt(a->WandVis.get(player.getMainHandItem(),a)).sum();R2Content.WAND.get().onUseTick(level,player,player.getMainHandItem(),tick);int after=Aspects.PRIMALS.stream().mapToInt(a->WandVis.get(player.getMainHandItem(),a)).sum();if(after>before)successes++;}
        R2Content.WAND.get().releaseUsing(wand,level,player,70900);check.accept(successes>0&&Aspects.PRIMALS.stream().allMatch(a->WandVis.get(player.getMainHandItem(),a)>=600),"held wand charges all required primal vis from node at original cadence");
        check.accept(node.drainer()==null,"releasing the wand clears its charge beam");
        var before=new LinkedHashMap<String,Integer>();for(var a:Aspects.PRIMALS)before.put(a.getTag(),WandVis.get(wand,a));bench.setItem(10,wand);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);arcane.broadcastChanges();
        var goggles=arcane.getSlot(0).tryRemove(1,64,player).orElseThrow();check.accept(goggles.is(R2Content.GOGGLES.get())&&goggles.getCount()==1,"normal output pickup crafts one pair of goggles after research and vis");
        for(var a:Aspects.PRIMALS)check.accept(before.get(a.getTag())-WandVis.get(wand,a)==(int)((a==Aspects.ORDER||a==Aspects.ENTROPY?3:5)*100*1.1f),"iron cap vis cost charged once for "+a);
        check.accept(bench.getItem(0).isEmpty()&&bench.getItem(6).isEmpty()&&bench.getItem(8).isEmpty()&&arcane.craft(player).isEmpty(),"ingredients consumed once and a second pickup cannot duplicate goggles");
        check.accept(!KnowledgeService.get(outsider).completedResearch().contains("GOGGLES"),"knowledge remains isolated between player UUIDs");storage(evidence,check);
    }
    private static UseOnContext context(ServerPlayer player,BlockPos pos) {return new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.UP,pos,false));}
    private static ItemStack recipe(ServerLevel level,String key,List<ItemStack> inputs,int width,int height) {var holder=level.getServer().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft",key)).orElseThrow();var recipe=(CraftingRecipe)holder.value();var input=CraftingInput.of(width,height,inputs);if(!recipe.matches(input,level))throw new AssertionError("Recipe does not match: "+key);return recipe.assemble(input,level.registryAccess());}
    static List<Step> solve(ResearchNote original,Set<String> known) {
        var note=original.copy();var anchors=note.grid.entrySet().stream().filter(e->e.getValue().type()==1).map(Map.Entry::getKey).toList();var connected=new HashSet<ResearchNote.Hex>();connected.add(anchors.getFirst());var placed=new ArrayList<Step>();
        for(var target:anchors.subList(1,anchors.size())) {
            var queue=new ArrayDeque<Step>();var previous=new HashMap<Step,Step>();for(var hex:connected){var start=new Step(hex,note.grid.get(hex).aspect());queue.add(start);previous.put(start,null);}Step end=null;
            while(!queue.isEmpty()) {var step=queue.remove();if(step.hex.equals(target)){end=step;break;}var aspect=Aspect.getAspect(step.tag);
                for(int d=0;d<6;d++){var next=step.hex.neighbour(d);var cell=note.grid.get(next);if(cell==null)continue;for(var tag:known){var other=Aspect.getAspect(tag);if(other==null||!aspect.connects(other)||cell.type()>0&&!cell.aspect().equals(tag))continue;var candidate=new Step(next,tag);if(!previous.containsKey(candidate)){previous.put(candidate,step);queue.add(candidate);}}}
            }
            if(end==null)throw new AssertionError("No original-rules solution for "+target);var chain=new ArrayList<Step>();for(var step=end;step!=null;step=previous.get(step))chain.add(step);Collections.reverse(chain);
            for(var step:chain){var cell=note.grid.get(step.hex);if(cell.type()==0){note.grid.put(step.hex,new ResearchNote.Entry(step.tag,2));placed.add(step);}connected.add(step.hex);}
        }
        if(!note.checkComplete(known))throw new AssertionError("Solver produced invalid links");return placed;
    }
    private static void storage(Path evidence,BiConsumer<Boolean,String> check) throws Exception {
        var fixture=evidence.resolve("knowledge-fixtures").resolve(UUID.randomUUID().toString());UUID id=UUID.randomUUID();
        try(var store=new WorldKnowledgeStore(fixture)){var k=store.get(id).copy();k.completeResearch("GOGGLES");store.commit(id,k);store.save(id);k=k.copy();k.setWarp(3,4,5,6);store.commit(id,k);store.save(id);}
        var file=fixture.resolve("thaumcraft/players/"+id+".thaum");var root=NbtIo.readCompressed(file,NbtAccounter.create(16*1024*1024));root.putString("addonValue","retained");root.getCompound("data").putString("addonNested","retained");NbtIo.writeCompressed(root,file);
        try(var store=new WorldKnowledgeStore(fixture)){var k=store.get(id).copy();check.accept(k.completedResearch().contains("GOGGLES")&&k.totalWarp()==12,"compressed knowledge round trip preserves progress and all warp kinds");k.changed();store.commit(id,k);store.save(id);}
        var saved=NbtIo.readCompressed(file,NbtAccounter.create(16*1024*1024));check.accept(saved.getString("addonValue").equals("retained")&&saved.getCompound("data").getString("addonNested").equals("retained"),"unknown add-on fields survive atomic rewrite");
        check.accept(Files.exists(file.resolveSibling(id+".thaumbak"))&&!Files.exists(file.resolveSibling(id+".thaum.tmp")),"atomic save keeps backup and leaves no pending temporary file");
        Files.writeString(file,"deliberately corrupt integration fixture");try(var store=new WorldKnowledgeStore(fixture)){var k=store.get(id).copy();check.accept(k.completedResearch().contains("GOGGLES"),"corrupt main knowledge file recovers from previous backup");k.changed();store.commit(id,k);store.save(id);}
        try(var siblings=Files.list(file.getParent())){check.accept(siblings.anyMatch(p->p.getFileName().toString().startsWith(id+".corrupt-")),"corrupt source is preserved before recovery rewrite");}
        saved=NbtIo.readCompressed(file,NbtAccounter.create(16*1024*1024));saved.putInt("schemaVersion",999);NbtIo.writeCompressed(saved,file);byte[] before=Files.readAllBytes(file);boolean refused=false;
        try(var store=new WorldKnowledgeStore(fixture)){store.get(id);}catch(WorldKnowledgeStore.UnsupportedSchemaException expected){refused=true;}
        check.accept(refused&&Arrays.equals(before,Files.readAllBytes(file)),"future knowledge schema refused without overwriting file or silently using older backup");
    }
    private GameplayVerification() {}
}

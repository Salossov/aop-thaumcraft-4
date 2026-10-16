package thaumcraft.verification;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.*;
import thaumcraft.crafting.InfusionEnchantmentRecipeData;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.*;
import thaumcraft.registry.*;

/** Native world generation and persistence, isolated from the active visual server. */
final class ComparisonWorldBuilder {
    private static final int Y=150;
    private final ServerLevel level;
    private final BiConsumer<Boolean,String> check;
    private final JsonObject manifest;
    private final List<String> missing=new ArrayList<>(),unavailable=new ArrayList<>();
    private final Set<ResourceLocation> preparedRecipes=new HashSet<>();
    private int infusions,chests;
    private ComparisonWorldBuilder(ServerLevel level,BiConsumer<Boolean,String> check,JsonObject manifest){this.level=level;this.check=check;this.manifest=manifest;}
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws java.io.IOException{
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));
        var manifest=JsonParser.parseString(Files.readString(root.resolve("analysis/comparison/manifest.json"))).getAsJsonObject();
        var builder=new ComparisonWorldBuilder(level,check,manifest);
        builder.main();builder.stations();builder.remainingNativeRecipes();builder.knowledge();
        level.getServer().saveEverything(false,true,true);
        var result=new LinkedHashMap<String,Object>();result.put("world",level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().toString());
        result.put("source_stations",manifest.getAsJsonArray("stations").size());result.put("prepared_native_recipes",builder.preparedRecipes.size());result.put("prepared_infusions",builder.infusions);
        result.put("catalog_chests",builder.chests);result.put("missing_native_recipe_keys",builder.missing);result.put("unavailable_dependency_recipe_keys",builder.unavailable);result.put("manual_gameplay_and_visual_acceptance","pending");
        result.put("native_block_entity_save_validation","pending restart check");result.put("natural_worldgen","Normal generator outside prepared chunks");
        Files.writeString(root.resolve("analysis/comparison/port-world.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check)throws java.io.IOException{
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var manifest=JsonParser.parseString(Files.readString(root.resolve("analysis/comparison/manifest.json"))).getAsJsonObject();
        var builder=new ComparisonWorldBuilder(level,check,manifest);int altars=0;
        int expectedAltars=(int)manifest.getAsJsonArray("stations").asList().stream().map(e->e.getAsJsonObject()).filter(s->!s.get("native_recipe").isJsonNull()).filter(s->level.getRecipeManager().byKey(ResourceLocation.parse(s.get("native_recipe").getAsString())).map(h->h.value() instanceof InfusionRecipe).orElse(false)).count();
        for(var entry:manifest.getAsJsonArray("stations")){
            var s=entry.getAsJsonObject();if(s.get("native_recipe").isJsonNull())continue;var holder=level.getRecipeManager().byKey(ResourceLocation.parse(s.get("native_recipe").getAsString()));
            if(holder.isEmpty()||!(holder.get().value() instanceof InfusionRecipe recipe))continue;
            var coordinate=s.getAsJsonArray("position");int x=coordinate.get(0).getAsInt(),z=coordinate.get(2).getAsInt();var matrixPos=new BlockPos(x,Y+2,z);
            for(int dx:new int[]{-7,7})for(int dz:new int[]{-7,7})level.getChunk((x+dx)>>4,(z+dz)>>4);level.getChunkAt(matrixPos);
            check.accept(level.getBlockEntity(matrixPos) instanceof InfusionMatrixBlockEntity matrix&&matrix.active&&matrix.validLocation(),"comparison altar survives dedicated restart: "+s.get("id").getAsString());
            var central=(PedestalBlockEntity)level.getBlockEntity(matrixPos.below(2));var supplied=new ArrayList<ItemStack>();
            var expected=recipe.components(central.getItem(0));for(int i=0;i<expected.size();i++){var offset=manifest.getAsJsonArray("pedestal_ring").get(i).getAsJsonArray();var pos=new BlockPos(x+offset.get(0).getAsInt(),Y,z+offset.get(1).getAsInt());level.getChunkAt(pos);supplied.add(((PedestalBlockEntity)level.getBlockEntity(pos)).getItem(0));}
            check.accept(recipe.matches(new thaumcraft.api.crafting.InfusionInput(central.getItem(0),supplied)),"saved fixture retains native ingredient components: "+holder.get().id());altars++;
        }
        check.accept(altars==expectedAltars,"all "+expectedAltars+" mapped original infusion fixtures survive a real server restart");
        var starter=new BlockPos(-4,Y,8);level.getChunkAt(starter);var chest=(ChestBlockEntity)level.getBlockEntity(starter);
        check.accept(chest!=null&&chest.getItem(0).is(R2Content.BOOK.get())&&chest.getItem(1).is(R2Content.WAND.get()),"comparison starter kit survives disk save");
        var inventory=new net.minecraft.nbt.ListTag();for(int i=0;i<7;i++){var stack=(net.minecraft.nbt.CompoundTag)chest.getItem(i).save(level.registryAccess());stack.putByte("Slot",(byte)i);inventory.add(stack);}var kit=new net.minecraft.nbt.CompoundTag();kit.put("Inventory",inventory);net.minecraft.nbt.NbtIo.writeCompressed(kit,root.resolve("analysis/comparison/port-player-kit.nbt"));
        var result=JsonParser.parseString(Files.readString(root.resolve("analysis/comparison/port-world.json"))).getAsJsonObject();result.addProperty("native_block_entity_save_validation",true);result.addProperty("original_infusions_reloaded",altars);Files.writeString(root.resolve("analysis/comparison/port-world.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    private void floor(int x1,int z1,int x2,int z2){
        for(int z=z1;z<=z2;z++)for(int x=x1;x<=x2;x++)level.setBlock(new BlockPos(x,Y-1,z),Blocks.STONE.defaultBlockState(),2);
    }
    private void sign(int x,int z,String... lines){
        var pos=new BlockPos(x,Y,z);level.setBlock(pos,Blocks.OAK_SIGN.defaultBlockState(),2);
        var sign=(SignBlockEntity)level.getBlockEntity(pos);var text=sign.getFrontText();
        for(int i=0;i<Math.min(4,lines.length);i++)text=text.setMessage(i,Component.literal(lines[i]));sign.setText(text,true);sign.setChanged();
    }
    private void chest(int x,int z,List<ItemStack> items,String name){
        for(int part=0;part<Math.max(1,(items.size()+26)/27);part++){
            var pos=new BlockPos(x+part*2,Y,z);level.setBlock(pos,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH),2);
            var chest=(ChestBlockEntity)level.getBlockEntity(pos);var saved=chest.saveWithoutMetadata(level.registryAccess());saved.putString("CustomName",Component.Serializer.toJson(Component.literal(name),level.registryAccess()));chest.loadWithComponents(saved,level.registryAccess());
            for(int slot=0;slot<27&&part*27+slot<items.size();slot++)chest.setItem(slot,items.get(part*27+slot).copy());chests++;
            check.accept(chest.getContainerSize()==27,"comparison chest is a native container: "+name+" part "+part);
        }
    }
    private PedestalBlockEntity pedestal(BlockPos pos,ItemStack item){
        level.setBlock(pos,R3Content.ARCANE_PEDESTAL.get().defaultBlockState(),2);var pedestal=(PedestalBlockEntity)level.getBlockEntity(pos);
        if(!item.isEmpty())pedestal.setItem(0,item.copyWithCount(1));return pedestal;
    }
    private void jar(BlockPos pos,String aspect,int amount){
        level.setBlock(pos,R3Content.WARDED_JAR.get().defaultBlockState().setValue(ProductionBlock.FACING,Direction.SOUTH),2);
        var jar=(TransportBlockEntity)level.getBlockEntity(pos);var type=Aspect.getAspect(aspect);jar.applyLabel(type);jar.addToContainer(type,amount);
    }
    private void altar(int x,int z,ItemStack central,List<ItemStack> components,Map<String,Integer> aspects){
        var matrixPos=new BlockPos(x,Y+2,z);level.setBlock(matrixPos,R3Content.INFUSION_MATRIX.get().defaultBlockState(),2);pedestal(matrixPos.below(2),central);
        for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1}){
            int orientation=dx<0?(dz<0?2:3):(dz<0?4:5);
            var state=R3Content.INFUSION_PILLAR.get().defaultBlockState().setValue(InfusionPillarBlock.ORIENTATION,orientation);
            level.setBlock(matrixPos.offset(dx,-2,dz),state,2);level.setBlock(matrixPos.offset(dx,-1,dz),state.setValue(InfusionPillarBlock.HALF,DoubleBlockHalf.UPPER),2);
        }
        var ring=manifest.getAsJsonArray("pedestal_ring");check.accept(components.size()<=ring.size(),"infusion components fit comparison altar: "+x+","+z);
        for(int i=0;i<components.size();i++){var offset=ring.get(i).getAsJsonArray();pedestal(new BlockPos(x+offset.get(0).getAsInt(),Y,z+offset.get(1).getAsInt()),components.get(i));}
        var sources=new ArrayList<Map.Entry<String,Integer>>();aspects.forEach((a,n)->{for(int amount=n;amount>0;amount-=64)sources.add(Map.entry(a,Math.min(64,amount)));});
        var perimeter=new ArrayList<BlockPos>();for(int dx=-5;dx<=5;dx++)for(int dz:new int[]{-5,5})perimeter.add(new BlockPos(x+dx,Y,z+dz));for(int dz=-4;dz<=4;dz++)for(int dx:new int[]{-5,5})perimeter.add(new BlockPos(x+dx,Y,z+dz));
        check.accept(sources.size()<=perimeter.size(),"all essentia fits isolated comparison altar");for(int i=0;i<sources.size();i++)jar(perimeter.get(i),sources.get(i).getKey(),sources.get(i).getValue());
        var matrix=(InfusionMatrixBlockEntity)level.getBlockEntity(matrixPos);matrix.active=true;matrix.getSurroundings();matrix.setChanged();
        check.accept(matrix.validLocation(),"comparison altar has an actual valid native multiblock: "+x+","+z);
    }
    private void main(){
        floor(-16,-16,208,160);level.setDefaultSpawnPos(new BlockPos(0,Y,12),180);level.setDayTime(6000);level.setWeatherParameters(0,6000,false,false);
        var rules=level.getGameRules();for(var key:List.of(GameRules.RULE_DAYLIGHT,GameRules.RULE_WEATHER_CYCLE,GameRules.RULE_DOMOBSPAWNING,GameRules.RULE_MOBGRIEFING))rules.getRule(key).set(false,level.getServer());rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true,level.getServer());
        sign(0,10,"TC4 comparison","Port 1.21.1","All recipes east","Creative + cheats");
        var wand=new ItemStack(R2Content.WAND.get());for(var a:Aspects.PRIMALS)thaumcraft.items.WandVis.set(wand,a,2500);
        chest(-4,8,List.of(new ItemStack(R2Content.BOOK.get()),wand,new ItemStack(R2Content.THAUMOMETER.get()),new ItemStack(R2Content.GOGGLES.get()),new ItemStack(R2Content.SCRIBING_TOOLS.get()),new ItemStack(R3Content.RESONATOR.get()),new ItemStack(R3Equipment.GOLEM_BELL.get())),"Starter kit");
        var catalog=BuiltInRegistries.ITEM.entrySet().stream().filter(e->e.getKey().location().getNamespace().equals("thaumcraft")).sorted(Comparator.comparing(e->e.getKey().location().toString())).map(e->new ItemStack(e.getValue())).toList();
        for(int i=0;i<catalog.size();i+=27){int n=i/27,x=8+(n%8)*22,z=96+(n/8)*8;chest(x,z,catalog.subList(i,Math.min(i+27,catalog.size())),"Native catalog "+(i+1));sign(x,z+2,"Native catalog",(i+1)+" - "+Math.min(i+27,catalog.size()));}
        altar(0,0,ItemStack.EMPTY,List.of(),Map.of("ignis",64,"aqua",64));for(var direction:Direction.Plane.HORIZONTAL)pedestal(new BlockPos(0,Y,0).relative(direction,3),ItemStack.EMPTY);
        sign(-6,6,"Infusion altar","Load recipe kit","Wand on matrix");for(int x:new int[]{-6,-4})level.setBlock(new BlockPos(x,Y,18),R3Content.NITOR_BLOCK.get().defaultBlockState(),2);sign(-6,21,"Nitor","Compare day","and night");
        var primals=new LinkedHashMap<String,Integer>();for(var a:Aspects.PRIMALS)primals.put(a.getTag(),50);
        for(int i=0;i<NodeState.Type.values().length;i++){int x=24+i*24,z=24;var p=new BlockPos(x,Y+1,z);level.setBlock(p,R2Content.AURA_NODE.get().defaultBlockState(),2);((AuraNodeBlockEntity)level.getBlockEntity(p)).initialize(primals,NodeState.Type.values()[i],NodeState.Modifier.NONE);sign(x,z+4,"Aura node",NodeState.Type.values()[i].name(),"Goggles / scanner","Wand charge");}
        int i=0;for(var a:Aspect.all().values()){jar(new BlockPos(8+(i%16)*6,Y,64+(i/16)*6),a.getTag(),64);i++;}sign(0,68,"48 essentia jars","Labels / goggles","Phials / transport");
        var table=R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(TableBlock.FACING,Direction.EAST);level.setBlock(new BlockPos(-10,Y,32),table,2);level.setBlock(new BlockPos(-9,Y,32),table.setValue(TableBlock.FACING,Direction.WEST).setValue(TableBlock.SLAVE,true),2);
        ((TableBlockEntity)level.getBlockEntity(new BlockPos(-10,Y,32))).setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));sign(-10,36,"Research table","Notes in catalog");level.setBlock(new BlockPos(-10,Y,40),R2Content.ARCANE_WORKBENCH.get().defaultBlockState(),2);sign(-10,44,"Arcane workbench");
    }
    private ItemStack central(InfusionRecipe recipe){
        if(recipe instanceof thaumcraft.crafting.InfusionRunicRecipe)return new ItemStack(R2Content.GOGGLES.get());
        if(recipe instanceof InfusionEnchantmentRecipeData enchant)return BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(s->s.isDamageableItem()&&enchant.enchantment().value().canEnchant(s)).findFirst().orElseThrow();
        return representative(recipe.input(),"infusion centre");
    }
    private ItemStack representative(net.minecraft.world.item.crafting.Ingredient ingredient,String recipe){
        var choices=ingredient.getItems();
        if(choices.length==0||Arrays.stream(choices).anyMatch(stack->stack.isEmpty()||stack.is(Items.BARRIER)))throw new IllegalStateException("Cannot prepare a comparison fixture with unresolved ingredients: "+recipe);
        return choices[0].copy();
    }
    private void recipe(int x,int z,String label,RecipeHolder<?> holder){
        var recipe=holder.value();var supplies=new ArrayList<ItemStack>();for(var ingredient:recipe.getIngredients())if(ingredient!=net.minecraft.world.item.crafting.Ingredient.EMPTY)supplies.add(representative(ingredient,holder.id().toString()));
        var result=recipe.getResultItem(level.registryAccess());if(!result.isEmpty())supplies.add(result.copy());
        int radius=recipe instanceof InfusionRecipe?7:4;floor(x-radius,z-radius,x+radius,z+radius);sign(x-radius+1,z+radius-1,label,holder.id().getPath());
        if(recipe instanceof InfusionRecipe infusion){
            var central=central(infusion);var components=infusion.components(central).stream().map(g->representative(g,holder.id().toString())).toList();altar(x,z,central,components,infusion.aspects(central));supplies.add(central.copy());
            check.accept(infusion.matches(new thaumcraft.api.crafting.InfusionInput(central,components)),"comparison central item and components match actual native recipe: "+holder.id());infusions++;
        }else if(recipe instanceof thaumcraft.api.crafting.CrucibleRecipe crucibleRecipe){
            var pos=new BlockPos(x,Y,z);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.below(),R3Content.NITOR_BLOCK.get().defaultBlockState());level.setBlockAndUpdate(pos,R3Content.CRUCIBLE.get().defaultBlockState());
            var tile=(CrucibleBlockEntity)level.getBlockEntity(pos);tile.fillWater(1000);tile.heat=200;crucibleRecipe.aspects().forEach((a,n)->tile.addToContainer(Aspect.getAspect(a),n));
            check.accept(crucibleRecipe.matches(tile,representative(crucibleRecipe.catalyst(),holder.id().toString())),"comparison crucible contains the native recipe's exact essentia: "+holder.id());
        }else if(recipe instanceof thaumcraft.api.crafting.IArcaneRecipe){
            var pos=new BlockPos(x,Y,z);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,R2Content.ARCANE_WORKBENCH.get().defaultBlockState());
            var wand=new ItemStack(R2Content.WAND.get());for(var aspect:Aspects.PRIMALS)thaumcraft.items.WandVis.set(wand,aspect,2500);((TableBlockEntity)level.getBlockEntity(pos)).setItem(10,wand);supplies.add(wand.copy());
        }else level.setBlock(new BlockPos(x,Y,z),Blocks.CRAFTING_TABLE.defaultBlockState(),2);
        chest(x-radius+1,z+radius-3,supplies,label+" "+holder.id().getPath());preparedRecipes.add(holder.id());
    }
    private void stations(){
        for(var entry:manifest.getAsJsonArray("stations")){var s=entry.getAsJsonObject();var p=s.getAsJsonArray("position");int x=p.get(0).getAsInt(),z=p.get(2).getAsInt();String label=s.get("id").getAsString();
            var holder=s.get("native_recipe").isJsonNull()?Optional.<RecipeHolder<?>>empty():level.getRecipeManager().byKey(ResourceLocation.parse(s.get("native_recipe").getAsString()));
            if(holder.isPresent())recipe(x,z,label,holder.get());else{floor(x-4,z-4,x+4,z+4);var key=s.get("key").isJsonNull()?label:s.get("key").getAsString();if(s.has("native_conditions")&&!s.getAsJsonArray("native_conditions").isEmpty()){sign(x-3,z+3,label,key,"Missing dependency");unavailable.add(key);}else{sign(x-3,z+3,label,key,"Not resolved yet");missing.add(key);}}
        }
    }
    private void remainingNativeRecipes(){
        var extra=level.getRecipeManager().getRecipes().stream().filter(h->h.id().getNamespace().equals("thaumcraft")&&!preparedRecipes.contains(h.id())).sorted(Comparator.comparing(h->h.id().toString())).toList();
        for(int i=0;i<extra.size();i++)recipe(240+(i%8)*24,600+(i/8)*24,"Native extra "+(i+1),extra.get(i));
    }
    private void knowledge(){
        // Account-independent offline test profiles plus the original comparison profile.
        for(var name:List.of("R3Dev","Dev","APLYT_RTM")){
            var uuid=UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(StandardCharsets.UTF_8));var player=FakePlayerFactory.get(level,new GameProfile(uuid,name));KnowledgeService.initialize(player);
            KnowledgeService.mutate(player,k->{for(var key:ThaumcraftData.get(level.getServer()).research().keySet())k.completeResearch(key);for(var aspect:Aspect.all().values())k.setPool(aspect.getTag(),500);return true;});
            // Research completion adds its normal warp after the mutation. Clear it
            // separately so this generated QA profile starts without random events.
            KnowledgeService.mutate(player,k->{k.setWarp(0,0,0,0);return true;});
            check.accept(KnowledgeService.get(player).totalWarp()==0,"generated comparison profile starts without warp: "+name);
        }
    }
}

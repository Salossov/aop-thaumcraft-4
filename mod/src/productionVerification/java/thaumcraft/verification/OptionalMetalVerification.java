package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.crafting.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.*;
import thaumcraft.registry.*;

/** An isolated data pack stands in for foreign metals; no ores or ingots are added to production. */
final class OptionalMetalVerification {
    private static final BlockPos POS=new BlockPos(2500,140,32);
    private static final List<String> METALS=List.of("tin","silver","lead");
    private static ResourceLocation id(String value){return ResourceLocation.parse(value);}
    static void prepare(ServerLevel l){for(int x=-1;x<=2;x++)for(int z=-1;z<=1;z++){int cx=(POS.getX()>>4)+x,cz=(POS.getZ()>>4)+z;l.setChunkForced(cx,cz,true);l.getChunk(cx,cz);}}
    static boolean ready(ServerLevel l){for(int x=-1;x<=2;x++)for(int z=-1;z<=1;z++)if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong((POS.getX()>>4)+x,(POS.getZ()>>4)+z)))return false;return true;}
    static void release(ServerLevel l){for(int x=-1;x<=2;x++)for(int z=-1;z<=1;z++)l.setChunkForced((POS.getX()>>4)+x,(POS.getZ()>>4)+z,false);}
    static void run(ServerLevel l,String phase,BiConsumer<Boolean,String> check){
        boolean available=!phase.equals("optional-absent"),ores=phase.equals("optional-metals");
        for(var entity:l.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(40)))entity.discard();
        var data=ThaumcraftData.get(l.getServer());
        var split=l.getRecipeManager().byKey(id("minecraft:iron_nugget")).orElseThrow().value();var ironGrid=CraftingInput.of(1,1,List.of(new ItemStack(Items.IRON_INGOT)));@SuppressWarnings("unchecked") var splitRecipe=(Recipe<CraftingInput>)split;check.accept(splitRecipe.matches(ironGrid,l)&&splitRecipe.assemble(ironGrid,l.registryAccess()).is(Items.IRON_NUGGET)&&splitRecipe.assemble(ironGrid,l.registryAccess()).getCount()==9,"original iron-to-nine-nuggets conversion is fulfilled by native Minecraft recipe");
        var ironCombine=(ShapedRecipe)l.getRecipeManager().byKey(id("minecraft:iron_ingot_from_nuggets")).orElseThrow().value();var ironNuggets=CraftingInput.of(3,3,java.util.stream.IntStream.range(0,9).mapToObj(i->new ItemStack(Items.IRON_NUGGET)).toList());check.accept(ironCombine.matches(ironNuggets,l)&&ItemStack.matches(ironCombine.assemble(ironNuggets,l.registryAccess()),new ItemStack(Items.IRON_INGOT)),"original nine-iron-nuggets conversion is fulfilled by native Minecraft recipe");
        var synced=new net.minecraft.nbt.CompoundTag();thaumcraft.data.ResearchDataSync.encode(synced,data.research());var definitions=thaumcraft.data.ResearchDataSync.decode(synced);
        for(var metal:METALS){
            var nugget=R3Equipment.OPTIONAL_NUGGETS.get(metal).get();var cluster=R3Equipment.NATIVE_CLUSTERS.get(metal).get();
            check.accept(TagOutput.present(l.registryAccess(),"c:ingots/"+metal)==available,"fixture ingot discovery: "+metal);
            check.accept(TagOutput.present(l.registryAccess(),"c:ores/"+metal)==ores,"fixture ore discovery: "+metal);
            check.accept(definitions.containsKey("TRANS"+metal.toUpperCase(Locale.ROOT))==available,"transmutation visibility survives native knowledge sync: "+metal);
            check.accept(definitions.containsKey("PURE"+metal.toUpperCase(Locale.ROOT))==ores,"purification requires both an ingot and an ore in knowledge sync: "+metal);
            check.accept(R3Equipment.visible(nugget)==available&&R3Equipment.visible(cluster)==available,"creative visibility follows original ingot discovery: "+metal);
            for(var recipe:List.of("pure"+metal,"trans"+metal,metal+"_nuggets",metal+"_from_nuggets","native_"+metal+"_smelting","infernal_"+metal))check.accept(l.getRecipeManager().byKey(id("thaumcraft:"+recipe)).isPresent()==available,"native recipe condition follows ingot discovery: "+recipe);
            if(!available){check.accept(ThaumcraftData.aspects(l.getServer(),new ItemStack(cluster)).isEmpty(),"absent foreign ingot does not register cluster aspects: "+metal);continue;}
            if(ores)mining(l,metal,check);
            var extra=switch(metal){case "tin"->"vitreus";case "silver"->"lucrum";default->"ordo";};
            check.accept(ThaumcraftData.aspects(l.getServer(),new ItemStack(nugget)).equals(Map.of("metallum",1)),"foreign nugget tag receives original metal aspect: "+metal);
            check.accept(ThaumcraftData.aspects(l.getServer(),new ItemStack(cluster)).equals(extra.equals("ordo")?Map.of("ordo",3,"metallum",5,"terra",1):Map.of("ordo",1,"metallum",5,"terra",1,extra,2)),"discovered cluster receives exact original explicit aspects: "+metal);
            var ingot=TagOutput.first(l.registryAccess(),id("c:ingots/"+metal),1);
            check.accept(ThaumcraftData.aspects(l.getServer(),ingot).equals(Map.of("metallum",3,extra,1)),"foreign ingot resolves original tagged aspects: "+metal);
            var unpack=(ShapelessRecipe)l.getRecipeManager().byKey(id("thaumcraft:"+metal+"_nuggets")).orElseThrow().value();
            for(var alternative:unpack.getIngredients().getFirst().getItems()){var grid=CraftingInput.of(1,1,List.of(alternative));var result=unpack.assemble(grid,l.registryAccess());check.accept(unpack.matches(grid,l)&&result.is(nugget)&&result.getCount()==9,"every discovered ingot variant produces nine own nuggets: "+metal+" "+alternative);}
            var combine=(TagShapedRecipe)l.getRecipeManager().byKey(id("thaumcraft:"+metal+"_from_nuggets")).orElseThrow().value();
            var grid=CraftingInput.of(3,3,java.util.stream.IntStream.range(0,9).mapToObj(i->new ItemStack(nugget)).toList());
            check.accept(combine.matches(grid,l)&&ItemStack.matches(combine.assemble(grid,l.registryAccess()),ingot),"nine own nuggets return first discovered ingot: "+metal);
            var smelt=(TagSmeltingRecipe)l.getRecipeManager().byKey(id("thaumcraft:native_"+metal+"_smelting")).orElseThrow().value();
            var input=new SingleRecipeInput(new ItemStack(cluster));check.accept(smelt.matches(input,l)&&smelt.getType()==RecipeType.SMELTING&&ItemStack.matches(smelt.assemble(input,l.registryAccess()),ingot.copyWithCount(2))&&smelt.getExperience()==1&&smelt.getCookingTime()==200,"cluster is an actual native two-ingot smelting recipe with original XP: "+metal);
            var p=POS.offset(METALS.indexOf(metal)*12,0,0);l.getChunkAt(p);l.setBlockAndUpdate(p,Blocks.FURNACE.defaultBlockState());var furnace=(FurnaceBlockEntity)l.getBlockEntity(p);furnace.clearContent();furnace.setItem(0,new ItemStack(cluster));furnace.setItem(1,new ItemStack(Items.COAL));for(int t=0;t<210;t++)AbstractFurnaceBlockEntity.serverTick(l,p,l.getBlockState(p),furnace);
            check.accept(furnace.getItem(0).isEmpty()&&ItemStack.matches(furnace.getItem(2),ingot.copyWithCount(2)),"ordinary vanilla furnace actually smelts the tagged cluster: "+metal);
            var ip=p.north(4);l.getChunkAt(ip);l.setBlockAndUpdate(ip,R3Content.INFERNAL_FURNACE.get().defaultBlockState().setValue(InfernalFurnaceBlock.PART,13));var infernal=(InfernalFurnaceBlockEntity)l.getBlockEntity(ip);infernal.items.clearContent();infernal.items.setItem(0,new ItemStack(cluster));infernal.cookTime=1;infernal.maxCookTime=140;InfernalFurnaceBlockEntity.tick(l,ip,l.getBlockState(ip),infernal);var output=l.getEntitiesOfClass(ItemEntity.class,new AABB(ip).inflate(3),e->e.getItem().is(ingot.getItem()));check.accept(infernal.items.getItem(0).isEmpty()&&output.stream().mapToInt(e->e.getItem().getCount()).sum()==2,"infernal furnace actually consumes cluster and ejects two foreign ingots: "+metal);for(var entity:l.getEntitiesOfClass(ItemEntity.class,new AABB(ip).inflate(3)))entity.discard();
            var pure=(CrucibleRecipeData)l.getRecipeManager().byKey(id("thaumcraft:pure"+metal)).orElseThrow().value();var trans=(CrucibleRecipeData)l.getRecipeManager().byKey(id("thaumcraft:trans"+metal)).orElseThrow().value();
            var raw=TagOutput.first(l.registryAccess(),id("c:raw_materials/"+metal),1);var cp=p.south(4);l.setBlockAndUpdate(cp,R3Content.CRUCIBLE.get().defaultBlockState());var crucible=(CrucibleBlockEntity)l.getBlockEntity(cp);crucible.empty();crucible.heat=200;crucible.fillWater(1000);var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("7775fe4a-0d76-4657-8754-f7c39262fbd4"),"TC4_Metals"));KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->{k.completeResearch(pure.research());k.completeResearch(trans.research());return true;});
            boolean prior=ThaumcraftConfig.RAW_ORE_PURIFICATION.get();try{ThaumcraftConfig.RAW_ORE_PURIFICATION.set(true);crucible.store.setAspects(aspects(pure));check.accept(pure.matches(crucible,raw),"approved raw purification matches tagged foreign raw metal: "+metal);ThaumcraftConfig.RAW_ORE_PURIFICATION.set(false);check.accept(!pure.matches(crucible,raw),"live compatibility toggle rejects foreign raw catalyst: "+metal);if(ores){var ore=TagOutput.first(l.registryAccess(),id("c:ores/"+metal),1);check.accept(pure.matches(crucible,ore),"ore catalyst remains supported with raw purification disabled: "+metal);}ThaumcraftConfig.RAW_ORE_PURIFICATION.set(true);alchemy(l,crucible,player,raw,cluster,1,check,metal+" purification");crucible.store.setAspects(aspects(trans));alchemy(l,crucible,player,new ItemStack(nugget),nugget,3,check,metal+" transmutation");}finally{ThaumcraftConfig.RAW_ORE_PURIFICATION.set(prior);}
        }
        check.accept(definitions.containsKey("CAP_silver")==available,"silver wand-cap research is hidden when no silver ingot exists");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),l.registryAccess());try{var codec=net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket.STREAM_CODEC;var loaded=l.getRecipeManager().getRecipes();codec.encode(buffer,new net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket(loaded));var received=codec.decode(buffer).getRecipes();check.accept(received.size()==loaded.size()&&!buffer.isReadable(),"all native recipes survive actual multiplayer recipe synchronization");for(var h:received)if(h.value() instanceof TagSmeltingRecipe||h.value() instanceof TagShapedRecipe)check.accept(ItemStack.matches(h.value().getResultItem(l.registryAccess()),l.getRecipeManager().byKey(h.id()).orElseThrow().value().getResultItem(l.registryAccess())),"decoded tag-output recipe preserves original foreign result: "+h.id());}finally{buffer.release();}
        var stash=POS.west(4);l.getChunkAt(stash);if(phase.equals("optional-metals")){l.setBlockAndUpdate(stash,Blocks.CHEST.defaultBlockState());var chest=(ChestBlockEntity)l.getBlockEntity(stash);for(int i=0;i<3;i++){chest.setItem(i,new ItemStack(R3Equipment.OPTIONAL_NUGGETS.get(METALS.get(i)).get(),7));chest.setItem(i+3,new ItemStack(R3Equipment.NATIVE_CLUSTERS.get(METALS.get(i)).get(),2));}}else{var chest=(ChestBlockEntity)l.getBlockEntity(stash);check.accept(chest!=null,"comparison fixture survives real world restart");for(int i=0;i<3;i++)check.accept(chest.getItem(i).is(R3Equipment.OPTIONAL_NUGGETS.get(METALS.get(i)).get())&&chest.getItem(i).getCount()==7&&chest.getItem(i+3).is(R3Equipment.NATIVE_CLUSTERS.get(METALS.get(i)).get())&&chest.getItem(i+3).getCount()==2,"removing foreign tags preserves previously obtained own items: "+METALS.get(i));}
    }
    private static thaumcraft.api.aspects.AspectList aspects(CrucibleRecipeData recipe){var list=new thaumcraft.api.aspects.AspectList();recipe.aspects().forEach((tag,n)->list.add(thaumcraft.api.aspects.Aspect.getAspect(tag),n));return list;}
    private static void mining(ServerLevel l,String metal,BiConsumer<Boolean,String> check){
        var ore=TagOutput.first(l.registryAccess(),id("c:ores/"+metal),1);var raw=TagOutput.first(l.registryAccess(),id("c:raw_materials/"+metal),1);var block=((BlockItem)ore.getItem()).getBlock();var pick=new ItemStack(R3Equipment.TOOLS.get("elemental_pickaxe").get());var enchantments=l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),11);boolean prior=ThaumcraftConfig.RAW_ORE_PURIFICATION.get();
        try{for(boolean enabled:List.of(true,false)){ThaumcraftConfig.RAW_ORE_PURIFICATION.set(enabled);for(var source:List.of(raw,ore)){var drop=new ItemEntity(l,POS.getX(),POS.getY(),POS.getZ(),source.copyWithCount(3));var event=new net.neoforged.neoforge.event.level.BlockDropsEvent(l,POS,block.defaultBlockState(),null,new ArrayList<>(List.of(drop)),null,pick);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);boolean replaced=source==ore||enabled;check.accept(drop.getItem().is(replaced?R3Equipment.NATIVE_CLUSTERS.get(metal).get():source.getItem())&&drop.getItem().getCount()==3,"registered native block-drops event preserves quantity and raw toggle: "+metal+" raw="+(source==raw)+" enabled="+enabled);}}
            ThaumcraftConfig.RAW_ORE_PURIFICATION.set(true);pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);var drop=new ItemEntity(l,POS.getX(),POS.getY(),POS.getZ(),ore.copy());net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockDropsEvent(l,POS,block.defaultBlockState(),null,new ArrayList<>(List.of(drop)),null,pick));check.accept(ItemStack.matches(drop.getItem(),ore),"native silk-touch ore drop never becomes a cluster: "+metal);
        }finally{ThaumcraftConfig.RAW_ORE_PURIFICATION.set(prior);}
    }
    private static void alchemy(ServerLevel l,CrucibleBlockEntity crucible,net.minecraft.server.level.ServerPlayer player,ItemStack catalyst,Item result,int count,BiConsumer<Boolean,String> check,String description){var p=crucible.getBlockPos();int water=crucible.water();var thrown=new ItemEntity(l,p.getX()+.5,p.getY()+.7,p.getZ()+.5,catalyst.copy());thrown.setThrower(player);crucible.smelt(thrown);var products=l.getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(2),e->e.getItem().is(result)&&e.getPersistentData().getBoolean("TC4AlchemyProduct"));check.accept(thrown.isRemoved()&&crucible.water()==water-50&&crucible.store.amount()==0&&products.stream().mapToInt(e->e.getItem().getCount()).sum()==count,"real crucible spends exact catalyst, essentia and water without duplicate output: "+description);for(var e:products)e.discard();}
}

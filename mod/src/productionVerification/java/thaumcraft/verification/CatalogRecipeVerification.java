package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.CrucibleBlockEntity;
import thaumcraft.registry.R3Content;

/** Executes original-catalog crucible recipes against their actual server block entity. */
final class CatalogRecipeVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws java.io.IOException{
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));
        var original=JsonParser.parseString(Files.readString(root.resolve("analysis/thaumcraft4-4.2.3.5/r0/catalog/recipes.json"))).getAsJsonObject().getAsJsonArray("registrations");
        var aspectNames=new HashMap<String,String>();
        for(var entry:JsonParser.parseString(Files.readString(root.resolve("analysis/thaumcraft4-4.2.3.5/r0/catalog/aspects.json"))).getAsJsonArray()){
            var aspect=entry.getAsJsonObject();aspectNames.put("Aspect."+aspect.get("constant").getAsString(),aspect.get("tag").getAsString());
        }
        var targets=Map.of("AltGunpowder",Items.GUNPOWDER,"AltSlime",Items.SLIME_BALL,"AltClay",Items.CLAY_BALL,"AltGlowstone",Items.GLOWSTONE_DUST,"AltInk",Items.INK_SAC,"AltWeb",Items.COBWEB,"AltMossyCobble",Items.MOSSY_COBBLESTONE,"AltIce",Items.ICE,"AltCrackedBrick",Items.CRACKED_STONE_BRICKS,"AltBonemeal",Items.BONE_MEAL);
        var catalysts=Map.of("AltGunpowder",Items.GUNPOWDER,"AltSlime",Items.SLIME_BALL,"AltClay",Items.CLAY_BALL,"AltGlowstone",Items.GLOWSTONE_DUST,"AltInk",Items.INK_SAC,"AltWeb",Items.STRING,"AltMossyCobble",Items.COBBLESTONE,"AltIce",Items.SNOW_BLOCK,"AltCrackedBrick",Items.STONE_BRICKS,"AltBonemeal",Items.BONE);
        var expectedTargets=new HashMap<String,Item>(targets);expectedTargets.put("BathSalts",thaumcraft.registry.R4Content.BATH_SALTS.get());expectedTargets.put("SaneSoap",thaumcraft.registry.R4Content.SANITY_SOAP.get());expectedTargets.put("LiquidDeath",thaumcraft.registry.R4Content.DEATH_BUCKET.get());expectedTargets.put("EtherealBloom",thaumcraft.registry.R4Content.BLOOM_ITEM.get());
        var expectedCatalysts=new HashMap<String,Item>(catalysts);expectedCatalysts.put("BathSalts",thaumcraft.registry.R3Equipment.SALIS_MUNDUS.get());expectedCatalysts.put("SaneSoap",R3Content.TALLOW_BLOCK_ITEM.get());expectedCatalysts.put("LiquidDeath",Items.BUCKET);expectedCatalysts.put("EtherealBloom",R3Content.SHIMMERLEAF_ITEM.get());
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("01234567-aaaa-bbbb-cccc-012345678901"),"CatalogQA"));KnowledgeService.initialize(player);
        KnowledgeService.mutate(player,k->{k.completeResearch("ALCHEMICALDUPLICATION");k.completeResearch("ENTROPICPROCESSING");return true;});int count=0;
        for(var entry:original){
            var source=entry.getAsJsonObject();if(!source.has("page_key")||source.get("page_key").isJsonNull())continue;var key=source.get("page_key").getAsString();if(!expectedTargets.containsKey(key))continue;
            var holder=level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("thaumcraft",key.toLowerCase(Locale.ROOT))).orElseThrow();
            check.accept(holder.value() instanceof CrucibleRecipe,"catalog recipe loads through the native manager: "+key);var recipe=(CrucibleRecipe)holder.value();
            var arguments=source.getAsJsonArray("normalized_arguments");var amounts=new LinkedHashMap<String,Integer>();
            arguments.get(3).getAsJsonObject().getAsJsonObject("quantities").entrySet().forEach(e->amounts.put(aspectNames.get(e.getKey()),e.getValue().getAsInt()));
            check.accept(recipe.aspects().equals(amounts),"recipe essentia matches the TC4 catalog: "+key);
            check.accept(ThaumcraftData.get(level.getServer()).research().containsKey(recipe.research()),"recipe research exists: "+key);
            KnowledgeService.mutate(player,k->{k.completeResearch(recipe.research());return true;});
            int amount=arguments.get(1).getAsJsonObject().get("count").getAsInt();
            check.accept(recipe.getRecipeOutput().is(expectedTargets.get(key))&&recipe.getRecipeOutput().getCount()==amount,"recipe result matches the TC4 catalog: "+key);
            var pos=new BlockPos(920+count*4,110,0);level.getChunkAt(pos);level.removeBlock(pos,false);level.setBlockAndUpdate(pos,R3Content.CRUCIBLE.get().defaultBlockState());level.setBlockAndUpdate(pos.below(),Blocks.FIRE.defaultBlockState());
            var crucible=(CrucibleBlockEntity)level.getBlockEntity(pos);crucible.fillWater(1000);crucible.heat=200;amounts.forEach((a,n)->crucible.store.addToContainer(Aspect.getAspect(a),n));
            var input=new ItemEntity(level,pos.getX()+.5,pos.getY()+.7,.5,new ItemStack(expectedCatalysts.get(key)));input.setThrower(player);crucible.smelt(input);
            var products=level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),e->e.getPersistentData().getBoolean("TC4AlchemyProduct"));
            check.accept(input.isRemoved()&&crucible.water()==950&&crucible.store.amount()==0,"real crucible consumes the catalyst, exact essentia and 50 mB: "+key);
            check.accept(products.size()==1&&products.getFirst().getItem().is(expectedTargets.get(key))&&products.getFirst().getItem().getCount()==amount,"real crucible produces the catalog output exactly once: "+key);products.forEach(ItemEntity::discard);count++;
        }
        check.accept(count==14,"all fourteen duplication, entropic and cleansing recipes craft in the server world");
        LabelRecipeVerification.run(level,check);
    }
}

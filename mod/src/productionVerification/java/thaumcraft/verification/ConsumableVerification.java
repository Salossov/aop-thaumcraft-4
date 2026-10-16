package thaumcraft.verification;
import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.registry.*;
import thaumcraft.items.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.data.ThaumcraftData;

final class ConsumableVerification {
    private static final BlockPos POS=new BlockPos(1488,120,0);
    static void run(ServerLevel l,BiConsumer<Boolean,String> check){
        l.getChunkAt(POS);var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678905"),"ConsumablesQA"));player.moveTo(POS.getX()+.5,POS.getY()+1,.5,0,0);player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for(var id:List.of("triple_meat_treat_r0270","triple_meat_treat_r0271","triple_meat_treat_r0272","triple_meat_treat_r0273")){var recipe=(ShapelessRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:"+id)).orElseThrow().value();var grid=CraftingInput.of(2,2,recipe.getIngredients().stream().map(g->g.getItems()[0].copy()).toList());check.accept(recipe.matches(grid,l)&&recipe.assemble(grid,l.registryAccess()).is(R4Content.TRIPLE_MEAT_TREAT.get()),"original sugar plus three distinct meat nuggets craft treat: "+id);}
        var food=new ItemStack(R4Content.TRIPLE_MEAT_TREAT.get(),2);var properties=food.getFoodProperties(player);check.accept(properties.nutrition()==6&&properties.saturation()==9.6f&&properties.canAlwaysEat(),"meat treat retains six hunger, 9.6 saturation and consumption while full");
        player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);var remaining=food.finishUsingItem(l,player);check.accept(remaining.getCount()==1&&player.getFoodData().getFoodLevel()==16&&Math.abs(player.getFoodData().getSaturationLevel()-9.6f)<.0001,"eating consumes one treat and applies its actual hunger/saturation values");
        int regenerations=0;for(int i=0;i<100;i++){player.removeEffect(MobEffects.REGENERATION);new ItemStack(R4Content.TRIPLE_MEAT_TREAT.get()).finishUsingItem(l,player);if(player.hasEffect(MobEffects.REGENERATION)){regenerations++;check.accept(player.getEffect(MobEffects.REGENERATION).getDuration()==100&&player.getEffect(MobEffects.REGENERATION).getAmplifier()==0,"successful treat effect is five seconds of Regeneration I");}}
        check.accept(regenerations>0&&regenerations<100,"native consumption can both grant and omit the probabilistic regeneration effect");
        var aspects=ThaumcraftData.aspects(l.getServer(),new ItemStack(R4Content.TRIPLE_MEAT_TREAT.get()));check.accept(aspects.getOrDefault("sano",0)>=1&&!aspects.containsKey("fames"),"derived treat aspects add healing and remove hunger as in original complex object tags");
        for(var item:R3Equipment.ARMOR.values())if(item.get() instanceof ThaumicArmorItem armor&&armor.kind()==ThaumicArmorItem.Kind.ROBE){
            var recipe=(thaumcraft.crafting.RobeDyeRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:"+(armor.kind()==ThaumicArmorItem.Kind.ROBE?"robe_dye":"void_robe_dye"))).orElseThrow().value();var source=new ItemStack(armor);source.setDamageValue(17);var grid=CraftingInput.of(2,1,List.of(source,new ItemStack(Items.RED_DYE)));var dyed=recipe.assemble(grid,l.registryAccess());
            check.accept(recipe.matches(grid,l)&&dyed.is(armor)&&dyed.getCount()==1&&dyed.getDamageValue()==17&&(DyedItemColor.getOrDefault(dyed,0)&0xffffff)==0x8c3960&&(DyedItemColor.getOrDefault(source,0)&0xffffff)==6961280,"original red mix preserves durability and input while producing reference colour on "+armor);
            check.accept(!recipe.matches(CraftingInput.of(3,1,List.of(source,source.copy(),new ItemStack(Items.RED_DYE))),l)&&!recipe.matches(CraftingInput.of(2,1,List.of(source,new ItemStack(Items.STICK))),l),"robe dye recipe rejects multiple garments and non-dye contaminants");
            l.setBlockAndUpdate(POS,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,1));player.setItemInHand(InteractionHand.MAIN_HAND,dyed);var context=new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(POS.getCenter(),Direction.UP,POS,false));
            check.accept(armor.onItemUseFirst(dyed,context).consumesAction()&&l.getBlockState(POS).is(Blocks.CAULDRON)&&(DyedItemColor.getOrDefault(dyed,6961280)&0xffffff)==6961280&&dyed.getDamageValue()==17,"washing restores original robe colour, consumes the last water level and preserves damage");
        }
        check.accept(l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:void_robe_dye")).isEmpty(),"fixed-purple void robe has no registered dye crafting recipe");
        player.tickCount=1;
        for(var item:R3Equipment.ARMOR.values())if(item.get() instanceof ThaumicArmorItem armor&&armor.kind()==ThaumicArmorItem.Kind.VOID_ROBE){
            var source=new ItemStack(armor);source.setDamageValue(17);source.set(DataComponents.DYED_COLOR,new DyedItemColor(0xff0000,true));armor.inventoryTick(source,l,player,0,false);
            check.accept(!source.has(DataComponents.DYED_COLOR)&&source.getDamageValue()==17,"void robe clears legacy dye without changing durability: "+armor);
            var disabled=new thaumcraft.crafting.RobeDyeRecipe(CraftingBookCategory.EQUIPMENT,true);var dyeInput=CraftingInput.of(2,1,List.of(source,new ItemStack(Items.RED_DYE)));
            check.accept(!disabled.matches(dyeInput,l)&&disabled.assemble(dyeInput,l.registryAccess()).isEmpty(),"legacy void dye serializer cannot consume a fixed-colour robe: "+armor);
        }
        var fragments=(ShapedRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:knowfrag")).orElseThrow().value();var grid=CraftingInput.of(3,3,Collections.nCopies(9,new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get())));var unknown=fragments.assemble(grid,l.registryAccess());
        check.accept(fragments.matches(grid,l)&&unknown.is(R2Content.NOTES.get())&&UnknownResearchNotes.isUnknown(unknown)&&ResearchNotes.read(unknown)==null,"nine fragments produce marked unknown research notes through the actual native component codec");
        var loaded=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)unknown.save(l.registryAccess()));check.accept(UnknownResearchNotes.isUnknown(loaded),"unknown research-note state survives item NBT round trip");
        var data=ThaumcraftData.get(l.getServer()).research();var hidden=data.values().stream().filter(d->d.hidden()&&!d.aspects().isEmpty()&&(!d.itemTriggers().isEmpty()||!d.entityTriggers().isEmpty()||!d.aspectTriggers().isEmpty())).findFirst().orElseThrow();
        var fresh=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"HiddenNotesQA"));fresh.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);KnowledgeService.mutate(fresh,k->{hidden.parents().forEach(k::completeResearch);hidden.hiddenParents().forEach(k::completeResearch);return true;});var eligible=UnknownResearchNotes.candidates(data.values(),KnowledgeService.get(fresh));check.accept(!eligible.isEmpty(),"hidden-note candidates require original prerequisites and at least one discovery trigger");
        fresh.setItemInHand(InteractionHand.MAIN_HAND,loaded);R2Content.NOTES.get().use(l,fresh,InteractionHand.MAIN_HAND);var note=ResearchNotes.read(loaded);check.accept(note!=null&&!note.complete&&!UnknownResearchNotes.isUnknown(loaded)&&eligible.stream().anyMatch(d->d.key().equals(note.key)),"opening unknown notes generates a puzzle for an eligible hidden research without completing it");
        note.complete=true;ResearchNotes.write(loaded,note);R2Content.NOTES.get().use(l,fresh,InteractionHand.MAIN_HAND);check.accept(loaded.isEmpty()&&KnowledgeService.get(fresh).completedResearch().contains(note.key),"solved hidden note completes from its prerequisites without requiring a separate discovery marker");
        KnowledgeService.mutate(player,k->{data.keySet().forEach(k::completeResearch);return true;});l.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(4)).forEach(ItemEntity::discard);player.setItemInHand(InteractionHand.MAIN_HAND,unknown);R2Content.NOTES.get().use(l,player,InteractionHand.MAIN_HAND);var refunds=l.getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(4));
        check.accept(unknown.isEmpty()&&refunds.size()==1&&refunds.getFirst().getItem().is(R3Equipment.KNOWLEDGE_FRAGMENT.get())&&refunds.getFirst().getItem().getCount()>=7&&refunds.getFirst().getItem().getCount()<=9,"exhausted hidden research consumes the note and refunds seven to nine fragments");
        var plant=R4Content.CINDERPEARL.get();l.setBlockAndUpdate(POS,Blocks.SAND.defaultBlockState());l.setBlockAndUpdate(POS.above(),plant.defaultBlockState());
        check.accept(l.getBlockState(POS.above()).canSurvive(l,POS.above())&&l.getBlockState(POS.above()).getLightEmission(l,POS.above())==8,"cinderpearl survives desert sand and retains original light level eight");
        var plantDrops=Block.getDrops(l.getBlockState(POS.above()),l,POS.above(),null);check.accept(plantDrops.size()==1&&plantDrops.getFirst().is(plant.asItem()),"harvesting cinderpearl yields its native plant item");
        l.setBlockAndUpdate(POS,Blocks.AIR.defaultBlockState());check.accept(l.getBlockState(POS.above()).isAir(),"unsupported cinderpearl removes itself through native neighbor updates");
        var powder=(ShapedRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:cinderpearl_blaze_powder")).orElseThrow().value();var plantGrid=CraftingInput.of(1,1,List.of(new ItemStack(plant)));check.accept(powder.matches(plantGrid,l)&&powder.assemble(plantGrid,l.registryAccess()).is(Items.BLAZE_POWDER)&&powder.assemble(plantGrid,l.registryAccess()).getCount()==1,"one cinderpearl grinds into one vanilla blaze powder");
        var patch=POS.offset(0,0,16);for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++){var p=patch.offset(x,0,z);l.getChunkAt(p);l.setBlockAndUpdate(p,Blocks.SAND.defaultBlockState());for(int y=1;y<=4;y++)l.setBlockAndUpdate(p.above(y),Blocks.AIR.defaultBlockState());}var obstruction=patch.above();l.setBlockAndUpdate(obstruction,Blocks.STONE.defaultBlockState());
        thaumcraft.world.LegacyCinderpearlFlowers.place(l,net.minecraft.util.RandomSource.create(4235),patch.above());int count=0;for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)if(l.getBlockState(patch.offset(x,1,z)).is(plant))count++;
        check.accept(count>0&&count<=18&&l.getBlockState(obstruction).is(Blocks.STONE),"original eighteen-attempt flower scatter creates supported plants while preserving occupied positions");
    }
}

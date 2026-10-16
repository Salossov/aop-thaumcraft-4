package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.api.aspects.*;
import thaumcraft.api.crafting.*;
import thaumcraft.api.wands.*;
import thaumcraft.crafting.*;
import thaumcraft.entities.*;
import thaumcraft.items.*;
import thaumcraft.knowledge.*;
import thaumcraft.production.*;
import thaumcraft.registry.*;

/** Runs against Minecraft's real registry, recipe manager, loot and world data. */
final class EquipmentVerification {
    static final BlockPos ORIGIN = new BlockPos(320,160,0);
    static final UUID OWNER = UUID.fromString("727dd0e4-ec13-4a37-9001-c153f36d2b9b");
    static void run(ServerLevel level, BiConsumer<Boolean,String> check) throws Exception {
        recipes(level,check);
        var player=new net.neoforged.neoforge.common.util.FakePlayer(level,new GameProfile(OWNER,"R3_Equipment_Test")){
            @Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source){return false;}
        };
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.moveTo(ORIGIN.getCenter());
        KnowledgeService.initialize(player);
        for(var kind:FocusProjectile.Kind.values()){
            var projectile=R3Entities.FOCUS_PROJECTILE.get().create(level).configure(kind,2,1);
            projectile.setOwner(player);projectile.setPos(ORIGIN.getX()+.5,ORIGIN.getY()+2,ORIGIN.getZ()+.5);level.addFreshEntity(projectile);projectile.tick();
            var tag=new CompoundTag();projectile.addAdditionalSaveData(tag);var copy=R3Entities.FOCUS_PROJECTILE.get().create(level);copy.readAdditionalSaveData(tag);
            check.accept(copy.kind()==kind&&copy.damage()==2&&copy.flag(1)&&!copy.getItem().isEmpty(),kind+": native projectile constructs, ticks and preserves casting data through NBT");projectile.discard();
        }
        var primal=R3Entities.PRIMAL_ORB.get().create(level);
        check.accept(primal.kind()==FocusProjectile.Kind.PRIMAL&&thaumcraft.data.ThaumcraftData.aspects(level.getServer(),primal).equals(Map.of("aer",5,"perditio",10,"praecantatio",10,"potentia",10)),"primal orb uses its distinct scan identity and exact original entity aspects");
        check.accept(thaumcraft.data.ThaumcraftData.get(level.getServer()).research().get("ROD_primal_staff").entityTriggers().contains(thaumcraft.data.ThaumcraftData.entityScanKey(level.getServer(),primal)),"native primal orb scan identity resolves the primal staff research trigger");
        check.accept(!thaumcraft.data.ThaumcraftData.aspects(level.getServer(),new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get())).isEmpty(),"knowledge fragment retains its original scan aspects");
        var wand=new ItemStack(R3Equipment.WAND.get());
        wand.set(WandComponents.PARTS.get(),new WandParts("silverwood","thaumium"));
        WandVis.set(wand,Aspects.FIRE,10000);WandVis.set(wand,Aspects.WATER,10000);
        check.accept(WandVis.capacity(wand)==10000,"silverwood wand retains original 100 vis capacity");
        var restored=ItemStack.parseOptional(level.registryAccess(),(CompoundTag)wand.save(level.registryAccess()));
        check.accept(ItemStack.matches(wand,restored),"rod, cap and current centivis survive native item serialization");
        int fire=WandVis.get(wand,Aspects.FIRE);
        check.accept(!WandVis.consume(wand,Map.of("ignis",1,"terra",1),0,true)&&WandVis.get(wand,Aspects.FIRE)==fire,"multi-aspect payment is atomic when another primal is missing");
        check.accept(WandVis.consume(wand,Map.of("ignis",1),0,true)&&WandVis.get(wand,Aspects.FIRE)==fire-90,"thaumium cap applies original ten percent cost reduction");
        var staff=new ItemStack(R3Equipment.STAFF.get());staff.set(WandComponents.PARTS.get(),new WandParts("silverwood_staff","void"));
        check.accept(WandVis.capacity(staff)==25000,"silverwood staff retains original 250 vis capacity");
        var sceptre=new ItemStack(R3Equipment.SCEPTRE.get());sceptre.set(WandComponents.PARTS.get(),new WandParts("silverwood","gold"));
        check.accept(WandVis.capacity(sceptre)==15000&&!WandFocus.supports(sceptre),"sceptre has increased capacity and rejects casting foci");
        player.setItemInHand(InteractionHand.MAIN_HAND,wand);
        for(var entry:R3Equipment.FOCI.entrySet()){
            var focus=new ItemStack(entry.getValue().get());var item=(ItemFocusBasic)focus.getItem();
            check.accept(!item.applyUpgrade(focus,FocusUpgradeType.potency,2),entry.getKey()+": out-of-order focus upgrade is rejected");
            for(int rank=1;rank<=5;rank++){
                var choices=item.getPossibleUpgradesByRank(focus,rank);
                check.accept(!choices.isEmpty()&&item.applyUpgrade(focus,choices.getFirst(),rank),entry.getKey()+": native rank "+rank+" upgrade applies");
            }
            var loaded=ItemStack.parseOptional(level.registryAccess(),(CompoundTag)focus.save(level.registryAccess()));
            check.accept(Arrays.equals(item.getAppliedUpgrades(focus),item.getAppliedUpgrades(loaded)),entry.getKey()+": focus upgrade tree survives item NBT");
        }
        var pouch=new ItemStack(R3Equipment.FOCUS_POUCH.get());var inside=FocusPouchItem.contents(pouch);
        inside.set(0,new ItemStack(R3Equipment.FOCI.get("fire").get()));FocusPouchItem.store(pouch,inside);player.getInventory().setItem(1,pouch);
        check.accept(WandFocus.select(player,InteractionHand.MAIN_HAND,1018)&&WandFocus.get(wand).is(R3Equipment.FOCI.get("fire").get())&&FocusPouchItem.contents(pouch).getFirst().isEmpty(),"radial selection moves one focus from pouch to held wand");
        var second=FocusPouchItem.contents(pouch);second.set(0,new ItemStack(R3Equipment.FOCI.get("frost").get()));FocusPouchItem.store(pouch,second);
        check.accept(WandFocus.select(player,InteractionHand.MAIN_HAND,1018)&&WandFocus.get(wand).is(R3Equipment.FOCI.get("frost").get())&&FocusPouchItem.contents(pouch).getFirst().is(R3Equipment.FOCI.get("fire").get()),"pouch focus swap returns the previous focus without duplication");
        var crusher=new ItemStack(R3Equipment.TOOLS.get("primal_crusher").get());crusher.setDamageValue(10);player.tickCount=20;
        crusher.getItem().inventoryTick(crusher,level,player,2,true);
        player.setItemInHand(InteractionHand.MAIN_HAND,crusher);
        check.accept(crusher.getDamageValue()==9&&WarpService.gearWarp(player)==2,"primal crusher self-repairs and supplies original gear warp");
        check.accept(crusher.getRarity()==Rarity.EPIC,"primal crusher retains original epic rarity");
        var runic=new ItemStack(R3Equipment.ARMOR.get("thaumium_helmet").get());thaumcraft.equipment.RunicShield.harden(runic);player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,runic);
        var recharge=new ItemStack(R3Equipment.WAND.get());WandVis.set(recharge,Aspects.AIR,2500);WandVis.set(recharge,Aspects.EARTH,2500);player.getInventory().setItem(8,recharge);
        thaumcraft.equipment.RunicShield.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        check.accept(thaumcraft.equipment.RunicShield.charge(player)==1&&WandVis.get(recharge,Aspects.AIR)<2500&&WandVis.get(recharge,Aspects.EARTH)<2500,"runic hardening recharges one shield from actual inventory vis");
        float health=player.getHealth();player.hurt(level.damageSources().fall(),1);
        check.accept(player.getHealth()==health&&thaumcraft.equipment.RunicShield.charge(player)==0,"native incoming damage consumes runic charge before health");player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,ItemStack.EMPTY);
        var raw=new RawOreIngredient(Ingredient.of(Items.RAW_IRON));boolean rawBefore=thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.get();
        try{thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.set(true);check.accept(raw.test(new ItemStack(Items.RAW_IRON)),"raw ore purification enabled by default");thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.set(false);check.accept(!raw.test(new ItemStack(Items.RAW_IRON)),"raw ore switch updates matching without a data reload");}finally{thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.set(rawBefore);}
        var cluster=new ItemStack(R3Equipment.NATIVE_CLUSTERS.get("iron").get());var smelt=level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,new SingleRecipeInput(cluster),level).orElseThrow();
        var metal=smelt.value().assemble(new SingleRecipeInput(cluster),level.registryAccess());
        check.accept(metal.is(Items.IRON_INGOT)&&metal.getCount()==2,"purified iron cluster smelts into two native iron ingots");
        for(var metalName:List.of("iron","gold","copper")){
            var ore=BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:"+metalName+"_ore"));
            var rawMetal=BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:raw_"+metalName));
            check.accept(level.getRecipeManager().getAllRecipesFor(R3Recipes.CRUCIBLE_TYPE.get()).stream().anyMatch(r->r.value().catalyst().test(new ItemStack(ore)))&&level.getRecipeManager().getAllRecipesFor(R3Recipes.CRUCIBLE_TYPE.get()).stream().anyMatch(r->r.value().catalyst().test(new ItemStack(rawMetal))),metalName+": ore and raw metal have genuine crucible recipes");
        }
        oreDrops(level,player,check);
        var chestLoot=level.getServer().reloadableRegistries().getLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.STRONGHOLD_LIBRARY);
        var lootParams=new net.minecraft.world.level.storage.loot.LootParams.Builder(level).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,ORIGIN.getCenter()).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        var fragments=new ArrayList<ItemStack>();for(int seed=1;seed<=32;seed++)for(var stack:chestLoot.getRandomItems(lootParams,seed))if(stack.is(R3Equipment.KNOWLEDGE_FRAGMENT.get()))fragments.add(stack);
        check.accept(!fragments.isEmpty()&&fragments.stream().allMatch(s->s.getCount()>=1&&s.getCount()<=6),"modern stronghold library loot includes original weighted knowledge fragments");
        var poolBefore=new LinkedHashMap<String,Integer>();for(var aspect:Aspects.PRIMALS)poolBefore.put(aspect.getTag(),KnowledgeService.get(player).pool(aspect));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get(),2));R3Equipment.KNOWLEDGE_FRAGMENT.get().use(level,player,InteractionHand.MAIN_HAND);
        check.accept(player.getMainHandItem().getCount()==1&&Aspects.PRIMALS.stream().allMatch(a->{int gained=KnowledgeService.get(player).pool(a)-poolBefore.get(a.getTag());return gained>=1&&gained<=2;}),"one knowledge fragment grants original one or two points of each primal and consumes one item");
        boolean wuss=thaumcraft.config.ThaumcraftConfig.WUSS_MODE.get();
        try{thaumcraft.config.ThaumcraftConfig.WUSS_MODE.set(false);WarpService.add(player,2,3,4);check.accept(KnowledgeService.get(player).permanentWarp()>=2&&KnowledgeService.get(player).stickyWarp()>=3&&KnowledgeService.get(player).temporaryWarp()>=4,"warp service persists all three original warp categories");WarpService.dispatch(player,23,60);check.accept(player.hasEffect(thaumcraft.effects.WarpEffects.UNNATURAL_HUNGER),"warp dispatcher applies real unnatural hunger effect");player.removeAllEffects();thaumcraft.config.ThaumcraftConfig.WUSS_MODE.set(true);int before=KnowledgeService.get(player).totalWarp();WarpService.add(player,1,1,1);check.accept(KnowledgeService.get(player).totalWarp()==before,"wuss setting blocks new warp changes");}finally{thaumcraft.config.ThaumcraftConfig.WUSS_MODE.set(wuss);}
        new ItemStack(R3Equipment.CRIMSON_RITES.get()).getItem().use(level,player,InteractionHand.MAIN_HAND);
        check.accept(KnowledgeService.get(player).completedResearch().contains("CRIMSON"),"Crimson Rites unlocks its real research");
        machinery(level,player,check);
        golems(level,check);
        var altarPos=ORIGIN.offset(0,0,12);level.setBlockAndUpdate(altarPos,R3Content.ELDRITCH_ALTAR.get().defaultBlockState());
        var altar=(EldritchAltarBlockEntity)level.getBlockEntity(altarPos);
        altar.installEye();altar.installEye();check.accept(altar.eyes==2&&!altar.guardianSpawner,"first two installed eyes do not enable guardian spawning");
        altar.installEye();check.accept(altar.eyes==3&&altar.guardianSpawner,"third installed eye enables original guardian defense");altar.installEye();altar.installEye();
        check.accept(altar.eyes==4,"obelisk installation is limited to four eyes");
        var altarCopy=new EldritchAltarBlockEntity(altarPos,altar.getBlockState());altarCopy.loadWithComponents(altar.getUpdateTag(level.registryAccess()),level.registryAccess());
        check.accept(altarCopy.eyes==4&&altarCopy.guardianSpawner,"installed eyes and guardian defense persist and synchronize");
        check.accept(level.getServer().getLevel(thaumcraft.world.dim.EldritchTeleporter.OUTER).getChunkSource().getGenerator() instanceof thaumcraft.world.dim.OuterChunkGenerator&&!altar.open&&!level.getBlockState(altarPos.above()).is(R5Content.ELDRITCH_PORTAL.get()),"registered native R5 dimension does not open its portal merely from installing four eyes");
    }

    private static void recipes(ServerLevel level,BiConsumer<Boolean,String> check) throws Exception {
        var file=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/INFUSION_COVERAGE.json");
        var entries=JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonArray("entries");
        check.accept(entries.size()==87,"reference catalog contains all 87 original infusion registrations");
        int enchantments=0;
        for(var value:entries){var entry=value.getAsJsonObject();var page=entry.get("page_key").getAsString();if(page.equals("WandCapSilver")){
            boolean hasSilver=java.util.stream.Stream.of("c:ingots/silver","c:nuggets/silver").allMatch(tag->BuiltInRegistries.ITEM.getTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ResourceLocation.parse(tag))).map(values->values.size()>0).orElse(false));
            boolean present=level.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:wandcapsilver")).isPresent();
            check.accept(present==hasSilver,"original silver-cap infusion is conditional on installed silver ingredients");if(!hasSilver)continue;
        }var id=ResourceLocation.fromNamespaceAndPath("thaumcraft",page.toLowerCase(Locale.ROOT));var holder=level.getRecipeManager().byKey(id).orElseThrow(()->new AssertionError("Missing original infusion "+page));
            check.accept(holder.value() instanceof InfusionRecipe,page+": original recipe resolves through native RecipeManager");
            var recipe=(InfusionRecipe)holder.value();
            check.accept(thaumcraft.data.ThaumcraftData.get(level.getServer()).research().containsKey(recipe.research()),page+": required research exists in loaded data");
            check.accept(recipe.components().stream().allMatch(i->i.getItems().length>0&&Arrays.stream(i.getItems()).noneMatch(stack->stack.is(Items.BARRIER))),page+": every component resolves to installed items");
            if(recipe instanceof InfusionEnchantmentRecipeData enchant){enchantments++;var tool=BuiltInRegistries.ITEM.stream().filter(i->{var s=new ItemStack(i);return s.isDamageableItem()&&enchant.enchantment().value().canEnchant(s);}).findFirst().orElseThrow();var central=new ItemStack(tool);var components=recipe.components().stream().map(i->i.getItems()[0].copy()).toList();check.accept(recipe.matches(new InfusionInput(central,components)),page+": native equipment accepts enchantment infusion");var output=recipe.getRecipeOutput(central);check.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(output).getLevel(enchant.enchantment())==1,page+": infusion increases the actual native enchantment by one");}
            else{var central=recipe.input().getItems()[0].copy();var components=new ArrayList<>(recipe.components(central).stream().map(i->i.getItems()[0].copy()).toList());Collections.reverse(components);check.accept(recipe.matches(new InfusionInput(central,components)),page+": unordered original component set matches");var output=recipe.getRecipeOutput(central);check.accept(!output.isEmpty()&&ItemStack.matches(output,ItemStack.parseOptional(level.registryAccess(),(CompoundTag)output.save(level.registryAccess()))),page+": output and upgrades survive native serialization");if(recipe instanceof InfusionUpgradeRecipe)check.accept(!recipe.matches(new InfusionInput(output,components)),page+": identical item upgrade cannot be applied twice");}
        }
        check.accept(enchantments==24,"all 24 original infusion enchantments are usable");
    }

    private static void machinery(ServerLevel level,net.minecraft.server.level.ServerPlayer player,BiConsumer<Boolean,String> check){
        for(var cell:BlockPos.betweenClosed(ORIGIN.offset(-3,-1,-3),ORIGIN.offset(13,3,15))){level.getChunkAt(cell);level.setBlock(cell,cell.getY()==ORIGIN.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);}
        var chestPos=ORIGIN;level.setBlockAndUpdate(chestPos,R3Content.HUNGRY_CHEST.get().defaultBlockState());var chest=(HungryChestBlockEntity)level.getBlockEntity(chestPos);
        var item=new ItemEntity(level,chestPos.getX()+.5,chestPos.getY()+1,chestPos.getZ()+.5,new ItemStack(Items.DIAMOND,7));level.addFreshEntity(item);chest.eat(item);
        check.accept(item.isRemoved()&&chest.items.getItem(0).is(Items.DIAMOND)&&chest.items.getItem(0).getCount()==7,"hungry chest absorbs a real item entity exactly once");
        var center=ORIGIN.offset(8,1,0);for(int part=0;part<27;part++)level.setBlock(center.offset(InfernalFurnaceBlock.offset(part)),part==22?Blocks.AIR.defaultBlockState():InfernalFurnaceBlock.original(part,Direction.NORTH),2);
        check.accept(InfernalFurnaceBlock.fits(level,center)==Direction.NORTH,"original infernal furnace shell recognizes its output fence");
        KnowledgeService.mutate(player,k->k.completeResearch("INFERNALFURNACE"));var wand=new ItemStack(R3Equipment.WAND.get());wand.set(WandComponents.PARTS.get(),new WandParts("silverwood","gold"));WandVis.set(wand,Aspects.FIRE,10000);WandVis.set(wand,Aspects.EARTH,10000);
        check.accept(InfernalFurnaceBlock.activate(player,wand,center)&&WandVis.get(wand,Aspects.FIRE)==5000&&WandVis.get(wand,Aspects.EARTH)==5000,"researched furnace activation consumes exact fifty fire and earth vis");
        var furnace=(InfernalFurnaceBlockEntity)level.getBlockEntity(center);furnace.input.insertItem(0,new ItemStack(Items.RAW_IRON),false);
        for(int i=0;i<41;i++)InfernalFurnaceBlockEntity.tick(level,center,furnace.getBlockState(),furnace);
        check.accept(furnace.cookTime>0&&!furnace.items.isEmpty(),"infernal furnace starts real smelting without fuel");
        var snapshot=furnace.saveWithoutMetadata(level.registryAccess());var resumed=new InfernalFurnaceBlockEntity(center,furnace.getBlockState());resumed.loadWithComponents(snapshot,level.registryAccess());level.setBlockEntity(resumed);
        for(int i=0;i<160;i++)InfernalFurnaceBlockEntity.tick(level,center,resumed.getBlockState(),resumed);
        var outputs=level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(center).inflate(3),e->e.getItem().is(Items.IRON_INGOT));
        check.accept(resumed.items.isEmpty()&&outputs.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"restored infernal smelting emits exactly one iron ingot without duplicate input");outputs.forEach(ItemEntity::discard);
        resumed.input.insertItem(0,new ItemStack(Items.RAW_GOLD,3),false);level.destroyBlock(center.east(),true);
        check.accept(level.getBlockState(center).is(Blocks.LAVA),"breaking one shell part restores original lava");
        check.accept(level.getBlockState(center.north()).is(Blocks.IRON_BARS),"breaking one shell part restores original iron-bar output");
        check.accept(level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(center).inflate(3),e->e.getItem().is(Items.RAW_GOLD)).stream().mapToInt(e->e.getItem().getCount()).sum()==3,"breaking a non-core furnace part drops stored inventory exactly once");
        var borePos=ORIGIN.offset(0,0,8);level.setBlockAndUpdate(borePos,R3Content.BORE.get().defaultBlockState());var bore=(ArcaneBoreBlockEntity)level.getBlockEntity(borePos);
        bore.items.setItem(0,new ItemStack(R3Equipment.FOCI.get("excavation").get()));bore.items.setItem(1,new ItemStack(Items.DIAMOND_PICKAXE));check.accept(bore.valid(),"arcane bore accepts a real excavation focus and mining pick");
        var boreCopy=new ArcaneBoreBlockEntity(borePos,bore.getBlockState());boreCopy.loadWithComponents(bore.saveWithoutMetadata(level.registryAccess()),level.registryAccess());level.setBlockEntity(boreCopy);check.accept(boreCopy.valid()&&boreCopy.items.getItem(1).is(Items.DIAMOND_PICKAXE),"bore equipment survives replacement from saved NBT");
    }

    private static void golems(ServerLevel level,BiConsumer<Boolean,String> check){
        for(var material:GolemMaterial.values()){
            var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(material).get());var config=new CompoundTag();config.putInt("core",2);config.putBoolean("advanced",true);config.putString("deco","HG");config.putByteArray("upgrades",new byte[]{0,1,3});body.set(DataComponents.CUSTOM_DATA,CustomData.of(config));
            var golem=R3Entities.GOLEM.get().create(level);golem.initialize(material,OWNER,ORIGIN.above(),Direction.UP,body);golem.carried(new ItemStack(Items.EMERALD,3));golem.essentia=Aspects.FIRE;golem.essentiaAmount=2;golem.fluid=new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000);var tag=new CompoundTag();golem.addAdditionalSaveData(tag);
            var saved=R3Entities.GOLEM.get().create(level);saved.readAdditionalSaveData(tag);
            check.accept(saved.material()==material&&saved.core()==2&&saved.advanced()&&OWNER.equals(saved.owner())&&saved.carried().getCount()==3&&saved.essentiaAmount==2&&saved.fluid.getAmount()==1000,material+": body, owner, core, upgrades and carried resources survive entity NBT");
        }
        var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(GolemMaterial.WOOD).get());CustomData.update(DataComponents.CUSTOM_DATA,body,t->t.putInt("core",2));var golem=R3Entities.GOLEM.get().create(level);golem.initialize(GolemMaterial.WOOD,OWNER,ORIGIN.above(),Direction.UP,body);golem.moveTo(ORIGIN.getX()+.5,ORIGIN.getY()+1,ORIGIN.getZ()+.5);level.addFreshEntity(golem);
        var dropped=new ItemEntity(level,golem.getX(),golem.getY(),golem.getZ(),new ItemStack(Items.EMERALD,3));dropped.setNoPickUpDelay();level.addFreshEntity(dropped);
        var job=new GolemWorkGoal(golem);check.accept(job.canUse(),"gather core finds an actual nearby item entity");job.start();job.tick();check.accept(dropped.isRemoved()&&golem.carried().is(Items.EMERALD)&&golem.carried().getCount()==3,"gather job removes entity and carries its exact stack");job.stop();golem.tickCount+=6;check.accept(job.canUse(),"gather core schedules home deposit after pickup");job.start();job.tick();job.stop();
        var chest=(HungryChestBlockEntity)level.getBlockEntity(ORIGIN);check.accept(golem.carried().isEmpty()&&chest.items.getItem(1).is(Items.EMERALD)&&chest.items.getItem(1).getCount()==3,"gather job deposits through native sided item capability without duplication");golem.discard();
        var trunk=R3Entities.TRUNK.get().create(level);var player=FakePlayerFactory.get(level,new GameProfile(OWNER,"R3_Equipment_Test"));var packed=new ItemStack(R3Equipment.TRAVELING_TRUNK.get());CustomData.update(DataComponents.CUSTOM_DATA,packed,t->t.putByte("upgrade",(byte)1));trunk.configure(packed,player);trunk.inventory.setItem(35,new ItemStack(Items.NETHERITE_INGOT,2));trunk.toggleStay();var nbt=new CompoundTag();trunk.addAdditionalSaveData(nbt);var restored=R3Entities.TRUNK.get().create(level);restored.readAdditionalSaveData(nbt);check.accept(restored.rows()==4&&restored.stay()&&restored.inventory.getItem(35).getCount()==2,"earth-upgraded trunk retains four rows, last slot and stay mode");
    }

    private static void oreDrops(ServerLevel level,net.minecraft.server.level.ServerPlayer player,BiConsumer<Boolean,String> check){
        var origin=ORIGIN.offset(40,0,0);var tool=new ItemStack(R3Equipment.TOOLS.get("elemental_pickaxe").get());player.setItemInHand(InteractionHand.MAIN_HAND,tool);
        boolean before=thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.get();
        try{
            for(boolean enabled:new boolean[]{true,false}){
                thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.set(enabled);level.random.setSeed(348291);
                int broken=0;for(int x=0;x<32;x++){var pos=origin.east(x);level.getChunkAt(pos);level.setBlockAndUpdate(pos,Blocks.IRON_ORE.defaultBlockState());if(player.gameMode.destroyBlock(pos))broken++;}
                check.accept(broken==32,"elemental pickaxe harvests native iron ore through normal protection and loot hooks");
                var drops=level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(origin.getCenter(),origin.offset(32,2,2).getCenter()).inflate(1));
                int clusters=drops.stream().filter(e->e.getItem().is(R3Equipment.NATIVE_CLUSTERS.get("iron").get())).mapToInt(e->e.getItem().getCount()).sum();
                check.accept(enabled?clusters>0:clusters==0,enabled?"original special mining chance produces purified clusters from native raw drops":"raw ore switch preserves native raw loot during special mining");drops.forEach(ItemEntity::discard);
            }
        }finally{thaumcraft.config.ThaumcraftConfig.RAW_ORE_PURIFICATION.set(before);}
    }

    /** Independent save phase: reload never relies on all earlier equipment assertions completing. */
    static void prepareReload(ServerLevel level,BiConsumer<Boolean,String> check){
        level.getChunkAt(ORIGIN);level.getChunkAt(ORIGIN.offset(0,0,12));
        level.setBlockAndUpdate(ORIGIN,R3Content.HUNGRY_CHEST.get().defaultBlockState());
        var chest=(HungryChestBlockEntity)level.getBlockEntity(ORIGIN);
        chest.items.setItem(0,new ItemStack(Items.DIAMOND,7));chest.items.setItem(1,new ItemStack(Items.EMERALD,3));chest.setChanged();
        var altarPos=ORIGIN.offset(0,0,12);level.setBlockAndUpdate(altarPos,R3Content.ELDRITCH_ALTAR.get().defaultBlockState());
        var altar=(EldritchAltarBlockEntity)level.getBlockEntity(altarPos);for(int i=0;i<4;i++)altar.installEye();altar.setChanged();
        var borePos=ORIGIN.offset(0,0,8);level.setBlockAndUpdate(borePos,R3Content.BORE.get().defaultBlockState());
        var bore=(ArcaneBoreBlockEntity)level.getBlockEntity(borePos);bore.items.setItem(0,new ItemStack(R3Equipment.FOCI.get("excavation").get()));bore.items.setItem(1,new ItemStack(Items.DIAMOND_PICKAXE));bore.setChanged();
        var player=FakePlayerFactory.get(level,new GameProfile(OWNER,"R3_Equipment_Test"));KnowledgeService.initialize(player);
        KnowledgeService.mutate(player,k->{k.completeResearch("CRIMSON");k.setWarp(5,4,0,9);return true;});
        check.accept(chest.items.getItem(0).getCount()==7&&chest.items.getItem(1).getCount()==3&&altar.eyes==4&&bore.valid(),"independent equipment save fixture contains chest, altar and usable bore");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){
        level.getChunkAt(ORIGIN);level.getChunkAt(ORIGIN.offset(0,0,12));
        var chest=(HungryChestBlockEntity)level.getBlockEntity(ORIGIN);check.accept(chest!=null&&chest.items.getItem(0).getCount()==7&&chest.items.getItem(1).getCount()==3,"real server restart retains hungry chest contents without duplicated resources");
        var altar=(EldritchAltarBlockEntity)level.getBlockEntity(ORIGIN.offset(0,0,12));check.accept(altar!=null&&altar.eyes==4&&altar.guardianSpawner,"real server restart retains all four installed eyes");
        var bore=(ArcaneBoreBlockEntity)level.getBlockEntity(ORIGIN.offset(0,0,8));check.accept(bore!=null&&bore.valid(),"real server restart retains usable bore focus and pick");
        var player=FakePlayerFactory.get(level,new GameProfile(OWNER,"R3_Equipment_Test"));KnowledgeService.initialize(player);check.accept(KnowledgeService.get(player).completedResearch().contains("CRIMSON")&&KnowledgeService.get(player).totalWarp()>=9,"separate UUID knowledge file survives actual restart with research and warp");
    }
    private EquipmentVerification(){}
}

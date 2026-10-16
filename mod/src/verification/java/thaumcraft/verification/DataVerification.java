package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.api.research.ResearchNote;
import thaumcraft.blocks.*;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.items.ResearchNotes;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.menu.ResearchMenu;
import thaumcraft.registry.R2Content;

/** Real registry/recipe data and table transactions, independent of a client. */
final class DataVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check) throws Exception {
        var server=level.getServer();var snapshot=ThaumcraftData.get(server);
        check.accept(snapshot.research().get("CAP_iron").virtual()&&snapshot.research().get("ROD_wood").virtual()&&!snapshot.research().get("CAP_iron").available(new thaumcraft.api.knowledge.PlayerKnowledge()),"virtual TC4 cap/rod gates cannot be requested as visible research");
        var researchTag=new CompoundTag();thaumcraft.data.ResearchDataSync.encode(researchTag,snapshot.research());var researchCopy=thaumcraft.data.ResearchDataSync.decode(researchTag);
        check.accept(researchCopy.get("CAP_iron").virtual()&&researchCopy.get("THAUMONOMICON").round()&&researchCopy.get("THAUMONOMICON").stub()&&researchCopy.get("ASPECTS").icon().equals("thaumcraft:textures/misc/r_aspects.png"),"client codec preserves original virtual/round/stub flags and resource icons");
        check.accept(snapshot.objects().containsKey("thaumcraft:thaumometer")&&snapshot.objects().containsKey("thaumcraft:goggles_of_revealing"),"allowed TC4 crafting outputs receive calculated aspects");
        check.accept(!snapshot.objects().containsKey("minecraft:netherite_sword")&&!snapshot.objects().containsKey("minecraft:observer"),"new unlisted recipe outputs receive no automatic aspects");
        check.accept(snapshot.objects().get("minecraft:iron_ingot").equals(Map.of("metallum",4)),"explicit iron values dominate derivation through nuggets and blocks");
        check.accept(snapshot.objects().get("thaumcraft:iron_capped_wooden_wand").getOrDefault("praecantatio",0)>=1,"wand preserves original minimum magic aspect");
        check.accept(snapshot.objects().get("thaumcraft:goggles_of_revealing").getOrDefault("sensus",0)>=4,"goggles include original complex-object SENSES bonus");
        check.accept(ThaumcraftData.objectScanKey(server,new ItemStack(Items.DAMAGED_ANVIL)).equals(ThaumcraftData.objectScanKey(server,new ItemStack(Items.ANVIL))),"anvil wear variants share the original scan group");
        check.accept(!ThaumcraftData.objectScanKey(server,new ItemStack(Items.OAK_PLANKS)).equals(ThaumcraftData.objectScanKey(server,new ItemStack(Items.BIRCH_PLANKS))),"ungrouped wood metadata variants remain separate scans");
        var wisp=thaumcraft.entities.R2Entities.WISP.get().create(level);wisp.setAspect(Aspects.AIR);var air=ThaumcraftData.aspects(server,wisp);String first=ThaumcraftData.entityScanKey(server,wisp);wisp.setAspect(Aspects.FIRE);
        check.accept(air.getOrDefault("aer",0)==3&&air.getOrDefault("praecantatio",0)==1&&!first.equals(ThaumcraftData.entityScanKey(server,wisp)),"Wisp NBT variants carry original aspects and separate scan identity");
        var essence=thaumcraft.items.WispEssenceItem.of(Aspects.FIRE);
        check.accept(ThaumcraftData.aspects(server,essence).getOrDefault("auram",0)==2&&ThaumcraftData.aspects(server,essence).getOrDefault("ignis",0)==2,"dropped essence scanning adds contained essentia to the original aura base");
        check.accept(ThaumcraftData.aspects(server,thaumcraft.data.StackAspectRules.inventoryScanStack(essence)).equals(Map.of("auram",2)),"legacy inventory scans retain ID/metadata aspects without essentia NBT bonuses");
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TC4_Data_Test"));player.moveTo(120.5,95,120.5,0,0);KnowledgeService.initialize(player);
        var pos=new BlockPos(120,95,120);level.getChunkAt(pos);level.setBlockAndUpdate(pos,R2Content.RESEARCH_TABLE.get().defaultBlockState());
        var table=(TableBlockEntity)level.getBlockEntity(pos);
        // A saved bonus is available only while the personal pool is empty.
        loadBonuses(table,level,Map.of("aer",1,"ignis",1));
        var menu=new ResearchMenu(11,player.getInventory(),table);KnowledgeService.mutate(player,k->{k.setPool("aer",1);k.setPool("ignis",0);return true;});
        check.accept(menu.combine(player,"aer","ignis")&&KnowledgeService.get(player).pool(Aspects.AIR)==0&&table.bonus(Aspects.AIR)==1&&table.bonus(Aspects.FIRE)==0,"combination spends personal points first and reserved table bonuses second");
        loadBonuses(table,level,Map.of("aer",1));KnowledgeService.mutate(player,k->{k.setPool("aer",0);return true;});
        check.accept(!menu.combine(player,"aer","aer")&&table.bonus(Aspects.AIR)==1&&KnowledgeService.get(player).pool(Aspects.AIR)==0,"failed double-aspect combination rolls back both resources");
        KnowledgeService.mutate(player,k->{k.setPool("aer",1);return true;});
        check.accept(menu.combine(player,"aer","aer")&&table.bonus(Aspects.AIR)==0&&KnowledgeService.get(player).pool(Aspects.AIR)==0,"two equal ingredients may spend one personal and one table point");
        table.setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));var noteStack=new ItemStack(R2Content.NOTES.get());
        var grid=new LinkedHashMap<ResearchNote.Hex,ResearchNote.Entry>();grid.put(new ResearchNote.Hex(0,0),new ResearchNote.Entry("",0));grid.put(new ResearchNote.Hex(-4,0),new ResearchNote.Entry("ignis",1));grid.put(new ResearchNote.Hex(4,0),new ResearchNote.Entry("aqua",1));
        ResearchNotes.write(noteStack,new ResearchNote("GOGGLES",grid,false,0));table.setItem(1,noteStack);loadBonuses(table,level,Map.of("aer",1));
        check.accept(menu.place(player,0,0,"aer")&&table.bonus(Aspects.AIR)==0&&table.getItem(0).getDamageValue()==1,"placement consumes a bonus point and exactly one ink use");
        check.accept(!menu.place(player,0,0,"ignis")&&ResearchNotes.read(table.getItem(1)).grid.get(new ResearchNote.Hex(0,0)).aspect().equals("aer")&&table.getItem(0).getDamageValue()==1,"insufficient placement cannot change the grid or ink");
        check.accept(menu.place(player,0,0,"")&&table.getItem(0).getDamageValue()==2,"erasing a placed aspect retains original ink cost");
        loadBonuses(table,level,Map.of("ordo",1,"aer",1));var tag=table.saveWithoutMetadata(level.registryAccess());var copy=new TableBlockEntity(pos,table.getBlockState());copy.loadWithComponents(tag,level.registryAccess());
        check.accept(copy.bonus(Aspects.ORDER)==1&&copy.bonus(Aspects.AIR)==1&&copy.getItem(0).getDamageValue()==2,"table bonuses and ink survive a real block-entity NBT round trip");
        loadBonuses(table,level,Map.of());level.setBlockAndUpdate(pos.above(),Blocks.WATER.defaultBlockState());
        for(int i=0;i<300&&table.bonus(Aspects.WATER)==0;i++)table.recalculateBonuses(level);
        check.accept(table.bonus(Aspects.WATER)==1,"nearby water generates its original one-point bonus");for(int i=0;i<300;i++)table.recalculateBonuses(level);
        check.accept(table.bonus(Aspects.WATER)==1,"environment bonus cannot accumulate beyond one point per aspect");
        level.removeBlock(pos.above(),false);level.removeBlock(pos,false);
        inventoryScanning(level,player,check);
        snapshots(level,player,check);
        stackRules(level,check);
        reload(level,check);
    }
    private static void stackRules(ServerLevel level,BiConsumer<Boolean,String> check){var server=level.getServer();var sword=new ItemStack(Items.IRON_SWORD);
        check.accept(ThaumcraftData.aspects(server,sword).getOrDefault("telum",0)==7&&ThaumcraftData.aspects(server,new ItemStack(Items.IRON_PICKAXE)).getOrDefault("perfodio",0)==3,"old sword and pickaxe minimums match original tool-material damage");
        check.accept(ThaumcraftData.aspects(server,new ItemStack(Items.DIAMOND_HELMET)).getOrDefault("tutamen",0)>=3,"armour minimum respects original piece protection");
        var healing=new ItemStack(Items.POTION);healing.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.STRONG_HEALING));var values=ThaumcraftData.aspects(server,healing);
        check.accept(values.getOrDefault("aqua",0)==1&&values.getOrDefault("sano",0)==6&&values.getOrDefault("praecantatio",0)==4,"strong healing potion carries original water, amplified heal and magic aspects");var splash=new ItemStack(Items.SPLASH_POTION);splash.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.HEALING));
        check.accept(ThaumcraftData.aspects(server,splash).getOrDefault("perditio",0)==2,"splash potion with effects adds original entropy minimum");var water=new ItemStack(Items.POTION);water.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.WATER));
        check.accept(ThaumcraftData.aspects(server,water).equals(Map.of("aqua",1))&&!ThaumcraftData.objectScanKey(server,water).equals(ThaumcraftData.objectScanKey(server,healing)),"potion component identity preserves distinct legacy metadata scans");
        var lingering=new ItemStack(Items.LINGERING_POTION);lingering.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.HEALING));check.accept(ThaumcraftData.aspects(server,lingering).isEmpty(),"new unlisted lingering potions receive no automatic aspects");
        var enchants=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);sword.enchant(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS),3);sword.enchant(enchants.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING),2);values=ThaumcraftData.aspects(server,sword);
        check.accept(values.getOrDefault("telum",0)==7&&values.getOrDefault("praecantatio",0)==5&&values.getOrDefault("lucrum",0)==2,"old enchantments merge tag minimums and total levels of magic");check.accept(!ThaumcraftData.aspects(server,thaumcraft.data.StackAspectRules.inventoryScanStack(sword)).containsKey("praecantatio"),"legacy slot scanning does not invent enchantment-NBT rewards");
    }
    private static void reload(ServerLevel level,BiConsumer<Boolean,String> check) throws Exception {
        var server=level.getServer();var before=ThaumcraftData.get(server);var originalSelection=new ArrayList<>(server.getPackRepository().getSelectedIds());var caps=server.getRecipeManager().getRecipes().stream().filter(holder->holder.value().getResultItem(level.registryAccess()).is(R2Content.IRON_CAP.get())).toList();if(caps.isEmpty())throw new AssertionError("Missing iron-cap reload fixture recipes");
        var workspace=java.nio.file.Path.of(System.getProperty("thaumcraft.r2.root")).toRealPath();var packs=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR);java.nio.file.Files.createDirectories(packs);if(!packs.toRealPath().startsWith(workspace))throw new IllegalStateException("Verification datapack escaped workspace");var pack=packs.resolve("tc4-r2-verify-"+UUID.randomUUID());
        var files=new LinkedHashMap<java.nio.file.Path,String>();int format=net.minecraft.SharedConstants.getCurrentVersion().getPackVersion(net.minecraft.server.packs.PackType.SERVER_DATA);files.put(pack.resolve("pack.mcmeta"),"{\"pack\":{\"pack_format\":"+format+",\"description\":\"Temporary R2 aspect reload verification\"}}");files.put(pack.resolve("data/thaumcraft/thaumcraft/zz_r2_verification.json"),"{\"kind\":\"object_aspects\",\"entries\":{\"minecraft:book\":{\"cognitio\":31}}}");for(var cap:caps)files.put(pack.resolve("data/"+cap.id().getNamespace()+"/recipe/"+cap.id().getPath()+".json"),"{\"type\":\"minecraft:crafting_shaped\",\"category\":\"misc\",\"pattern\":[\"CC\",\"CC\"],\"key\":{\"C\":{\"item\":\"minecraft:cobblestone\"}},\"result\":{\"id\":\"thaumcraft:iron_wand_cap\",\"count\":1}}");
        try {for(var entry:files.entrySet()){java.nio.file.Files.createDirectories(entry.getKey().getParent());java.nio.file.Files.writeString(entry.getKey(),entry.getValue());}server.getPackRepository().reload();var selected=new ArrayList<>(originalSelection);String id="file/"+pack.getFileName();check.accept(server.getPackRepository().getAvailableIds().contains(id),"reload fixture is a real discoverable datapack");selected.add(id);var future=server.reloadResources(selected);server.managedBlock(future::isDone);future.join();var changed=ThaumcraftData.get(server);
            check.accept(changed!=before&&changed.objects().get("minecraft:book").equals(Map.of("cognitio",31)),"real datapack reload replaces definitions; explicit overrides dominate calculated book aspects");check.accept(changed.objects().get("thaumcraft:iron_wand_cap").equals(Map.of("terra",3,"perditio",3)),"recipe aspect allowlist recomputes every alternative cap recipe after real reload and tag binding");check.accept(!changed.objects().containsKey("minecraft:observer"),"reload cannot expand automatic aspect assignment beyond the allowlist");
        } finally {var restored=server.reloadResources(originalSelection);server.managedBlock(restored::isDone);restored.join();for(var file:files.keySet())java.nio.file.Files.deleteIfExists(file);var directories=new TreeSet<java.nio.file.Path>(Comparator.comparingInt((java.nio.file.Path p)->p.getNameCount()).thenComparing(java.nio.file.Path::toString).reversed());for(var file:files.keySet())for(var parent=file.getParent();parent!=null&&parent.startsWith(pack);parent=parent.getParent())directories.add(parent);for(var directory:directories)java.nio.file.Files.deleteIfExists(directory);server.getPackRepository().reload();}
        check.accept(ThaumcraftData.get(server).objects().get("thaumcraft:iron_wand_cap").equals(before.objects().get("thaumcraft:iron_wand_cap")),"restoring original datapacks restores original recipe-derived aspects");
    }
    private static void inventoryScanning(ServerLevel level,net.minecraft.server.level.ServerPlayer player,BiConsumer<Boolean,String> check){
        var session=KnowledgeService.session(player);player.containerMenu=player.inventoryMenu;player.getInventory().selected=0;player.getInventory().setItem(0,new ItemStack(R2Content.THAUMOMETER.get()));player.getInventory().setItem(1,new ItemStack(Items.DIRT));int slot=37;
        check.accept(!thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(UUID.randomUUID(),0,slot,1)),"inventory scan rejects other server sessions");
        check.accept(!thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,999,1)),"inventory scan rejects invalid slot IDs");
        check.accept(!thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,999,slot,2)),"inventory scan rejects unrelated containers");
        check.accept(thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,3)),"inventory scanner starts only on the live owned slot");
        int before=KnowledgeService.get(player).pool(Aspects.EARTH);for(int i=0;i<19;i++)thaumcraft.knowledge.InventoryScanService.advance(player);
        check.accept(!KnowledgeService.get(player).scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT).contains("minecraft:dirt"),"inventory scan grants no reward before twenty server ticks");thaumcraft.knowledge.InventoryScanService.advance(player);
        check.accept(KnowledgeService.get(player).scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT).contains("minecraft:dirt")&&KnowledgeService.get(player).pool(Aspects.EARTH)>before,"inventory scan rewards server-calculated item aspects after twenty ticks");
        check.accept(!thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,3)),"inventory scan rejects replayed requests");
        player.getInventory().setItem(1,new ItemStack(Items.SAND));thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,4));player.getInventory().setItem(1,new ItemStack(Items.GRAVEL));thaumcraft.knowledge.InventoryScanService.advance(player);
        check.accept(!thaumcraft.knowledge.InventoryScanService.active(player)&&!KnowledgeService.get(player).scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT).contains("minecraft:gravel"),"slot replacement cancels without scanning the substitute item");
        player.getInventory().setItem(1,new ItemStack(Items.SAND));thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,5));player.containerMenu=new ResearchMenu(22,player.getInventory(),new TableBlockEntity(new BlockPos(0,95,0),R2Content.RESEARCH_TABLE.get().defaultBlockState()));thaumcraft.knowledge.InventoryScanService.advance(player);
        check.accept(!thaumcraft.knowledge.InventoryScanService.active(player),"menu replacement cancels a pending scan even if slot numbers match");player.containerMenu=player.inventoryMenu;
        player.getInventory().setItem(0,ItemStack.EMPTY);check.accept(!thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,6)),"server refuses inventory scanning without a held thaumometer");player.containerMenu.setCarried(new ItemStack(R2Content.THAUMOMETER.get()));
        check.accept(thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,slot,7)),"thaumometer carried by the cursor retains original inventory scanning");thaumcraft.knowledge.InventoryScanService.begin(player,new thaumcraft.network.InventoryScanPayload(session,0,-1,8));check.accept(!thaumcraft.knowledge.InventoryScanService.active(player),"leaving the hovered slot explicitly cancels the scan");player.containerMenu.setCarried(ItemStack.EMPTY);
    }
    private static void snapshots(ServerLevel level,net.minecraft.server.level.ServerPlayer player,BiConsumer<Boolean,String> check){
        var data=thaumcraft.knowledge.KnowledgeNbt.encode(KnowledgeService.get(player));thaumcraft.data.ResearchDataSync.encode(data,ThaumcraftData.get(level.getServer()).research());thaumcraft.data.AspectDataSync.encode(data,ThaumcraftData.get(level.getServer()));data.putLong("snapshotSequence",1);
        var mirror=thaumcraft.data.AspectDataSync.decode(data);check.accept(mirror.objects().equals(ThaumcraftData.get(level.getServer()).objects())&&mirror.variants().get("thaumcraft:wisp").size()==48,"client definition codec preserves derived object values and all Wisp variants");
        thaumcraft.network.ClientKnowledge.clear();thaumcraft.network.ClientKnowledge.accept(new thaumcraft.network.KnowledgePayload(player.getUUID(),KnowledgeService.session(player),data));var changed=data.copy();changed.putLong("snapshotSequence",2);changed.getCompound("objectAspectDefinitions").getCompound("minecraft:dirt").putInt("terra",9);thaumcraft.network.ClientKnowledge.accept(new thaumcraft.network.KnowledgePayload(player.getUUID(),KnowledgeService.session(player),changed));
        check.accept(thaumcraft.network.ClientKnowledge.aspects().objects().get("minecraft:dirt").get("terra")==9,"new data snapshot applies when player knowledge revision is unchanged");thaumcraft.network.ClientKnowledge.accept(new thaumcraft.network.KnowledgePayload(player.getUUID(),KnowledgeService.session(player),data));check.accept(thaumcraft.network.ClientKnowledge.aspects().objects().get("minecraft:dirt").get("terra")==9,"replayed data snapshot cannot roll definitions back");thaumcraft.network.ClientKnowledge.clear();
    }
    private static void loadBonuses(TableBlockEntity table,ServerLevel level,Map<String,Integer> bonuses) {var data=table.saveWithoutMetadata(level.registryAccess());var tag=new CompoundTag();bonuses.forEach(tag::putInt);data.put("BonusAspects",tag);table.loadWithComponents(data,level.registryAccess());}
    private DataVerification() {}
}

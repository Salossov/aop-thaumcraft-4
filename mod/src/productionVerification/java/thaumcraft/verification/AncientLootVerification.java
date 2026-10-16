package thaumcraft.verification;

import java.util.*;
import java.nio.file.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.internal.WeightedRandomLoot;
import thaumcraft.api.aspects.*;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.*;
import thaumcraft.items.*;
import thaumcraft.loot.LegacyLoot;
import thaumcraft.registry.*;
import thaumcraft.world.*;

/** Actual native bag use, block drops, generation, loot tables and world restart. */
final class AncientLootVerification {
    private static final BlockPos MOUND=new BlockPos(2688,140,32),HILL=MOUND.offset(40,10,0),STASH=MOUND.offset(-24,10,0);
    private static List<ArrayList<WeightedRandomLoot>> pools(){return List.of(WeightedRandomLoot.lootBagCommon,WeightedRandomLoot.lootBagUncommon,WeightedRandomLoot.lootBagRare);}
    static void prepare(ServerLevel l){for(int x=166;x<=172;x++)for(int z=0;z<=4;z++){l.setChunkForced(x,z,true);l.getChunk(x,z);}}
    static boolean ready(ServerLevel l){for(int x=166;x<=172;x++)for(int z=0;z<=4;z++)if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    static void release(ServerLevel l){for(int x=166;x<=172;x++)for(int z=0;z<=4;z++)l.setChunkForced(x,z,false);}
    static void run(ServerLevel l,BiConsumer<Boolean,String> check)throws Exception {
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var proof=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/ancient-loot-provenance.json"))).getAsJsonObject();
        for(int rarity=0;rarity<3;rarity++){
            var pool=pools().get(rarity);var staticCounts=new HashMap<String,Integer>();
            for(var e:proof.getAsJsonArray("static_entries")){var row=e.getAsJsonObject();boolean applies=false;for(var r:row.getAsJsonArray("rarities"))if(r.getAsInt()==rarity)applies=true;if(applies)staticCounts.merge(row.get("id").getAsString()+"/"+row.get("count").getAsInt()+"/"+row.get("weight").getAsInt(),1,Integer::sum);}
            for(var entry:staticCounts.entrySet()){var key=entry.getKey().split("/");long actual=pool.stream().filter(e->BuiltInRegistries.ITEM.getKey(e.item.getItem()).toString().equals(key[0])&&e.item.getCount()==Integer.parseInt(key[1])&&e.getWeight().asInt()==Integer.parseInt(key[2])).count();check.accept(actual==entry.getValue(),"original weighted item multiplicity in tier "+rarity+": "+entry.getKey());}
            var potionRows=pool.stream().filter(e->e.item.is(Items.POTION)||e.item.is(Items.SPLASH_POTION)).toList();check.accept(potionRows.size()==72,"all 72 original PotionHelper variants occur in tier "+rarity);
            int index=0;for(var e:proof.getAsJsonArray("potion_entries")){var row=e.getAsJsonObject();var entry=potionRows.get(index++);var effect=row.getAsJsonArray("effects").get(0).getAsJsonObject();var contents=entry.item.get(DataComponents.POTION_CONTENTS);var list=new ArrayList<net.minecraft.world.effect.MobEffectInstance>();contents.getAllEffects().forEach(list::add);check.accept(list.size()==1&&BuiltInRegistries.MOB_EFFECT.getKey(list.getFirst().getEffect().value()).getPath().equals(originalEffect(effect.get("id").getAsInt()))&&list.getFirst().getDuration()==effect.get("duration").getAsInt()&&list.getFirst().getAmplifier()==effect.get("amplifier").getAsInt()&&entry.item.is((row.get("metadata").getAsInt()&16384)!=0?Items.SPLASH_POTION:Items.POTION)&&entry.getWeight().asInt()==row.get("weight").getAsInt(),"native potion retains actual original duration, amplifier, splash type and weight: tier "+rarity+" row "+index);roundTrip(l,entry.item,check,"potion "+rarity+"/"+index);}
        }
        var extension=new ItemStack(Items.AMETHYST_SHARD,7);int[] before=pools().stream().mapToInt(List::size).toArray();
        try{ThaumcraftApi.addLootBagItem(extension,13);ThaumcraftApi.addLootBagItem(extension,17,1,2,99);check.accept(pools().get(0).size()==before[0]+1&&pools().get(1).size()==before[1]+1&&pools().get(2).size()==before[2]+1,"native public loot API retains default tier and ignores invalid tiers");}finally{for(int i=0;i<3;i++)pools().get(i).subList(before[i],pools().get(i).size()).clear();}
        for(int tier=0;tier<3;tier++){var pool=pools().get(tier);var saved=pool.stream().map(e->e.item.copy()).toList();var random=RandomSource.create(710+ tier);boolean nonempty=true;for(int n=0;n<512;n++)nonempty&=!LegacyLoot.generate(l,tier,random).isEmpty();check.accept(nonempty,"512 real weighted generations never return empty stacks in tier "+tier);check.accept(java.util.stream.IntStream.range(0,pool.size()).allMatch(i->ItemStack.matches(saved.get(i),pool.get(i).item)),"generated enchantments never mutate shared loot templates: tier "+tier);}
        for(int slot=0;slot<5;slot++)for(int quality=0;quality<7;quality++)check.accept(LegacyLoot.gearItem(slot,quality)!=Items.AIR,"original gear tier and slot resolve a real native item: "+slot+"/"+quality);
        var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678914"),"AncientLootQA"));player.moveTo(STASH.getX()+.5,STASH.getY()+1,STASH.getZ()+.5,0,0);
        for(var mode:List.of(net.minecraft.world.level.GameType.SURVIVAL,net.minecraft.world.level.GameType.CREATIVE))for(int rarity=0;rarity<3;rarity++){
            player.setGameMode(mode);l.getEntitiesOfClass(ItemEntity.class,new AABB(STASH).inflate(5)).forEach(ItemEntity::discard);var bag=new ItemStack(R5Content.LOOT_BAGS.get(rarity).get(),2);player.setItemInHand(InteractionHand.MAIN_HAND,bag);var result=bag.use(l,player,InteractionHand.MAIN_HAND);var drops=l.getEntitiesOfClass(ItemEntity.class,new AABB(STASH).inflate(5));check.accept(result.getResult().consumesAction()&&bag.getCount()==1&&drops.size()>=8&&drops.size()<=12&&drops.stream().noneMatch(e->e.getItem().isEmpty()),"actual bag use consumes one bag and emits 8..12 native drops in "+mode+" tier "+rarity);drops.forEach(ItemEntity::discard);
        }
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        int slot=0;l.setBlockAndUpdate(STASH,Blocks.CHEST.defaultBlockState());var stash=(ChestBlockEntity)l.getBlockEntity(STASH);stash.clearContent();
        for(var block:List.of(R5Content.LOOT_URN.get(),R5Content.LOOT_CRATE.get()))for(int rarity=0;rarity<3;rarity++){
            var stack=AncientLootBlockItem.stack(block,rarity);roundTrip(l,stack,check,"loot container tier "+rarity);var p=STASH.offset(0,0,3+slot);l.setBlockAndUpdate(p.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());var hit=new BlockHitResult(Vec3.atCenterOf(p.below()).add(0,.5,0),Direction.UP,p.below(),false);var result=stack.getItem().useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));var state=l.getBlockState(p);check.accept(result.consumesAction()&&state.is(block)&&state.getValue(AncientLootBlock.RARITY)==rarity,"real block-item placement applies rarity components: "+block+" tier "+rarity);
            var clone=block.getCloneItemStack(state,hit,l,p,player);check.accept(ItemStack.matches(clone,AncientLootBlockItem.stack(block,rarity)),"pick block preserves rarity and original container variant: "+slot);
            for(boolean silk:new boolean[]{false,true}){var tool=new ItemStack(Items.DIAMOND_PICKAXE);if(silk)tool.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);var drops=Block.getDrops(state,l,p,null,player,tool);check.accept(drops.size()>=1+rarity&&drops.size()<=3+rarity&&drops.stream().noneMatch(e->e.is(block.asItem())||e.isEmpty()),"native block drops preserve original 1+rarity..3+rarity independent of Silk Touch: "+slot+"/"+silk);}
            stash.setItem(slot++,clone);l.setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());
        }
        for(int i=0;i<6;i++){var ring=R5Content.DISCOUNT_RINGS.get(i).get();for(var aspect:Aspects.PRIMALS)check.accept(ring.getVisDiscount(new ItemStack(ring),player,aspect)==(aspect==ring.aspect()?1:0),"loot ring discounts exactly its original primal aspect: "+i+" "+aspect.getTag());check.accept(new ItemStack(ring).is(net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.parse("curios:ring"))),"native Curios ring tag admits ancient aspect ring "+i);stash.setItem(slot++,new ItemStack(ring));}
        for(var bag:R5Content.LOOT_BAGS)stash.setItem(slot++,new ItemStack(bag.get(),4));
        var potion=pools().get(2).stream().filter(e->e.item.is(Items.SPLASH_POTION)).findFirst().orElseThrow().item;stash.setItem(slot++,potion.copy());
        structures(l,check);check.accept(stash.getContainerSize()==27,"persistent fixture uses an actual native chest");
    }
    private static void roundTrip(ServerLevel l,ItemStack stack,BiConsumer<Boolean,String> check,String label){var saved=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)stack.save(l.registryAccess()));var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),l.registryAccess());try{ItemStack.STREAM_CODEC.encode(buffer,stack);var synced=ItemStack.STREAM_CODEC.decode(buffer);check.accept(ItemStack.matches(stack,saved)&&ItemStack.matches(stack,synced)&&!buffer.isReadable(),"native save and multiplayer codecs preserve "+label);}finally{buffer.release();}}
    private static String originalEffect(int id){return switch(id){case 1->"speed";case 2->"slowness";case 5->"strength";case 6->"instant_health";case 7->"instant_damage";case 10->"regeneration";case 12->"fire_resistance";case 13->"water_breathing";case 14->"invisibility";case 16->"night_vision";case 18->"weakness";case 19->"poison";default->throw new AssertionError("Unexpected original potion effect "+id);};}
    private static void terrain(ServerLevel l,BlockPos origin,int radius,int surface){for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)for(int y=-2;y<=18;y++)l.setBlock(origin.offset(x,y,z),(y<surface?Blocks.STONE:y==surface?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);}
    private static void structures(ServerLevel l,BiConsumer<Boolean,String> check){
        terrain(l,MOUND,20,9);l.setBlockAndUpdate(MOUND.offset(18,9,18),Blocks.SAND.defaultBlockState());check.accept(!new LegacyMound().generate(l,RandomSource.create(42),MOUND)&&l.getBlockState(MOUND.offset(10,1,9)).is(Blocks.STONE),"mound rejects one invalid corner before writing any geometry");terrain(l,MOUND,20,9);check.accept(R3Worldgen.MOUND.get().place(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE,l,l.getChunkSource().getGenerator(),RandomSource.create(42),MOUND),"registered native mound feature generates on its five valid support points");
        for(var p:List.of(MOUND.offset(9,1,7),MOUND.offset(9,1,11)))check.accept(l.getBlockState(p).getBlock() instanceof AncientLootBlock,"mound contains an actual ancient loot container at "+p);
        check.accept(l.getBlockEntity(MOUND.offset(4,5,4)) instanceof SpawnerBlockEntity s&&s.saveWithoutMetadata(l.registryAccess()).toString().contains("minecraft:skeleton"),"mound first native spawner targets skeletons");check.accept(l.getBlockEntity(MOUND.offset(4,5,14)) instanceof SpawnerBlockEntity s&&s.saveWithoutMetadata(l.registryAccess()).toString().contains("minecraft:zombie"),"mound second native spawner targets zombies");
        var chest=(ChestBlockEntity)l.getBlockEntity(MOUND.offset(10,1,9));check.accept(chest!=null&&AncientStructureSupport.DUNGEON.equals(chest.getLootTable()),"mound chest is assigned the approved double dungeon table");check.accept(l.getBlockEntity(MOUND.offset(9,8,9)) instanceof AuraNodeBlockEntity n&&n.state.type==NodeState.Type.DARK,"mound creates the original forced-dark aura node");
        check.accept(!new LegacyHilltopStones().generate(l,RandomSource.create(1),new BlockPos(HILL.getX(),84,HILL.getZ())),"hilltop circle keeps original minimum elevation 85");terrain(l,HILL,6,-1);check.accept(R3Worldgen.HILLTOP_STONES.get().place(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE,l,l.getChunkSource().getGenerator(),RandomSource.create(57),HILL),"registered highland stone-circle feature generates on valid supports");
        check.accept(l.getBlockEntity(HILL) instanceof SpawnerBlockEntity s&&s.saveWithoutMetadata(l.registryAccess()).toString().contains("thaumcraft:wisp"),"stone circle embeds a native Wisp spawner");check.accept(l.getBlockEntity(HILL.above(2)) instanceof ChestBlockEntity c&&AncientStructureSupport.DUNGEON.equals(c.getLootTable()),"stone-circle chest also uses the double dungeon table");
        int columns=0;for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(Math.abs(x)==3&&Math.abs(z)<3&&Math.abs(z%2)==1||Math.abs(z)==3&&Math.abs(x)<3&&Math.abs(x%2)==1){columns++;check.accept(l.getBlockState(HILL.offset(x,1,z)).is(R3Content.ARCANE_STONE.get())&&l.getBlockState(HILL.offset(x,2,z)).is(R3Content.ARCANE_STONE.get())&&!l.getBlockState(HILL.offset(x,5,z)).is(R3Content.ARCANE_STONE.get()),"circle pillar uses original height range 2..4: "+x+","+z);}check.accept(columns==8,"stone circle retains eight outer pillars");
        var table=l.getServer().reloadableRegistries().getLootTable(AncientStructureSupport.DUNGEON);check.accept(table!=LootTable.EMPTY,"double dungeon table loads through the actual native loot registry");var params=new LootParams.Builder(l).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(HILL)).create(LootContextParamSets.CHEST);var loot=table.getRandomItems(params,710L);check.accept(!loot.isEmpty()&&loot.stream().noneMatch(ItemStack::isEmpty),"two nested modern dungeon tables produce real native chest loot");
    }
    static void reload(ServerLevel l,BiConsumer<Boolean,String> check){var stash=(ChestBlockEntity)l.getBlockEntity(STASH);check.accept(stash!=null,"ancient-loot fixture survives real server restart");for(int i=0;i<6;i++){var stack=stash.getItem(i);var block=i<3?R5Content.LOOT_URN.get():R5Content.LOOT_CRATE.get();check.accept(ItemStack.matches(stack,AncientLootBlockItem.stack(block,i%3)),"stateful ancient container survives restart: "+i);}for(int i=0;i<6;i++)check.accept(stash.getItem(i+6).is(R5Content.DISCOUNT_RINGS.get(i).get()),"primal ring survives restart: "+i);for(int i=0;i<3;i++)check.accept(stash.getItem(i+12).is(R5Content.LOOT_BAGS.get(i).get())&&stash.getItem(i+12).getCount()==4,"bag tier and count survive restart: "+i);check.accept(stash.getItem(15).is(Items.SPLASH_POTION)&&stash.getItem(15).get(DataComponents.POTION_CONTENTS)!=null,"original custom potion survives restart");for(var p:List.of(MOUND.offset(10,1,9),HILL.above(2)))check.accept(l.getBlockEntity(p) instanceof ChestBlockEntity c&&AncientStructureSupport.DUNGEON.equals(c.getLootTable()),"unopened structure chest retains deferred double loot through restart: "+p);check.accept(l.getBlockEntity(MOUND.offset(9,8,9)) instanceof AuraNodeBlockEntity n&&n.state.type==NodeState.Type.DARK&&!n.state.base.isEmpty(),"structure dark node and aspects survive actual restart");for(var p:List.of(MOUND.offset(4,5,4),MOUND.offset(4,5,14),HILL))check.accept(l.getBlockEntity(p) instanceof SpawnerBlockEntity,"structure spawner survives restart: "+p);}
}
